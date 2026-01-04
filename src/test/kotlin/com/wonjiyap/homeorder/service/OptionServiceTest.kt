package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.CategoryEntity
import com.wonjiyap.homeorder.domain.MenuEntity
import com.wonjiyap.homeorder.domain.OptionGroupEntity
import com.wonjiyap.homeorder.domain.PartyEntity
import com.wonjiyap.homeorder.domain.UserEntity
import com.wonjiyap.homeorder.enums.PartyStatus
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.service.dto.OptionCreateParam
import com.wonjiyap.homeorder.service.dto.OptionDeleteParam
import com.wonjiyap.homeorder.service.dto.OptionGetParam
import com.wonjiyap.homeorder.service.dto.OptionListParam
import com.wonjiyap.homeorder.service.dto.OptionReorderParam
import com.wonjiyap.homeorder.service.dto.OptionUpdateParam
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@Transactional
@Rollback
class OptionServiceTest {

    @Autowired
    private lateinit var optionService: OptionService

    private var testUserId: Long = 0
    private var testOptionGroupId: Long = 0

    @BeforeEach
    fun setUp() {
        testUserId = transaction {
            UserEntity.new {
                loginId = "option_test_${System.nanoTime()}"
                password = "password123"
                nickname = "테스트유저"
            }.id.value
        }
        val partyId = transaction {
            PartyEntity.new {
                hostId = testUserId
                name = "테스트 파티"
                status = PartyStatus.PLANNING
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }
        val categoryId = transaction {
            CategoryEntity.new {
                this.partyId = partyId
                name = "테스트 카테고리"
                displayOrder = 0
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }
        val menuId = transaction {
            MenuEntity.new {
                this.categoryId = categoryId
                name = "테스트 메뉴"
                displayOrder = 0
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }
        testOptionGroupId = transaction {
            OptionGroupEntity.new {
                this.menuId = menuId
                name = "테스트 옵션그룹"
                isRequired = false
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }
    }

    @Test
    fun `옵션 생성 테스트`() {
        // Given
        val param = OptionCreateParam(
            optionGroupId = testOptionGroupId,
            hostId = testUserId,
            name = "Large",
        )

        // When
        val option = optionService.create(param)

        // Then
        assertThat(option.id.value).isGreaterThan(0)
        assertThat(option.optionGroupId).isEqualTo(testOptionGroupId)
        assertThat(option.name).isEqualTo("Large")
        assertThat(option.displayOrder).isEqualTo(0)
        assertThat(option.deletedAt).isNull()
    }

    @Test
    fun `옵션 생성시 displayOrder 자동 증가 테스트`() {
        // Given
        optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Small",
            )
        )

        // When
        val option2 = optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Medium",
            )
        )
        val option3 = optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Large",
            )
        )

        // Then
        assertThat(option2.displayOrder).isEqualTo(1)
        assertThat(option3.displayOrder).isEqualTo(2)
    }

    @Test
    fun `중복된 옵션 이름 생성시 예외 발생 테스트`() {
        // Given
        optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Large",
            )
        )

        // When & Then
        assertThatThrownBy {
            optionService.create(
                OptionCreateParam(
                    optionGroupId = testOptionGroupId,
                    hostId = testUserId,
                    name = "Large",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT)
            .hasMessageContaining("같은 이름의 옵션이 이미 존재합니다")
    }

    @Test
    fun `존재하지 않는 옵션 그룹에 옵션 생성시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            optionService.create(
                OptionCreateParam(
                    optionGroupId = 999999L,
                    hostId = testUserId,
                    name = "Large",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("옵션 그룹을 찾을 수 없습니다")
    }

    @Test
    fun `다른 사용자 옵션 그룹에 옵션 생성시 예외 발생 테스트`() {
        // Given
        val otherUserId = transaction {
            UserEntity.new {
                loginId = "otheruser_${System.nanoTime()}"
                password = "password123"
                nickname = "다른유저"
            }.id.value
        }

        // When & Then
        assertThatThrownBy {
            optionService.create(
                OptionCreateParam(
                    optionGroupId = testOptionGroupId,
                    hostId = otherUserId,
                    name = "Large",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN)
    }

    @Test
    fun `옵션 목록 조회 테스트`() {
        // Given
        optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Small",
            )
        )
        optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Large",
            )
        )

        // When
        val options = optionService.list(
            OptionListParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(options).hasSize(2)
    }

    @Test
    fun `삭제된 옵션은 목록에서 제외됨 테스트`() {
        // Given
        val option1 = optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Small",
            )
        )
        optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Large",
            )
        )

        optionService.delete(
            OptionDeleteParam(
                id = option1.id.value,
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
            )
        )

        // When
        val options = optionService.list(
            OptionListParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(options).hasSize(1)
        assertThat(options[0].name).isEqualTo("Large")
    }

    @Test
    fun `옵션 상세 조회 테스트`() {
        // Given
        val created = optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Large",
            )
        )

        // When
        val option = optionService.get(
            OptionGetParam(
                id = created.id.value,
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(option.id.value).isEqualTo(created.id.value)
        assertThat(option.name).isEqualTo("Large")
    }

    @Test
    fun `존재하지 않는 옵션 조회시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            optionService.get(
                OptionGetParam(
                    id = 999999L,
                    optionGroupId = testOptionGroupId,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("옵션을 찾을 수 없습니다")
    }

    @Test
    fun `옵션 이름 수정 테스트`() {
        // Given
        val created = optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Large",
            )
        )

        // When
        val updated = optionService.update(
            OptionUpdateParam(
                id = created.id.value,
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Extra Large",
            )
        )

        // Then
        assertThat(updated.name).isEqualTo("Extra Large")
    }

    @Test
    fun `같은 이름으로 수정시 검증 생략 테스트`() {
        // Given
        val created = optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Large",
            )
        )

        // When
        val updated = optionService.update(
            OptionUpdateParam(
                id = created.id.value,
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Large",
            )
        )

        // Then
        assertThat(updated.name).isEqualTo("Large")
    }

    @Test
    fun `수정시 중복된 이름으로 변경하면 예외 발생 테스트`() {
        // Given
        optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Small",
            )
        )
        val option2 = optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Large",
            )
        )

        // When & Then
        assertThatThrownBy {
            optionService.update(
                OptionUpdateParam(
                    id = option2.id.value,
                    optionGroupId = testOptionGroupId,
                    hostId = testUserId,
                    name = "Small",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT)
    }

    @Test
    fun `옵션 삭제 테스트 (Soft Delete)`() {
        // Given
        val created = optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Large",
            )
        )

        // When
        optionService.delete(
            OptionDeleteParam(
                id = created.id.value,
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
            )
        )

        // Then
        assertThatThrownBy {
            optionService.get(
                OptionGetParam(
                    id = created.id.value,
                    optionGroupId = testOptionGroupId,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `삭제된 옵션과 같은 이름으로 새 옵션 생성 가능 테스트`() {
        // Given
        val option1 = optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Large",
            )
        )
        optionService.delete(
            OptionDeleteParam(
                id = option1.id.value,
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
            )
        )

        // When
        val option2 = optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Large",
            )
        )

        // Then
        assertThat(option2.id.value).isNotEqualTo(option1.id.value)
        assertThat(option2.name).isEqualTo("Large")
    }

    @Test
    fun `다른 옵션 그룹에 같은 이름의 옵션 생성 가능 테스트`() {
        // Given
        optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Large",
            )
        )

        val anotherOptionGroupId = transaction {
            val menuId = OptionGroupEntity.findById(testOptionGroupId)!!.menuId
            OptionGroupEntity.new {
                this.menuId = menuId
                name = "다른 옵션그룹"
                isRequired = false
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }

        // When
        val option = optionService.create(
            OptionCreateParam(
                optionGroupId = anotherOptionGroupId,
                hostId = testUserId,
                name = "Large",
            )
        )

        // Then
        assertThat(option.name).isEqualTo("Large")
        assertThat(option.optionGroupId).isEqualTo(anotherOptionGroupId)
    }

    @Test
    fun `옵션 순서 변경 테스트`() {
        // Given
        val option1 = optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Small",
            )
        )
        val option2 = optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Medium",
            )
        )
        val option3 = optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Large",
            )
        )

        // When - 역순으로 변경
        val reordered = optionService.reorder(
            OptionReorderParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                optionIds = listOf(option3.id.value, option2.id.value, option1.id.value),
            )
        )

        // Then
        assertThat(reordered).hasSize(3)
        assertThat(reordered[0].id.value).isEqualTo(option3.id.value)
        assertThat(reordered[0].displayOrder).isEqualTo(0)
        assertThat(reordered[1].id.value).isEqualTo(option2.id.value)
        assertThat(reordered[1].displayOrder).isEqualTo(1)
        assertThat(reordered[2].id.value).isEqualTo(option1.id.value)
        assertThat(reordered[2].displayOrder).isEqualTo(2)
    }

    @Test
    fun `일부 옵션만 순서 지정시 나머지는 뒤에 유지 테스트`() {
        // Given
        val option1 = optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Small",
            )
        )
        val option2 = optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Medium",
            )
        )
        val option3 = optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Large",
            )
        )

        // When - option3만 첫번째로
        val reordered = optionService.reorder(
            OptionReorderParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                optionIds = listOf(option3.id.value),
            )
        )

        // Then
        assertThat(reordered).hasSize(3)
        assertThat(reordered[0].id.value).isEqualTo(option3.id.value)
        assertThat(reordered[0].displayOrder).isEqualTo(0)
        assertThat(reordered[1].id.value).isEqualTo(option1.id.value)
        assertThat(reordered[1].displayOrder).isEqualTo(1)
        assertThat(reordered[2].id.value).isEqualTo(option2.id.value)
        assertThat(reordered[2].displayOrder).isEqualTo(2)
    }

    @Test
    fun `존재하지 않는 옵션 ID로 순서 변경시 예외 발생 테스트`() {
        // Given
        optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Small",
            )
        )

        // When & Then
        assertThatThrownBy {
            optionService.reorder(
                OptionReorderParam(
                    optionGroupId = testOptionGroupId,
                    hostId = testUserId,
                    optionIds = listOf(999999L),
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("옵션을 찾을 수 없습니다")
    }

    @Test
    fun `다른 사용자가 옵션 순서 변경시 예외 발생 테스트`() {
        // Given
        val option1 = optionService.create(
            OptionCreateParam(
                optionGroupId = testOptionGroupId,
                hostId = testUserId,
                name = "Small",
            )
        )

        val otherUserId = transaction {
            UserEntity.new {
                loginId = "otheruser_reorder_${System.nanoTime()}"
                password = "password123"
                nickname = "다른유저"
            }.id.value
        }

        // When & Then
        assertThatThrownBy {
            optionService.reorder(
                OptionReorderParam(
                    optionGroupId = testOptionGroupId,
                    hostId = otherUserId,
                    optionIds = listOf(option1.id.value),
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN)
    }
}
