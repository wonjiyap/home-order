package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.MenuEntity
import com.wonjiyap.homeorder.repository.dto.MenuFetchOneParam
import com.wonjiyap.homeorder.repository.dto.MenuFetchParam
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@Transactional
@Rollback
class MenuRepositoryTest {

    @Autowired
    private lateinit var menuRepository: MenuRepository

    @BeforeEach
    fun setUp() {
        menuRepository = MenuRepository()
    }

    @Test
    fun `같은 카테고리에 동일한 메뉴명이 두 개 존재할 수 없다`() {
        // Given & When & Then - unique constraint violation
        assertThrows<Exception> {
            transaction {
                MenuEntity.new {
                    categoryId = 1L
                    name = "Pizza"
                    description = "Delicious pizza"
                    isRecommended = true
                    isSoldOut = false
                    displayOrder = 1
                }

                MenuEntity.new {
                    categoryId = 1L
                    name = "Pizza"
                    description = "Another pizza"
                    isRecommended = false
                    isSoldOut = false
                    displayOrder = 2
                }

                flushCache()
            }
        }
    }

    @Test
    fun `다른 카테고리에는 동일한 메뉴명이 존재할 수 있다`() {
        // Given & When
        transaction {
            MenuEntity.new {
                categoryId = 1L
                name = "Pizza"
                description = "Pizza in category 1"
            }
            MenuEntity.new {
                categoryId = 2L
                name = "Pizza"
                description = "Pizza in category 2"
            }
        }

        // Then
        val param = MenuFetchParam(
            name = "Pizza",
            withDeleted = false
        )
        val menus = menuRepository.fetch(param)

        assertThat(menus).hasSize(2)
    }

    @Test
    fun `카테고리 ID로 메뉴 목록을 조회한다`() {
        // Given
        transaction {
            MenuEntity.new {
                categoryId = 1L
                name = "Pizza"
                description = "Delicious pizza"
            }
            MenuEntity.new {
                categoryId = 1L
                name = "Pasta"
                description = "Delicious pasta"
            }
            MenuEntity.new {
                categoryId = 2L
                name = "Burger"
                description = "Delicious burger"
            }
        }

        // When
        val param = MenuFetchParam(
            categoryId = 1L,
            withDeleted = false
        )
        val menus = menuRepository.fetch(param)

        // Then
        assertThat(menus).hasSize(2)
        assertThat(menus.map { it.name }).containsExactlyInAnyOrder("Pizza", "Pasta")
    }

    @Test
    fun `추천 메뉴만 조회한다`() {
        // Given
        transaction {
            MenuEntity.new {
                categoryId = 1L
                name = "Pizza"
                isRecommended = true
            }
            MenuEntity.new {
                categoryId = 1L
                name = "Pasta"
                isRecommended = false
            }
            MenuEntity.new {
                categoryId = 1L
                name = "Salad"
                isRecommended = true
            }
        }

        // When
        val param = MenuFetchParam(
            categoryId = 1L,
            isRecommended = true,
            withDeleted = false
        )
        val menus = menuRepository.fetch(param)

        // Then
        assertThat(menus).hasSize(2)
        assertThat(menus.map { it.name }).containsExactlyInAnyOrder("Pizza", "Salad")
        assertThat(menus).allMatch { it.isRecommended }
    }

    @Test
    fun `품절되지 않은 메뉴만 조회한다`() {
        // Given
        transaction {
            MenuEntity.new {
                categoryId = 1L
                name = "Pizza"
                isSoldOut = false
            }
            MenuEntity.new {
                categoryId = 1L
                name = "Pasta"
                isSoldOut = true
            }
            MenuEntity.new {
                categoryId = 1L
                name = "Salad"
                isSoldOut = false
            }
        }

        // When
        val param = MenuFetchParam(
            categoryId = 1L,
            isSoldOut = false,
            withDeleted = false
        )
        val menus = menuRepository.fetch(param)

        // Then
        assertThat(menus).hasSize(2)
        assertThat(menus.map { it.name }).containsExactlyInAnyOrder("Pizza", "Salad")
        assertThat(menus).noneMatch { it.isSoldOut }
    }

