package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.OrderEntity
import com.wonjiyap.homeorder.enums.OrderStatus
import com.wonjiyap.homeorder.repository.dto.OrderFetchOneParam
import com.wonjiyap.homeorder.repository.dto.OrderFetchParam
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
class OrderRepositoryTest {

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @BeforeEach
    fun setUp() {
        orderRepository = OrderRepository()
    }

    @Test
    fun `주문 생성 및 저장 테스트`() {
        // Given & When
        val order = transaction {
            OrderEntity.new {
                partyId = 1L
                guestId = 100L
                status = OrderStatus.READY
            }
        }
        orderRepository.save(order)

        // Then
        val param = OrderFetchOneParam(
            id = order.id.value
        )
        val foundOrder = orderRepository.fetchOne(param)

        assertThat(foundOrder).isNotNull()
        assertThat(foundOrder?.partyId).isEqualTo(1L)
        assertThat(foundOrder?.guestId).isEqualTo(100L)
        assertThat(foundOrder?.status).isEqualTo(OrderStatus.READY)
    }

    @Test
    fun `파티 ID로 주문 목록 조회 테스트`() {
        // Given
        transaction {
            OrderEntity.new {
                partyId = 1L
                guestId = 100L
                status = OrderStatus.READY
            }
            OrderEntity.new {
                partyId = 1L
                guestId = 101L
                status = OrderStatus.COMPLETED
            }
            OrderEntity.new {
                partyId = 2L
                guestId = 102L
                status = OrderStatus.READY
            }
        }

        // When
        val param = OrderFetchParam(
            partyId = 1L
        )
        val orders = orderRepository.fetch(param)

        // Then
        assertThat(orders).hasSize(2)
        assertThat(orders.all { it.partyId == 1L }).isTrue()
    }

    @Test
    fun `게스트 ID로 주문 목록 조회 테스트`() {
        // Given
        transaction {
            OrderEntity.new {
                partyId = 1L
                guestId = 100L
                status = OrderStatus.READY
            }
            OrderEntity.new {
                partyId = 2L
                guestId = 100L
                status = OrderStatus.COMPLETED
            }
            OrderEntity.new {
                partyId = 1L
                guestId = 101L
                status = OrderStatus.READY
            }
        }

        // When
        val param = OrderFetchParam(
            guestId = 100L
        )
        val orders = orderRepository.fetch(param)

        // Then
        assertThat(orders).hasSize(2)
        assertThat(orders.all { it.guestId == 100L }).isTrue()
    }

    @Test
    fun `주문 상태로 목록 조회 테스트`() {
        // Given
        transaction {
            OrderEntity.new {
                partyId = 1L
                guestId = 100L
                status = OrderStatus.READY
            }
            OrderEntity.new {
                partyId = 1L
                guestId = 101L
                status = OrderStatus.COMPLETED
            }
            OrderEntity.new {
                partyId = 1L
                guestId = 102L
                status = OrderStatus.READY
            }
        }

        // When
        val param = OrderFetchParam(
            status = OrderStatus.READY
        )
        val orders = orderRepository.fetch(param)

        // Then
        assertThat(orders).hasSize(2)
        assertThat(orders.all { it.status == OrderStatus.READY }).isTrue()
    }

    @Test
    fun `파티 ID와 게스트 ID 조합으로 주문 목록 조회 테스트`() {
        // Given
        transaction {
            OrderEntity.new {
                partyId = 1L
                guestId = 100L
                status = OrderStatus.READY
            }
            OrderEntity.new {
                partyId = 1L
                guestId = 100L
                status = OrderStatus.COMPLETED
            }
            OrderEntity.new {
                partyId = 1L
                guestId = 101L
                status = OrderStatus.READY
            }
            OrderEntity.new {
                partyId = 2L
                guestId = 100L
                status = OrderStatus.READY
            }
        }

        // When
        val param = OrderFetchParam(
            partyId = 1L,
            guestId = 100L
        )
        val orders = orderRepository.fetch(param)

        // Then
        assertThat(orders).hasSize(2)
        assertThat(orders.all { it.partyId == 1L && it.guestId == 100L }).isTrue()
    }

    @Test
    fun `모든 조건으로 주문 목록 조회 테스트`() {
        // Given
        transaction {
            OrderEntity.new {
                partyId = 1L
                guestId = 100L
                status = OrderStatus.READY
            }
            OrderEntity.new {
                partyId = 1L
                guestId = 100L
                status = OrderStatus.COMPLETED
            }
            OrderEntity.new {
                partyId = 1L
                guestId = 101L
                status = OrderStatus.READY
            }
        }

        // When
        val param = OrderFetchParam(
            partyId = 1L,
            guestId = 100L,
            status = OrderStatus.READY
        )
        val orders = orderRepository.fetch(param)

        // Then
        assertThat(orders).hasSize(1)
        assertThat(orders.first().status).isEqualTo(OrderStatus.READY)
    }

