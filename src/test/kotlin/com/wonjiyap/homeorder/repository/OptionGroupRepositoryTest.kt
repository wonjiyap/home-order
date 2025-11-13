package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.OptionGroupEntity
import com.wonjiyap.homeorder.repository.dto.OptionGroupFetchOneParam
import com.wonjiyap.homeorder.repository.dto.OptionGroupFetchParam
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
class OptionGroupRepositoryTest {

    @Autowired
    private lateinit var optionGroupRepository: OptionGroupRepository

    @BeforeEach
    fun setUp() {
        optionGroupRepository = OptionGroupRepository()
    }

    @Test
    fun `같은 메뉴에 동일한 옵션그룹명이 두 개 존재할 수 없다`() {
        // Given & When & Then - unique constraint violation
        assertThrows<Exception> {
            transaction {
                OptionGroupEntity.new {
                    menuId = 1L
                    name = "Size"
                    isRequired = true
                }

                OptionGroupEntity.new {
                    menuId = 1L
                    name = "Size"
                    isRequired = false
                }

                flushCache()
            }
        }
    }

    @Test
    fun `다른 메뉴에는 동일한 옵션그룹명이 존재할 수 있다`() {
        // Given & When
        transaction {
            OptionGroupEntity.new {
                menuId = 1L
                name = "Size"
                isRequired = true
            }
            OptionGroupEntity.new {
                menuId = 2L
                name = "Size"
                isRequired = false
            }
        }

        // Then
        val param = OptionGroupFetchParam(
            name = "Size",
            withDeleted = false
        )
        val optionGroups = optionGroupRepository.fetch(param)

        assertThat(optionGroups).hasSize(2)
    }

    @Test
    fun `메뉴 ID로 옵션그룹 목록을 조회한다`() {
        // Given
        transaction {
            OptionGroupEntity.new {
                menuId = 1L
                name = "Size"
                isRequired = true
            }
            OptionGroupEntity.new {
                menuId = 1L
                name = "Topping"
                isRequired = false
            }
            OptionGroupEntity.new {
                menuId = 2L
                name = "Temperature"
                isRequired = true
            }
        }

        // When
        val param = OptionGroupFetchParam(
            menuId = 1L,
            withDeleted = false
        )
        val optionGroups = optionGroupRepository.fetch(param)

        // Then
        assertThat(optionGroups).hasSize(2)
        assertThat(optionGroups.map { it.name }).containsExactlyInAnyOrder("Size", "Topping")
    }

    @Test
    fun `필수 옵션그룹만 조회한다`() {
        // Given
        transaction {
            OptionGroupEntity.new {
                menuId = 1L
                name = "Size"
                isRequired = true
            }
            OptionGroupEntity.new {
                menuId = 1L
                name = "Topping"
                isRequired = false
            }
            OptionGroupEntity.new {
                menuId = 1L
                name = "Temperature"
                isRequired = true
            }
        }

        // When
        val param = OptionGroupFetchParam(
            menuId = 1L,
            isRequired = true,
            withDeleted = false
        )
        val optionGroups = optionGroupRepository.fetch(param)

        // Then
        assertThat(optionGroups).hasSize(2)
        assertThat(optionGroups.map { it.name }).containsExactlyInAnyOrder("Size", "Temperature")
        assertThat(optionGroups).allMatch { it.isRequired }
    }

    @Test
    fun `선택 옵션그룹만 조회한다`() {
        // Given
        transaction {
            OptionGroupEntity.new {
                menuId = 1L
                name = "Size"
                isRequired = true
            }
            OptionGroupEntity.new {
                menuId = 1L
                name = "Topping"
                isRequired = false
            }
        }

        // When
        val param = OptionGroupFetchParam(
            menuId = 1L,
            isRequired = false,
            withDeleted = false
        )
        val optionGroups = optionGroupRepository.fetch(param)

        // Then
        assertThat(optionGroups).hasSize(1)
        assertThat(optionGroups.first().name).isEqualTo("Topping")
        assertThat(optionGroups.first().isRequired).isFalse()
    }

