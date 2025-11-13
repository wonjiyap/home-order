package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.CategoryEntity
import com.wonjiyap.homeorder.repository.dto.CategoryFetchOneParam
import com.wonjiyap.homeorder.repository.dto.CategoryFetchParam
import org.assertj.core.api.Assertions.assertThat
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
class CategoryRepositoryTest {

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @BeforeEach
    fun setUp() {
        categoryRepository = CategoryRepository()
    }

    @Test
    fun `카테고리 생성 및 저장 테스트`() {
        // Given & When
        val category = transaction {
            CategoryEntity.new {
                partyId = 1L
                name = "한식"
                displayOrder = 1
            }
        }
        categoryRepository.save(category)

        // Then
        val param = CategoryFetchOneParam(
            id = category.id.value
        )
        val foundCategory = categoryRepository.fetchOne(param)

        assertThat(foundCategory).isNotNull()
        assertThat(foundCategory?.partyId).isEqualTo(1L)
        assertThat(foundCategory?.name).isEqualTo("한식")
        assertThat(foundCategory?.displayOrder).isEqualTo(1)
    }

    @Test
    fun `파티 ID로 카테고리 목록 조회 테스트`() {
        // Given
        transaction {
            CategoryEntity.new {
                partyId = 1L
                name = "한식"
                displayOrder = 1
            }
            CategoryEntity.new {
                partyId = 1L
                name = "중식"
                displayOrder = 2
            }
            CategoryEntity.new {
                partyId = 2L
                name = "일식"
                displayOrder = 1
            }
        }

        // When
        val param = CategoryFetchParam(
            partyId = 1L
        )
        val categories = categoryRepository.fetch(param)

        // Then
        assertThat(categories).hasSize(2)
        assertThat(categories.all { it.partyId == 1L }).isTrue()
    }

    @Test
    fun `카테고리 이름으로 검색 테스트`() {
        // Given
        transaction {
            CategoryEntity.new {
                partyId = 1L
                name = "한식"
                displayOrder = 1
            }
            CategoryEntity.new {
                partyId = 1L
                name = "중식"
                displayOrder = 2
            }
            CategoryEntity.new {
                partyId = 1L
                name = "일식"
                displayOrder = 3
            }
        }

        // When
        val param = CategoryFetchParam(
            name = "식"
        )
        val categories = categoryRepository.fetch(param)

        // Then
        assertThat(categories).hasSize(3)
        assertThat(categories.all { it.name.contains("식") }).isTrue()
    }

    @Test
    fun `카테고리 이름 대소문자 무시 검색 테스트`() {
        // Given
        transaction {
            CategoryEntity.new {
                partyId = 1L
                name = "Pizza"
                displayOrder = 1
            }
            CategoryEntity.new {
                partyId = 1L
                name = "Pasta"
                displayOrder = 2
            }
            CategoryEntity.new {
                partyId = 1L
                name = "Salad"
                displayOrder = 3
            }
        }

        // When
        val param = CategoryFetchParam(
            name = "PIZZA"
        )
        val categories = categoryRepository.fetch(param)

        // Then
        assertThat(categories).hasSize(1)
        assertThat(categories.first().name).isEqualTo("Pizza")
    }

    @Test
    fun `파티 ID와 카테고리 이름 조합 검색 테스트`() {
        // Given
        transaction {
            CategoryEntity.new {
                partyId = 1L
                name = "한식"
                displayOrder = 1
            }
            CategoryEntity.new {
                partyId = 1L
                name = "중식"
                displayOrder = 2
            }
            CategoryEntity.new {
                partyId = 2L
                name = "한식"
                displayOrder = 1
            }
        }

        // When
        val param = CategoryFetchParam(
            partyId = 1L,
            name = "한식"
        )
        val categories = categoryRepository.fetch(param)

        // Then
        assertThat(categories).hasSize(1)
        assertThat(categories.first().partyId).isEqualTo(1L)
        assertThat(categories.first().name).isEqualTo("한식")
    }

    @Test
    fun `ID로 단일 카테고리 조회 테스트`() {
        // Given
        val category = transaction {
            CategoryEntity.new {
                partyId = 1L
                name = "디저트"
                displayOrder = 5
            }
        }

        // When
        val param = CategoryFetchOneParam(
            id = category.id.value
        )
        val foundCategory = categoryRepository.fetchOne(param)

        // Then
        assertThat(foundCategory).isNotNull()
        assertThat(foundCategory?.id?.value).isEqualTo(category.id.value)
        assertThat(foundCategory?.name).isEqualTo("디저트")
    }

    @Test
    fun `파티 ID와 카테고리 이름으로 단일 조회 테스트`() {
        // Given
        transaction {
            CategoryEntity.new {
                partyId = 1L
                name = "음료"
                displayOrder = 1
            }
            CategoryEntity.new {
                partyId = 1L
                name = "주류"
                displayOrder = 2
            }
        }

        // When
        val param = CategoryFetchOneParam(
            partyId = 1L,
            name = "주류"
        )
        val foundCategory = categoryRepository.fetchOne(param)

        // Then
        assertThat(foundCategory).isNotNull()
        assertThat(foundCategory?.partyId).isEqualTo(1L)
        assertThat(foundCategory?.name).isEqualTo("주류")
    }

