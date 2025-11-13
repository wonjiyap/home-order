package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.OrderItemOptionEntity
import com.wonjiyap.homeorder.repository.dto.OrderItemOptionFetchOneParam
import com.wonjiyap.homeorder.repository.dto.OrderItemOptionFetchParam
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@Rollback
class OrderItemOptionRepositoryTest {

    @Autowired
    private lateinit var orderItemOptionRepository: OrderItemOptionRepository

    @BeforeEach
    fun setUp() {
        orderItemOptionRepository = OrderItemOptionRepository()
    }

    @Test
    fun `주문 아이템 옵션 생성 및 저장 테스트`() {
        // Given & When
        val orderItemOption = transaction {
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 100L
            }
        }
        orderItemOptionRepository.save(orderItemOption)

        // Then
        val param = OrderItemOptionFetchOneParam(
            id = orderItemOption.id.value
        )
        val foundOption = orderItemOptionRepository.fetchOne(param)

        assertThat(foundOption).isNotNull()
        assertThat(foundOption?.orderItemId).isEqualTo(1L)
        assertThat(foundOption?.optionId).isEqualTo(100L)
        assertThat(foundOption?.createdAt).isNotNull()
    }

    @Test
    fun `주문 아이템 ID로 옵션 목록 조회 테스트`() {
        // Given
        transaction {
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 100L
            }
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 101L
            }
            OrderItemOptionEntity.new {
                orderItemId = 2L
                optionId = 100L
            }
        }

        // When
        val param = OrderItemOptionFetchParam(
            orderItemId = 1L
        )
        val options = orderItemOptionRepository.fetch(param)

        // Then
        assertThat(options).hasSize(2)
        assertThat(options.all { it.orderItemId == 1L }).isTrue()
    }

    @Test
    fun `옵션 ID로 주문 아이템 옵션 목록 조회 테스트`() {
        // Given
        transaction {
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 100L
            }
            OrderItemOptionEntity.new {
                orderItemId = 2L
                optionId = 100L
            }
            OrderItemOptionEntity.new {
                orderItemId = 3L
                optionId = 101L
            }
        }

        // When
        val param = OrderItemOptionFetchParam(
            optionId = 100L
        )
        val options = orderItemOptionRepository.fetch(param)

        // Then
        assertThat(options).hasSize(2)
        assertThat(options.all { it.optionId == 100L }).isTrue()
    }

    @Test
    fun `주문 아이템 ID와 옵션 ID 조합으로 목록 조회 테스트`() {
        // Given
        transaction {
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 100L
            }
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 101L
            }
            OrderItemOptionEntity.new {
                orderItemId = 2L
                optionId = 100L
            }
        }

        // When
        val param = OrderItemOptionFetchParam(
            orderItemId = 1L,
            optionId = 100L
        )
        val options = orderItemOptionRepository.fetch(param)

        // Then
        assertThat(options).hasSize(1)
        assertThat(options.first().orderItemId).isEqualTo(1L)
        assertThat(options.first().optionId).isEqualTo(100L)
    }

    @Test
    fun `ID로 단일 옵션 조회 테스트`() {
        // Given
        val orderItemOption = transaction {
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 100L
            }
        }

        // When
        val param = OrderItemOptionFetchOneParam(
            id = orderItemOption.id.value
        )
        val foundOption = orderItemOptionRepository.fetchOne(param)

        // Then
        assertThat(foundOption).isNotNull()
        assertThat(foundOption?.id?.value).isEqualTo(orderItemOption.id.value)
        assertThat(foundOption?.orderItemId).isEqualTo(1L)
        assertThat(foundOption?.optionId).isEqualTo(100L)
    }

    @Test
    fun `주문 아이템 ID와 옵션 ID로 단일 옵션 조회 테스트`() {
        // Given
        transaction {
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 100L
            }
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 101L
            }
        }

        // When
        val param = OrderItemOptionFetchOneParam(
            orderItemId = 1L,
            optionId = 101L
        )
        val foundOption = orderItemOptionRepository.fetchOne(param)

        // Then
        assertThat(foundOption).isNotNull()
        assertThat(foundOption?.orderItemId).isEqualTo(1L)
        assertThat(foundOption?.optionId).isEqualTo(101L)
    }

    @Test
    fun `존재하지 않는 옵션 조회 시 null 반환 테스트`() {
        // When
        val param = OrderItemOptionFetchOneParam(
            id = 9999L
        )
        val foundOption = orderItemOptionRepository.fetchOne(param)

        // Then
        assertThat(foundOption).isNull()
    }

    @Test
    fun `조건 없이 fetch 호출 시 모든 옵션 조회 테스트`() {
        // Given
        transaction {
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 100L
            }
            OrderItemOptionEntity.new {
                orderItemId = 2L
                optionId = 101L
            }
        }

        // When
        val param = OrderItemOptionFetchParam()
        val options = orderItemOptionRepository.fetch(param)

        // Then
        assertThat(options).hasSize(2)
    }

    @Test
    fun `특정 주문 아이템의 모든 옵션 조회 테스트`() {
        // Given
        transaction {
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 100L
            }
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 101L
            }
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 102L
            }
            OrderItemOptionEntity.new {
                orderItemId = 2L
                optionId = 100L
            }
        }

        // When
        val param = OrderItemOptionFetchParam(
            orderItemId = 1L
        )
        val options = orderItemOptionRepository.fetch(param)

        // Then
        assertThat(options).hasSize(3)
        assertThat(options.all { it.orderItemId == 1L }).isTrue()
    }

    @Test
    fun `특정 옵션이 사용된 모든 주문 아이템 조회 테스트`() {
        // Given
        transaction {
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 100L
            }
            OrderItemOptionEntity.new {
                orderItemId = 2L
                optionId = 100L
            }
            OrderItemOptionEntity.new {
                orderItemId = 3L
                optionId = 100L
            }
            OrderItemOptionEntity.new {
                orderItemId = 4L
                optionId = 101L
            }
        }

        // When
        val param = OrderItemOptionFetchParam(
            optionId = 100L
        )
        val options = orderItemOptionRepository.fetch(param)

        // Then
        assertThat(options).hasSize(3)
        assertThat(options.all { it.optionId == 100L }).isTrue()
    }

    @Test
    fun `동일한 주문 아이템에 여러 옵션 추가 테스트`() {
        // Given & When
        transaction {
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 100L
            }
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 101L
            }
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 102L
            }
        }

        // Then
        val param = OrderItemOptionFetchParam(
            orderItemId = 1L
        )
        val options = orderItemOptionRepository.fetch(param)

        assertThat(options).hasSize(3)
        assertThat(options.map { it.optionId }).containsExactlyInAnyOrder(100L, 101L, 102L)
    }

    @Test
    fun `동일한 옵션이 여러 주문 아이템에 사용되는 테스트`() {
        // Given & When
        transaction {
            OrderItemOptionEntity.new {
                orderItemId = 1L
                optionId = 100L
            }
            OrderItemOptionEntity.new {
                orderItemId = 2L
                optionId = 100L
            }
            OrderItemOptionEntity.new {
                orderItemId = 3L
                optionId = 100L
            }
        }

        // Then
        val param = OrderItemOptionFetchParam(
            optionId = 100L
        )
        val options = orderItemOptionRepository.fetch(param)

        assertThat(options).hasSize(3)
        assertThat(options.map { it.orderItemId }).containsExactlyInAnyOrder(1L, 2L, 3L)
    }
}