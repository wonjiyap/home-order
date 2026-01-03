package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.PartyEntity
import com.wonjiyap.homeorder.domain.UserEntity
import com.wonjiyap.homeorder.enums.PartyStatus
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.service.dto.CategoryCreateParam
import com.wonjiyap.homeorder.service.dto.CategoryDeleteParam
import com.wonjiyap.homeorder.service.dto.CategoryGetParam
import com.wonjiyap.homeorder.service.dto.CategoryListParam
import com.wonjiyap.homeorder.service.dto.CategoryReorderParam
import com.wonjiyap.homeorder.service.dto.CategoryUpdateParam
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
class CategoryServiceTest {

    @Autowired
    private lateinit var categoryService: CategoryService

    private var testUserId: Long = 0
    private var testPartyId: Long = 0

    @BeforeEach
    fun setUp() {
        testUserId = transaction {
            UserEntity.new {
                loginId = "categorytest_${System.nanoTime()}"
                password = "password123"
                nickname = "카테고리테스트유저"
            }.id.value
        }
        testPartyId = transaction {
            PartyEntity.new {
                hostId = testUserId
                name = "테스트 파티"
                status = PartyStatus.PLANNING
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }
    }

    @Test
    fun `카테고리 생성 테스트`() {
        // Given
        val param = CategoryCreateParam(
            partyId = testPartyId,
            hostId = testUserId,
            name = "음료",
        )

        // When
        val category = categoryService.create(param)

        // Then
        assertThat(category.id.value).isGreaterThan(0)
        assertThat(category.partyId).isEqualTo(testPartyId)
        assertThat(category.name).isEqualTo("음료")
        assertThat(category.displayOrder).isEqualTo(0)
        assertThat(category.deletedAt).isNull()
    }

    @Test
    fun `카테고리 생성시 displayOrder 자동 증가 테스트`() {
        // Given
        categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "음료",
            )
        )