    @Test
    fun `존재하지 않는 카테고리 조회 시 null 반환 테스트`() {
        // When
        val param = CategoryFetchOneParam(
            id = 9999L
        )
        val foundCategory = categoryRepository.fetchOne(param)

        // Then
        assertThat(foundCategory).isNull()
    }

    @Test
    fun `카테고리 정보 수정 후 저장 테스트`() {
        // Given
        val category = transaction {
            CategoryEntity.new {
                partyId = 1L
                name = "사이드"
                displayOrder = 3
            }
        }

        // When
        transaction {
            category.name = "사이드 메뉴"
            category.displayOrder = 5
        }
        categoryRepository.save(category)

        // Then
        val param = CategoryFetchOneParam(
            id = category.id.value
        )
        val foundCategory = categoryRepository.fetchOne(param)

        assertThat(foundCategory?.name).isEqualTo("사이드 메뉴")
        assertThat(foundCategory?.displayOrder).isEqualTo(5)
    }

    @Test
    fun `카테고리 soft delete 테스트`() {
        // Given
        val category = transaction {
            CategoryEntity.new {
                partyId = 1L
                name = "삭제될 카테고리"
                displayOrder = 1
            }
        }
        val categoryId = category.id.value

        // When
        transaction {
            category.deletedAt = Instant.now()
        }
        categoryRepository.save(category)

        // Then
        val param = CategoryFetchOneParam(
            id = categoryId
        )
        val foundCategory = categoryRepository.fetchOne(param)
        assertThat(foundCategory).isNull()

        transaction {
            val entity = CategoryEntity.findById(categoryId)
            assertThat(entity).isNotNull()
            assertThat(entity?.deletedAt).isNotNull()
        }
    }

    @Test
    fun `삭제된 카테고리는 fetch 목록에서 제외되는지 테스트`() {
        // Given
        transaction {
            CategoryEntity.new {
                partyId = 1L
                name = "활성 카테고리1"
                displayOrder = 1
            }
            CategoryEntity.new {
                partyId = 1L
                name = "활성 카테고리2"
                displayOrder = 2
            }
            val deletedCategory = CategoryEntity.new {
                partyId = 1L
                name = "삭제된 카테고리"
                displayOrder = 3
            }
            deletedCategory.deletedAt = Instant.now()
        }

        // When
        val param = CategoryFetchParam(
            partyId = 1L
        )
        val categories = categoryRepository.fetch(param)

        // Then
        assertThat(categories).hasSize(2)
        assertThat(categories.map { it.name }).containsExactlyInAnyOrder("활성 카테고리1", "활성 카테고리2")
    }

    @Test
    fun `조건 없이 fetch 호출 시 모든 카테고리 조회 테스트`() {
        // Given
        transaction {
            CategoryEntity.new {
                partyId = 1L
                name = "한식"
                displayOrder = 1
            }
            CategoryEntity.new {
                partyId = 2L
                name = "중식"
                displayOrder = 1
            }
        }

        // When
        val param = CategoryFetchParam()
        val categories = categoryRepository.fetch(param)

        // Then
        assertThat(categories).hasSize(2)
    }

    @Test
    fun `특정 파티의 모든 카테고리를 displayOrder 순으로 조회 테스트`() {
        // Given
        transaction {
            CategoryEntity.new {
                partyId = 1L
                name = "디저트"
                displayOrder = 3
            }
            CategoryEntity.new {
                partyId = 1L
                name = "메인"
                displayOrder = 1
            }
            CategoryEntity.new {
                partyId = 1L
                name = "사이드"
                displayOrder = 2
            }
        }

        // When
        val param = CategoryFetchParam(
            partyId = 1L
        )
        val categories = categoryRepository.fetch(param)

        // Then
        assertThat(categories).hasSize(3)
        assertThat(categories.map { it.displayOrder }).contains(1, 2, 3)
    }

    @Test
    fun `카테고리 이름 부분 일치 검색 테스트`() {
        // Given
        transaction {
            CategoryEntity.new {
                partyId = 1L
                name = "메인 요리"
                displayOrder = 1
            }
            CategoryEntity.new {
                partyId = 1L
                name = "메인 음료"
                displayOrder = 2
            }
            CategoryEntity.new {
                partyId = 1L
                name = "사이드"
                displayOrder = 3
            }
        }

        // When
        val param = CategoryFetchParam(
            name = "메인"
        )
        val categories = categoryRepository.fetch(param)

        // Then
        assertThat(categories).hasSize(2)
        assertThat(categories.all { it.name.contains("메인") }).isTrue()
    }

    @Test
    fun `displayOrder 변경 테스트`() {
        // Given
        val category = transaction {
            CategoryEntity.new {
                partyId = 1L
                name = "테스트 카테고리"
                displayOrder = 1
            }
        }

        // When
        transaction {
            category.displayOrder = 10
        }
        categoryRepository.save(category)

        // Then
        val param = CategoryFetchOneParam(
            id = category.id.value
        )
        val foundCategory = categoryRepository.fetchOne(param)

        assertThat(foundCategory?.displayOrder).isEqualTo(10)
    }
}