    @Test
    fun `품절된 메뉴만 조회한다`() {
        // Given
        transaction {
            MenuEntity.new {
                categoryId = 1L
                name = "Pizza"
                isSoldOut = true
            }
            MenuEntity.new {
                categoryId = 1L
                name = "Pasta"
                isSoldOut = false
            }
        }

        // When
        val param = MenuFetchParam(
            categoryId = 1L,
            isSoldOut = true,
            withDeleted = false
        )
        val menus = menuRepository.fetch(param)

        // Then
        assertThat(menus).hasSize(1)
        assertThat(menus.first().name).isEqualTo("Pizza")
        assertThat(menus.first().isSoldOut).isTrue()
    }

    @Test
    fun `메뉴명으로 메뉴를 조회한다`() {
        // Given
        transaction {
            MenuEntity.new {
                categoryId = 1L
                name = "Pizza"
                description = "Delicious pizza"
            }
        }

        // When
        val param = MenuFetchParam(
            name = "Pizza",
            withDeleted = false
        )
        val menus = menuRepository.fetch(param)

        // Then
        assertThat(menus).hasSize(1)
        assertThat(menus.first().name).isEqualTo("Pizza")
    }

    @Test
    fun `ID로 메뉴를 단건 조회한다`() {
        // Given
        val menuId = transaction {
            MenuEntity.new {
                categoryId = 1L
                name = "Pizza"
                description = "Delicious pizza"
            }.id.value
        }

        // When
        val param = MenuFetchOneParam(
            id = menuId,
            withDeleted = false
        )
        val menu = menuRepository.fetchOne(param)

        // Then
        assertThat(menu).isNotNull
        assertThat(menu?.name).isEqualTo("Pizza")
    }

    @Test
    fun `카테고리 ID와 메뉴명으로 메뉴를 단건 조회한다`() {
        // Given
        transaction {
            MenuEntity.new {
                categoryId = 1L
                name = "Pizza"
                description = "Pizza in category 1"
            }
            MenuEntity.new {
                categoryId = 2L
                name = "Pizza"
                description = "Pizza in category 2"
            }
        }

        // When
        val param = MenuFetchOneParam(
            categoryId = 1L,
            name = "Pizza",
            withDeleted = false
        )
        val menu = menuRepository.fetchOne(param)

        // Then
        assertThat(menu).isNotNull
        assertThat(menu?.categoryId).isEqualTo(1L)
        assertThat(menu?.name).isEqualTo("Pizza")
    }

    @Test
    fun `삭제된 메뉴는 기본적으로 조회되지 않는다`() {
        // Given
        transaction {
            MenuEntity.new {
                categoryId = 1L
                name = "Pizza"
                deletedAt = Instant.now()
            }
            MenuEntity.new {
                categoryId = 1L
                name = "Pasta"
                deletedAt = null
            }
        }

        // When
        val param = MenuFetchParam(
            categoryId = 1L,
            withDeleted = false
        )
        val menus = menuRepository.fetch(param)

        // Then
        assertThat(menus).hasSize(1)
        assertThat(menus.first().name).isEqualTo("Pasta")
    }

    @Test
    fun `withDeleted=true 시 삭제된 메뉴도 조회된다`() {
        // Given
        transaction {
            MenuEntity.new {
                categoryId = 1L
                name = "Pizza"
                deletedAt = Instant.now()
            }
            MenuEntity.new {
                categoryId = 1L
                name = "Pasta"
                deletedAt = null
            }
        }

        // When
        val param = MenuFetchParam(
            categoryId = 1L,
            withDeleted = true
        )
        val menus = menuRepository.fetch(param)

        // Then
        assertThat(menus).hasSize(2)
        assertThat(menus.map { it.name }).containsExactlyInAnyOrder("Pizza", "Pasta")
    }

    @Test
    fun `복합 조건으로 메뉴를 조회한다`() {
        // Given
        transaction {
            MenuEntity.new {
                categoryId = 1L
                name = "Pizza"
                isRecommended = true
                isSoldOut = false
            }
            MenuEntity.new {
                categoryId = 1L
                name = "Pasta"
                isRecommended = true
                isSoldOut = true
            }
            MenuEntity.new {
                categoryId = 1L
                name = "Salad"
                isRecommended = false
                isSoldOut = false
            }
        }

        // When
        val param = MenuFetchParam(
            categoryId = 1L,
            isRecommended = true,
            isSoldOut = false,
            withDeleted = false
        )
        val menus = menuRepository.fetch(param)

        // Then
        assertThat(menus).hasSize(1)
        assertThat(menus.first().name).isEqualTo("Pizza")
        assertThat(menus.first().isRecommended).isTrue()
        assertThat(menus.first().isSoldOut).isFalse()
    }
}