        // When
        val category2 = categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "음식",
            )
        )
        val category3 = categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "디저트",
            )
        )

        // Then
        assertThat(category2.displayOrder).isEqualTo(1)
        assertThat(category3.displayOrder).isEqualTo(2)
    }

    @Test
    fun `중복된 카테고리 이름 생성시 예외 발생 테스트`() {
        // Given
        categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "음료",
            )
        )

        // When & Then
        assertThatThrownBy {
            categoryService.create(
                CategoryCreateParam(
                    partyId = testPartyId,
                    hostId = testUserId,
                    name = "음료",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT)
            .hasMessageContaining("같은 이름의 카테고리가 이미 존재합니다")
    }

    @Test
    fun `대소문자 구분 없이 중복 체크 테스트`() {
        // Given
        categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "Drink",
            )
        )

        // When & Then
        assertThatThrownBy {
            categoryService.create(
                CategoryCreateParam(
                    partyId = testPartyId,
                    hostId = testUserId,
                    name = "drink",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT)
    }

    @Test
    fun `존재하지 않는 파티에 카테고리 생성시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            categoryService.create(
                CategoryCreateParam(
                    partyId = 999999L,
                    hostId = testUserId,
                    name = "음료",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("파티를 찾을 수 없습니다")
    }

    @Test
    fun `다른 사용자 파티에 카테고리 생성시 예외 발생 테스트`() {
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
            categoryService.create(
                CategoryCreateParam(
                    partyId = testPartyId,
                    hostId = otherUserId,
                    name = "음료",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `카테고리 목록 조회 테스트`() {
        // Given
        categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "음료",
            )
        )
        categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "음식",
            )
        )

        // When
        val categories = categoryService.list(
            CategoryListParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(categories).hasSize(2)
    }

    @Test
    fun `삭제된 카테고리는 목록에서 제외됨 테스트`() {
        // Given
        val category1 = categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "음료",
            )
        )
        categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "음식",
            )
        )

        categoryService.delete(
            CategoryDeleteParam(
                id = category1.id.value,
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // When
        val categories = categoryService.list(
            CategoryListParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(categories).hasSize(1)
        assertThat(categories[0].name).isEqualTo("음식")
    }

    @Test
    fun `카테고리 상세 조회 테스트`() {
        // Given
        val created = categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "음료",
            )
        )

        // When
        val category = categoryService.get(
            CategoryGetParam(
                id = created.id.value,
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(category.id.value).isEqualTo(created.id.value)
        assertThat(category.name).isEqualTo("음료")
    }

    @Test
    fun `존재하지 않는 카테고리 조회시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            categoryService.get(
                CategoryGetParam(
                    id = 999999L,
                    partyId = testPartyId,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("카테고리를 찾을 수 없습니다")
    }

    @Test
    fun `카테고리 이름 수정 테스트`() {
        // Given
        val created = categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "음료",
            )
        )

        // When
        val updated = categoryService.update(
            CategoryUpdateParam(
                id = created.id.value,
                partyId = testPartyId,
                hostId = testUserId,
                name = "음료수",
            )
        )

        // Then
        assertThat(updated.name).isEqualTo("음료수")
    }

    @Test
    fun `같은 이름으로 수정시 검증 생략 테스트`() {
        // Given
        val created = categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "음료",
            )
        )

        // When - 같은 이름으로 수정해도 에러 없음
        val updated = categoryService.update(
            CategoryUpdateParam(
                id = created.id.value,
                partyId = testPartyId,
                hostId = testUserId,
                name = "음료",
            )
        )

        // Then
        assertThat(updated.name).isEqualTo("음료")
    }

    @Test
    fun `수정시 중복된 이름으로 변경하면 예외 발생 테스트`() {
        // Given
        categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "음료",
            )
        )
        val category2 = categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "음식",
            )
        )

        // When & Then
        assertThatThrownBy {
            categoryService.update(
                CategoryUpdateParam(
                    id = category2.id.value,
                    partyId = testPartyId,
                    hostId = testUserId,
                    name = "음료",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT)
    }

    @Test
    fun `존재하지 않는 카테고리 수정시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            categoryService.update(
                CategoryUpdateParam(
                    id = 999999L,
                    partyId = testPartyId,
                    hostId = testUserId,
                    name = "음료",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `카테고리 삭제 테스트 (Soft Delete)`() {
        // Given
        val created = categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "음료",
            )
        )

        // When
        categoryService.delete(
            CategoryDeleteParam(
                id = created.id.value,
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // Then - 삭제된 카테고리는 조회되지 않음
        assertThatThrownBy {
            categoryService.get(
                CategoryGetParam(
                    id = created.id.value,
                    partyId = testPartyId,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `존재하지 않는 카테고리 삭제시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            categoryService.delete(
                CategoryDeleteParam(
                    id = 999999L,
                    partyId = testPartyId,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `삭제된 카테고리와 같은 이름으로 새 카테고리 생성 가능 테스트`() {
        // Given
        val category1 = categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "음료",
            )
        )
        categoryService.delete(
            CategoryDeleteParam(
                id = category1.id.value,
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // When
        val category2 = categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "음료",
            )
        )

        // Then
        assertThat(category2.id.value).isNotEqualTo(category1.id.value)
        assertThat(category2.name).isEqualTo("음료")
    }

    @Test
    fun `다른 파티에 같은 이름의 카테고리 생성 가능 테스트`() {
        // Given
        categoryService.create(
            CategoryCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
                name = "음료",
            )
        )

        val anotherPartyId = transaction {
            PartyEntity.new {
                hostId = testUserId
                name = "다른 파티"
                status = PartyStatus.PLANNING
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }

        // When
        val category = categoryService.create(
            CategoryCreateParam(
                partyId = anotherPartyId,
                hostId = testUserId,
                name = "음료",
            )
        )

        // Then
        assertThat(category.name).isEqualTo("음료")
        assertThat(category.partyId).isEqualTo(anotherPartyId)
    }

    // ==================== Reorder 테스트 ====================

    @Test
    fun `카테고리 순서 변경 테스트`() {
        // Given
        val category1 = categoryService.create(
            CategoryCreateParam(partyId = testPartyId, hostId = testUserId, name = "음료")
        )
        val category2 = categoryService.create(
            CategoryCreateParam(partyId = testPartyId, hostId = testUserId, name = "음식")
        )
        val category3 = categoryService.create(
            CategoryCreateParam(partyId = testPartyId, hostId = testUserId, name = "디저트")
        )

        // When - 순서를 3, 1, 2로 변경
        val reordered = categoryService.reorder(
            CategoryReorderParam(
                partyId = testPartyId,
                hostId = testUserId,
                categoryIds = listOf(category3.id.value, category1.id.value, category2.id.value),
            )
        )

        // Then
        assertThat(reordered).hasSize(3)
        assertThat(reordered[0].id.value).isEqualTo(category3.id.value)
        assertThat(reordered[0].displayOrder).isEqualTo(0)
        assertThat(reordered[1].id.value).isEqualTo(category1.id.value)
        assertThat(reordered[1].displayOrder).isEqualTo(1)
        assertThat(reordered[2].id.value).isEqualTo(category2.id.value)
        assertThat(reordered[2].displayOrder).isEqualTo(2)
    }

    @Test
    fun `일부 카테고리만 순서 변경시 나머지는 뒤로 배치 테스트`() {
        // Given
        val category1 = categoryService.create(
            CategoryCreateParam(partyId = testPartyId, hostId = testUserId, name = "음료")
        )
        val category2 = categoryService.create(
            CategoryCreateParam(partyId = testPartyId, hostId = testUserId, name = "음식")
        )
        val category3 = categoryService.create(
            CategoryCreateParam(partyId = testPartyId, hostId = testUserId, name = "디저트")
        )

        // When - category3만 맨 앞으로 이동
        val reordered = categoryService.reorder(
            CategoryReorderParam(
                partyId = testPartyId,
                hostId = testUserId,
                categoryIds = listOf(category3.id.value),
            )
        )

        // Then - category3이 0, 나머지는 기존 순서대로 1, 2
        assertThat(reordered).hasSize(3)
        assertThat(reordered[0].id.value).isEqualTo(category3.id.value)
        assertThat(reordered[0].displayOrder).isEqualTo(0)
        assertThat(reordered[1].id.value).isEqualTo(category1.id.value)
        assertThat(reordered[1].displayOrder).isEqualTo(1)
        assertThat(reordered[2].id.value).isEqualTo(category2.id.value)
        assertThat(reordered[2].displayOrder).isEqualTo(2)
    }

    @Test
    fun `존재하지 않는 카테고리 ID로 순서 변경시 예외 발생 테스트`() {
        // Given
        categoryService.create(
            CategoryCreateParam(partyId = testPartyId, hostId = testUserId, name = "음료")
        )

        // When & Then
        assertThatThrownBy {
            categoryService.reorder(
                CategoryReorderParam(
                    partyId = testPartyId,
                    hostId = testUserId,
                    categoryIds = listOf(999999L),
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `다른 사용자 파티의 카테고리 순서 변경시 예외 발생 테스트`() {
        // Given
        val category = categoryService.create(
            CategoryCreateParam(partyId = testPartyId, hostId = testUserId, name = "음료")
        )
        val otherUserId = transaction {
            UserEntity.new {
                loginId = "otheruser_${System.nanoTime()}"
                password = "password123"
                nickname = "다른유저"
            }.id.value
        }

        // When & Then
        assertThatThrownBy {
            categoryService.reorder(
                CategoryReorderParam(
                    partyId = testPartyId,
                    hostId = otherUserId,
                    categoryIds = listOf(category.id.value),
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `빈 목록으로 순서 변경시 기존 순서 유지 테스트`() {
        // Given
        val category1 = categoryService.create(
            CategoryCreateParam(partyId = testPartyId, hostId = testUserId, name = "음료")
        )
        val category2 = categoryService.create(
            CategoryCreateParam(partyId = testPartyId, hostId = testUserId, name = "음식")
        )

        // When - 빈 목록 전달
        val reordered = categoryService.reorder(
            CategoryReorderParam(
                partyId = testPartyId,
                hostId = testUserId,
                categoryIds = emptyList(),
            )
        )

        // Then - 기존 순서 유지
        assertThat(reordered).hasSize(2)
        assertThat(reordered[0].id.value).isEqualTo(category1.id.value)
        assertThat(reordered[0].displayOrder).isEqualTo(0)
        assertThat(reordered[1].id.value).isEqualTo(category2.id.value)
        assertThat(reordered[1].displayOrder).isEqualTo(1)
    }
}