    @Test
    fun `옵션그룹명으로 옵션그룹을 조회한다`() {
        // Given
        transaction {
            OptionGroupEntity.new {
                menuId = 1L
                name = "Size"
                isRequired = true
            }
        }

        // When
        val param = OptionGroupFetchParam(
            name = "Size",
            withDeleted = false
        )
        val optionGroups = optionGroupRepository.fetch(param)

        // Then
        assertThat(optionGroups).hasSize(1)
        assertThat(optionGroups.first().name).isEqualTo("Size")
    }

    @Test
    fun `ID로 옵션그룹을 단건 조회한다`() {
        // Given
        val optionGroupId = transaction {
            OptionGroupEntity.new {
                menuId = 1L
                name = "Size"
                isRequired = true
            }.id.value
        }

        // When
        val param = OptionGroupFetchOneParam(
            id = optionGroupId,
            withDeleted = false
        )
        val optionGroup = optionGroupRepository.fetchOne(param)

        // Then
        assertThat(optionGroup).isNotNull
        assertThat(optionGroup?.name).isEqualTo("Size")
    }

    @Test
    fun `메뉴 ID와 옵션그룹명으로 옵션그룹을 단건 조회한다`() {
        // Given
        transaction {
            OptionGroupEntity.new {
                menuId = 1L
                name = "Size"
                isRequired = true
            }
            OptionGroupEntity.new {
                menuId = 2L
                name = "Size"
                isRequired = false
            }
        }

        // When
        val param = OptionGroupFetchOneParam(
            menuId = 1L,
            name = "Size",
            withDeleted = false
        )
        val optionGroup = optionGroupRepository.fetchOne(param)

        // Then
        assertThat(optionGroup).isNotNull
        assertThat(optionGroup?.menuId).isEqualTo(1L)
        assertThat(optionGroup?.name).isEqualTo("Size")
    }

    @Test
    fun `삭제된 옵션그룹은 기본적으로 조회되지 않는다`() {
        // Given
        transaction {
            OptionGroupEntity.new {
                menuId = 1L
                name = "Size"
                deletedAt = Instant.now()
            }
            OptionGroupEntity.new {
                menuId = 1L
                name = "Topping"
                deletedAt = null
            }
        }

        // When
        val param = OptionGroupFetchParam(
            menuId = 1L,
            withDeleted = false
        )
        val optionGroups = optionGroupRepository.fetch(param)

        // Then
        assertThat(optionGroups).hasSize(1)
        assertThat(optionGroups.first().name).isEqualTo("Topping")
    }

    @Test
    fun `삭제된 객체 포함 조회시 삭제된 옵션그룹도 조회된다`() {
        // Given
        transaction {
            OptionGroupEntity.new {
                menuId = 1L
                name = "Size"
                deletedAt = Instant.now()
            }
            OptionGroupEntity.new {
                menuId = 1L
                name = "Topping"
                deletedAt = null
            }
        }

        // When
        val param = OptionGroupFetchParam(
            menuId = 1L,
            withDeleted = true
        )
        val optionGroups = optionGroupRepository.fetch(param)

        // Then
        assertThat(optionGroups).hasSize(2)
        assertThat(optionGroups.map { it.name }).containsExactlyInAnyOrder("Size", "Topping")
    }

    @Test
    fun `복합 조건으로 옵션그룹을 조회한다`() {
        // Given
        transaction {
            OptionGroupEntity.new {
                menuId = 1L
                name = "Size"
                isRequired = true
            }
            OptionGroupEntity.new {
                menuId = 1L
                name = "Topping"
                isRequired = false
            }
            OptionGroupEntity.new {
                menuId = 2L
                name = "Temperature"
                isRequired = true
            }
        }

        // When
        val param = OptionGroupFetchParam(
            menuId = 1L,
            isRequired = true,
            withDeleted = false
        )
        val optionGroups = optionGroupRepository.fetch(param)

        // Then
        assertThat(optionGroups).hasSize(1)
        assertThat(optionGroups.first().name).isEqualTo("Size")
        assertThat(optionGroups.first().menuId).isEqualTo(1L)
        assertThat(optionGroups.first().isRequired).isTrue()
    }
}