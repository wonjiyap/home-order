package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.OptionEntity
import com.wonjiyap.homeorder.repository.dto.OptionFetchOneParam
import com.wonjiyap.homeorder.repository.dto.OptionFetchParam
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
class OptionRepositoryTest {

    @Autowired
    private lateinit var optionRepository: OptionRepository

    @BeforeEach
    fun setUp() {
        optionRepository = OptionRepository()
    }

    @Test
    fun `같은 옵션그룹에 동일한 옵션명이 두 개 존재할 수 없다`() {
        // Given & When & Then - unique constraint violation
        assertThrows<Exception> {
            transaction {
                OptionEntity.new {
                    optionGroupId = 1L
                    name = "Large"
                    displayOrder = 1
                }

                OptionEntity.new {
                    optionGroupId = 1L
                    name = "Large"
                    displayOrder = 2
                }

                flushCache()
            }
        }
    }

    @Test
    fun `다른 옵션그룹에는 동일한 옵션명이 존재할 수 있다`() {
        // Given & When
        transaction {
            OptionEntity.new {
                optionGroupId = 1L
                name = "Large"
                displayOrder = 1
            }
            OptionEntity.new {
                optionGroupId = 2L
                name = "Large"
                displayOrder = 1
            }
        }

        // Then
        val param = OptionFetchParam(
            name = "Large",
            withDeleted = false
        )
        val options = optionRepository.fetch(param)

        assertThat(options).hasSize(2)
    }

    @Test
    fun `옵션그룹 ID로 옵션 목록을 조회한다`() {
        // Given
        transaction {
            OptionEntity.new {
                optionGroupId = 1L
                name = "Small"
                displayOrder = 1
            }
            OptionEntity.new {
                optionGroupId = 1L
                name = "Medium"
                displayOrder = 2
            }
            OptionEntity.new {
                optionGroupId = 1L
                name = "Large"
                displayOrder = 3
            }
            OptionEntity.new {
                optionGroupId = 2L
                name = "Hot"
                displayOrder = 1
            }
        }

        // When
        val param = OptionFetchParam(
            optionGroupId = 1L,
            withDeleted = false
        )
        val options = optionRepository.fetch(param)

        // Then
        assertThat(options).hasSize(3)
        assertThat(options.map { it.name }).containsExactlyInAnyOrder("Small", "Medium", "Large")
    }

    @Test
    fun `옵션명으로 옵션을 조회한다`() {
        // Given
        transaction {
            OptionEntity.new {
                optionGroupId = 1L
                name = "Large"
                displayOrder = 1
            }
        }

        // When
        val param = OptionFetchParam(
            name = "Large",
            withDeleted = false
        )
        val options = optionRepository.fetch(param)

        // Then
        assertThat(options).hasSize(1)
        assertThat(options.first().name).isEqualTo("Large")
    }

    @Test
    fun `ID로 옵션을 단건 조회한다`() {
        // Given
        val optionId = transaction {
            OptionEntity.new {
                optionGroupId = 1L
                name = "Large"
                displayOrder = 1
            }.id.value
        }

        // When
        val param = OptionFetchOneParam(
            id = optionId,
            withDeleted = false
        )
        val option = optionRepository.fetchOne(param)

        // Then
        assertThat(option).isNotNull
        assertThat(option?.name).isEqualTo("Large")
    }

    @Test
    fun `옵션그룹 ID와 옵션명으로 옵션을 단건 조회한다`() {
        // Given
        transaction {
            OptionEntity.new {
                optionGroupId = 1L
                name = "Large"
                displayOrder = 1
            }
            OptionEntity.new {
                optionGroupId = 2L
                name = "Large"
                displayOrder = 1
            }
        }

        // When
        val param = OptionFetchOneParam(
            optionGroupId = 1L,
            name = "Large",
            withDeleted = false
        )
        val option = optionRepository.fetchOne(param)

        // Then
        assertThat(option).isNotNull
        assertThat(option?.optionGroupId).isEqualTo(1L)
        assertThat(option?.name).isEqualTo("Large")
    }

    @Test
    fun `삭제된 옵션은 기본적으로 조회되지 않는다`() {
        // Given
        transaction {
            OptionEntity.new {
                optionGroupId = 1L
                name = "Small"
                deletedAt = Instant.now()
            }
            OptionEntity.new {
                optionGroupId = 1L
                name = "Large"
                deletedAt = null
            }
        }

        // When
        val param = OptionFetchParam(
            optionGroupId = 1L,
            withDeleted = false
        )
        val options = optionRepository.fetch(param)

        // Then
        assertThat(options).hasSize(1)
        assertThat(options.first().name).isEqualTo("Large")
    }

    @Test
    fun `삭제된 객체 포함 조회시 삭제된 옵션도 조회된다`() {
        // Given
        transaction {
            OptionEntity.new {
                optionGroupId = 1L
                name = "Small"
                deletedAt = Instant.now()
            }
            OptionEntity.new {
                optionGroupId = 1L
                name = "Large"
                deletedAt = null
            }
        }

        // When
        val param = OptionFetchParam(
            optionGroupId = 1L,
            withDeleted = true
        )
        val options = optionRepository.fetch(param)

        // Then
        assertThat(options).hasSize(2)
        assertThat(options.map { it.name }).containsExactlyInAnyOrder("Small", "Large")
    }

    @Test
    fun `복합 조건으로 옵션을 조회한다`() {
        // Given
        transaction {
            OptionEntity.new {
                optionGroupId = 1L
                name = "Small"
                displayOrder = 1
            }
            OptionEntity.new {
                optionGroupId = 1L
                name = "Large"
                displayOrder = 2
            }
            OptionEntity.new {
                optionGroupId = 2L
                name = "Hot"
                displayOrder = 1
            }
        }

        // When
        val param = OptionFetchParam(
            optionGroupId = 1L,
            name = "Large",
            withDeleted = false
        )
        val options = optionRepository.fetch(param)

        // Then
        assertThat(options).hasSize(1)
        assertThat(options.first().name).isEqualTo("Large")
        assertThat(options.first().optionGroupId).isEqualTo(1L)
    }

    @Test
    fun `displayOrder 순서대로 옵션을 조회한다`() {
        // Given
        transaction {
            OptionEntity.new {
                optionGroupId = 1L
                name = "Large"
                displayOrder = 3
            }
            OptionEntity.new {
                optionGroupId = 1L
                name = "Small"
                displayOrder = 1
            }
            OptionEntity.new {
                optionGroupId = 1L
                name = "Medium"
                displayOrder = 2
            }
        }

        // When
        val param = OptionFetchParam(
            optionGroupId = 1L,
            withDeleted = false
        )
        val options = optionRepository.fetch(param).sortedBy { it.displayOrder }

        // Then
        assertThat(options).hasSize(3)
        assertThat(options.map { it.name }).containsExactly("Small", "Medium", "Large")
    }
}