    @Test
    fun `ID로 단일 주문 조회 테스트`() {
        // Given
        val order = transaction {
            OrderEntity.new {
                partyId = 1L
                guestId = 100L
                status = OrderStatus.READY
            }
        }

        // When
        val param = OrderFetchOneParam(
            id = order.id.value
        )
        val foundOrder = orderRepository.fetchOne(param)

        // Then
        assertThat(foundOrder).isNotNull()
        assertThat(foundOrder?.id?.value).isEqualTo(order.id.value)
        assertThat(foundOrder?.partyId).isEqualTo(1L)
        assertThat(foundOrder?.guestId).isEqualTo(100L)
    }

    @Test
    fun `파티 ID와 게스트 ID로 단일 주문 조회 테스트`() {
        // Given
        transaction {
            OrderEntity.new {
                partyId = 1L
                guestId = 100L
                status = OrderStatus.READY
            }
            OrderEntity.new {
                partyId = 1L
                guestId = 101L
                status = OrderStatus.READY
            }
        }

        // When
        val param = OrderFetchOneParam(
            partyId = 1L,
            guestId = 100L
        )
        val foundOrder = orderRepository.fetchOne(param)

        // Then
        assertThat(foundOrder).isNotNull()
        assertThat(foundOrder?.partyId).isEqualTo(1L)
        assertThat(foundOrder?.guestId).isEqualTo(100L)
    }

    @Test
    fun `존재하지 않는 주문 조회 시 null 반환 테스트`() {
        // When
        val param = OrderFetchOneParam(
            id = 9999L
        )
        val foundOrder = orderRepository.fetchOne(param)

        // Then
        assertThat(foundOrder).isNull()
    }

    @Test
    fun `주문 상태 변경 후 저장 테스트`() {
        // Given
        val order = transaction {
            OrderEntity.new {
                partyId = 1L
                guestId = 100L
                status = OrderStatus.READY
            }
        }

        // When
        transaction {
            order.status = OrderStatus.COMPLETED
        }
        orderRepository.save(order)

        // Then
        val param = OrderFetchOneParam(
            id = order.id.value
        )
        val foundOrder = orderRepository.fetchOne(param)

        assertThat(foundOrder?.status).isEqualTo(OrderStatus.COMPLETED)
    }

    @Test
    fun `주문 취소 테스트`() {
        // Given
        val order = transaction {
            OrderEntity.new {
                partyId = 1L
                guestId = 100L
                status = OrderStatus.READY
            }
        }

        // When
        transaction {
            order.status = OrderStatus.CANCELLED
        }
        orderRepository.save(order)

        // Then
        val param = OrderFetchOneParam(
            id = order.id.value,
            status = OrderStatus.CANCELLED
        )
        val foundOrder = orderRepository.fetchOne(param)

        assertThat(foundOrder).isNotNull()
        assertThat(foundOrder?.status).isEqualTo(OrderStatus.CANCELLED)
    }

    @Test
    fun `조건 없이 fetch 호출 시 모든 주문 조회 테스트`() {
        // Given
        transaction {
            OrderEntity.new {
                partyId = 1L
                guestId = 100L
                status = OrderStatus.READY
            }
            OrderEntity.new {
                partyId = 2L
                guestId = 101L
                status = OrderStatus.COMPLETED
            }
        }

        // When
        val param = OrderFetchParam()
        val orders = orderRepository.fetch(param)

        // Then
        assertThat(orders).hasSize(2)
    }

    @Test
    fun `특정 파티의 준비중 상태인 주문만 조회 테스트`() {
        // Given
        transaction {
            OrderEntity.new {
                partyId = 1L
                guestId = 100L
                status = OrderStatus.READY
            }
            OrderEntity.new {
                partyId = 1L
                guestId = 101L
                status = OrderStatus.COMPLETED
            }
            OrderEntity.new {
                partyId = 1L
                guestId = 102L
                status = OrderStatus.READY
            }
            OrderEntity.new {
                partyId = 2L
                guestId = 103L
                status = OrderStatus.READY
            }
        }

        // When
        val param = OrderFetchParam(
            partyId = 1L,
            status = OrderStatus.READY
        )
        val orders = orderRepository.fetch(param)

        // Then
        assertThat(orders).hasSize(2)
        assertThat(orders.all { it.partyId == 1L && it.status == OrderStatus.READY }).isTrue()
    }
}