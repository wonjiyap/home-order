package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.CategoryEntity
import com.wonjiyap.homeorder.domain.MenuEntity
import com.wonjiyap.homeorder.domain.PartyEntity
import com.wonjiyap.homeorder.domain.UserEntity
import com.wonjiyap.homeorder.enums.PartyStatus
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.service.dto.OptionGroupCreateParam
import com.wonjiyap.homeorder.service.dto.OptionGroupDeleteParam
import com.wonjiyap.homeorder.service.dto.OptionGroupGetParam
import com.wonjiyap.homeorder.service.dto.OptionGroupListParam
import com.wonjiyap.homeorder.service.dto.OptionGroupUpdateParam
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
class OptionGroupServiceTest {

    @Autowired
    private lateinit var optionGroupService: OptionGroupService

    private var testUserId: Long = 0
    private var testMenuId: Long = 0

    @BeforeEach
    fun setUp() {
        testUserId = transaction {
            UserEntity.new {
                loginId = "optiongroup_test_${System.nanoTime()}"
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
        testMenuId = transaction {
            MenuEntity.new {
                this.categoryId = categoryId
                name = "테스트 메뉴"
                displayOrder = 0
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }
    }

    @Test
    fun `옵션 그룹 생성 테스트`() {
        // Given
        val param = OptionGroupCreateParam(
            menuId = testMenuId,
            hostId = testUserId,
            name = "사이즈",
            isRequired = true,
        )

        // When
        val optionGroup = optionGroupService.create(param)

        // Then
        assertThat(optionGroup.id.value).isGreaterThan(0)
        assertThat(optionGroup.menuId).isEqualTo(testMenuId)
        assertThat(optionGroup.name).isEqualTo("사이즈")
        assertThat(optionGroup.isRequired).isTrue()
        assertThat(optionGroup.deletedAt).isNull()
    }

    @Test
    fun `중복된 옵션 그룹 이름 생성시 예외 발생 테스트`() {
        // Given
        optionGroupService.create(
            OptionGroupCreateParam(
                menuId = testMenuId,
                hostId = testUserId,
                name = "사이즈",
            )
        )

        // When & Then
        assertThatThrownBy {
            optionGroupService.create(
                OptionGroupCreateParam(
                    menuId = testMenuId,
                    hostId = testUserId,
                    name = "사이즈",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT)
            .hasMessageContaining("같은 이름의 옵션 그룹이 이미 존재합니다")
    }

    @Test
    fun `존재하지 않는 메뉴에 옵션 그룹 생성시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            optionGroupService.create(
                OptionGroupCreateParam(
                    menuId = 999999L,
                    hostId = testUserId,
                    name = "사이즈",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("메뉴를 찾을 수 없습니다")
    }

    @Test
    fun `다른 사용자 메뉴에 옵션 그룹 생성시 예외 발생 테스트`() {
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
            optionGroupService.create(
                OptionGroupCreateParam(
                    menuId = testMenuId,
                    hostId = otherUserId,
                    name = "사이즈",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN)
    }

    @Test
    fun `옵션 그룹 목록 조회 테스트`() {
        // Given
        optionGroupService.create(
            OptionGroupCreateParam(
                menuId = testMenuId,
                hostId = testUserId,
                name = "사이즈",
            )
        )
        optionGroupService.create(
            OptionGroupCreateParam(
                menuId = testMenuId,
                hostId = testUserId,
                name = "토핑",
            )
        )

        // When
        val optionGroups = optionGroupService.list(
            OptionGroupListParam(
                menuId = testMenuId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(optionGroups).hasSize(2)
    }

    @Test
    fun `삭제된 옵션 그룹은 목록에서 제외됨 테스트`() {
        // Given
        val optionGroup1 = optionGroupService.create(
            OptionGroupCreateParam(
                menuId = testMenuId,
                hostId = testUserId,
                name = "사이즈",
            )
        )
        optionGroupService.create(
            OptionGroupCreateParam(
                menuId = testMenuId,
                hostId = testUserId,
                name = "토핑",
            )
        )

        optionGroupService.delete(
            OptionGroupDeleteParam(
                id = optionGroup1.id.value,
                menuId = testMenuId,
                hostId = testUserId,
            )
        )

        // When
        val optionGroups = optionGroupService.list(
            OptionGroupListParam(
                menuId = testMenuId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(optionGroups).hasSize(1)
        assertThat(optionGroups[0].name).isEqualTo("토핑")
    }

    @Test
    fun `옵션 그룹 상세 조회 테스트`() {
        // Given
        val created = optionGroupService.create(
            OptionGroupCreateParam(
                menuId = testMenuId,
                hostId = testUserId,
                name = "사이즈",
                isRequired = true,
            )
        )

        // When
        val optionGroup = optionGroupService.get(
            OptionGroupGetParam(
                id = created.id.value,
                menuId = testMenuId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(optionGroup.id.value).isEqualTo(created.id.value)
        assertThat(optionGroup.name).isEqualTo("사이즈")
        assertThat(optionGroup.isRequired).isTrue()
    }

    @Test
    fun `존재하지 않는 옵션 그룹 조회시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            optionGroupService.get(
                OptionGroupGetParam(
                    id = 999999L,
                    menuId = testMenuId,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("옵션 그룹을 찾을 수 없습니다")
    }

    @Test
    fun `옵션 그룹 이름 수정 테스트`() {
        // Given
        val created = optionGroupService.create(
            OptionGroupCreateParam(
                menuId = testMenuId,
                hostId = testUserId,
                name = "사이즈",
            )
        )

        // When
        val updated = optionGroupService.update(
            OptionGroupUpdateParam(
                id = created.id.value,
                menuId = testMenuId,
                hostId = testUserId,
                name = "크기",
            )
        )

        // Then
        assertThat(updated.name).isEqualTo("크기")
    }

    @Test
    fun `옵션 그룹 필수 여부 수정 테스트`() {
        // Given
        val created = optionGroupService.create(
            OptionGroupCreateParam(
                menuId = testMenuId,
                hostId = testUserId,
                name = "사이즈",
                isRequired = false,
            )
        )

        // When
        val updated = optionGroupService.update(
            OptionGroupUpdateParam(
                id = created.id.value,
                menuId = testMenuId,
                hostId = testUserId,
                isRequired = true,
            )
        )

        // Then
        assertThat(updated.isRequired).isTrue()
    }

    @Test
    fun `같은 이름으로 수정시 검증 생략 테스트`() {
        // Given
        val created = optionGroupService.create(
            OptionGroupCreateParam(
                menuId = testMenuId,
                hostId = testUserId,
                name = "사이즈",
            )
        )

        // When
        val updated = optionGroupService.update(
            OptionGroupUpdateParam(
                id = created.id.value,
                menuId = testMenuId,
                hostId = testUserId,
                name = "사이즈",
            )
        )

        // Then
        assertThat(updated.name).isEqualTo("사이즈")
    }

    @Test
    fun `수정시 중복된 이름으로 변경하면 예외 발생 테스트`() {
        // Given
        optionGroupService.create(
            OptionGroupCreateParam(
                menuId = testMenuId,
                hostId = testUserId,
                name = "사이즈",
            )
        )
        val optionGroup2 = optionGroupService.create(
            OptionGroupCreateParam(
                menuId = testMenuId,
                hostId = testUserId,
                name = "토핑",
            )
        )

        // When & Then
        assertThatThrownBy {
            optionGroupService.update(
                OptionGroupUpdateParam(
                    id = optionGroup2.id.value,
                    menuId = testMenuId,
                    hostId = testUserId,
                    name = "사이즈",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT)
    }

    @Test
    fun `옵션 그룹 삭제 테스트 (Soft Delete)`() {
        // Given
        val created = optionGroupService.create(
            OptionGroupCreateParam(
                menuId = testMenuId,
                hostId = testUserId,
                name = "사이즈",
            )
        )

        // When
        optionGroupService.delete(
            OptionGroupDeleteParam(
                id = created.id.value,
                menuId = testMenuId,
                hostId = testUserId,
            )
        )

        // Then
        assertThatThrownBy {
            optionGroupService.get(
                OptionGroupGetParam(
                    id = created.id.value,
                    menuId = testMenuId,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `삭제된 옵션 그룹과 같은 이름으로 새 옵션 그룹 생성 가능 테스트`() {
        // Given
        val optionGroup1 = optionGroupService.create(
            OptionGroupCreateParam(
                menuId = testMenuId,
                hostId = testUserId,
                name = "사이즈",
            )
        )
        optionGroupService.delete(
            OptionGroupDeleteParam(
                id = optionGroup1.id.value,
                menuId = testMenuId,
                hostId = testUserId,
            )
        )

        // When
        val optionGroup2 = optionGroupService.create(
            OptionGroupCreateParam(
                menuId = testMenuId,
                hostId = testUserId,
                name = "사이즈",
            )
        )

        // Then
        assertThat(optionGroup2.id.value).isNotEqualTo(optionGroup1.id.value)
        assertThat(optionGroup2.name).isEqualTo("사이즈")
    }
}
