package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.OrderItemEntity
import com.wonjiyap.homeorder.repository.dto.OrderItemFetchOneParam
import com.wonjiyap.homeorder.repository.dto.OrderItemFetchParam
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
class OrderItemRepositoryTest {

    @Autowired
    private lateinit var orderItemRepository: OrderItemRepository

    @BeforeEach
    fun setUp() {
        orderItemRepository = OrderItemRepository()
    }

    @Test
    fun `주문 아이템 생성 및 저장 테스트`() {
        // Given & When
        val orderItem = transaction {
            OrderItemEntity.new {
                orderId = 1L
                menuId = 100L
                quantity = 2
                notes = "매운맛 빼주세요"
            }
        }
        orderItemRepository.save(orderItem)

        // Then
        val param = OrderItemFetchOneParam(
            id = orderItem.id.value
        )
        val foundItem = orderItemRepository.fetchOne(param)

        assertThat(foundItem).isNotNull()
        assertThat(foundItem?.orderId).isEqualTo(1L)
        assertThat(foundItem?.menuId).isEqualTo(100L)
        assertThat(foundItem?.quantity).isEqualTo(2)
        assertThat(foundItem?.notes).isEqualTo("매운맛 빼주세요")
    }

    @Test
    fun `주문 ID로 아이템 목록 조회 테스트`() {
        // Given
        transaction {
            OrderItemEntity.new {
                orderId = 1L
                menuId = 100L
                quantity = 1
                notes = null
            }
            OrderItemEntity.new {
                orderId = 1L
                menuId = 101L
                quantity = 2
                notes = "얼음 많이"
            }
            OrderItemEntity.new {
                orderId = 2L
                menuId = 100L
                quantity = 1
                notes = null
            }
        }

        // When
        val param = OrderItemFetchParam(
            orderId = 1L
        )
        val items = orderItemRepository.fetch(param)

        // Then
        assertThat(items).hasSize(2)
        assertThat(items.all { it.orderId == 1L }).isTrue()
    }

    @Test
    fun `메뉴 ID로 아이템 목록 조회 테스트`() {
        // Given
        transaction {
            OrderItemEntity.new {
                orderId = 1L
                menuId = 100L
                quantity = 1
                notes = null
            }
            OrderItemEntity.new {
                orderId = 2L
                menuId = 100L
                quantity = 2
                notes = null
            }
            OrderItemEntity.new {
                orderId = 3L
                menuId = 101L
                quantity = 1
                notes = null
            }
        }

        // When
        val param = OrderItemFetchParam(
            menuId = 100L
        )
        val items = orderItemRepository.fetch(param)

        // Then
        assertThat(items).hasSize(2)
        assertThat(items.all { it.menuId == 100L }).isTrue()
    }

    @Test
    fun `주문 ID와 메뉴 ID 조합으로 아이템 목록 조회 테스트`() {
        // Given
        transaction {
            OrderItemEntity.new {
                orderId = 1L
                menuId = 100L
                quantity = 1
                notes = null
            }
            OrderItemEntity.new {
                orderId = 1L
                menuId = 101L
                quantity = 2
                notes = null
            }
            OrderItemEntity.new {
                orderId = 2L
                menuId = 100L
                quantity = 1
                notes = null
            }
        }

        // When
        val param = OrderItemFetchParam(
            orderId = 1L,
            menuId = 100L
        )
        val items = orderItemRepository.fetch(param)

        // Then
        assertThat(items).hasSize(1)
        assertThat(items.first().orderId).isEqualTo(1L)
        assertThat(items.first().menuId).isEqualTo(100L)
    }

    @Test
    fun `ID로 단일 아이템 조회 테스트`() {
        // Given
        val orderItem = transaction {
            OrderItemEntity.new {
                orderId = 1L
                menuId = 100L
                quantity = 3
                notes = "조리 안 함"
            }
        }

        // When
        val param = OrderItemFetchOneParam(
            id = orderItem.id.value
        )
        val foundItem = orderItemRepository.fetchOne(param)

        // Then
        assertThat(foundItem).isNotNull()
        assertThat(foundItem?.id?.value).isEqualTo(orderItem.id.value)
        assertThat(foundItem?.quantity).isEqualTo(3)
        assertThat(foundItem?.notes).isEqualTo("조리 안 함")
    }

    @Test
    fun `주문 ID와 메뉴 ID로 단일 아이템 조회 테스트`() {
        // Given
        transaction {
            OrderItemEntity.new {
                orderId = 1L
                menuId = 100L
                quantity = 1
                notes = null
            }
            OrderItemEntity.new {
                orderId = 1L
                menuId = 101L
                quantity = 2
                notes = null
            }
        }

        // When
        val param = OrderItemFetchOneParam(
            orderId = 1L,
            menuId = 101L
        )
        val foundItem = orderItemRepository.fetchOne(param)

        // Then
        assertThat(foundItem).isNotNull()
        assertThat(foundItem?.orderId).isEqualTo(1L)
        assertThat(foundItem?.menuId).isEqualTo(101L)
        assertThat(foundItem?.quantity).isEqualTo(2)
    }

    @Test
    fun `존재하지 않는 아이템 조회 시 null 반환 테스트`() {
        // When
        val param = OrderItemFetchOneParam(
            id = 9999L
        )
        val foundItem = orderItemRepository.fetchOne(param)

        // Then
        assertThat(foundItem).isNull()
    }

    @Test
    fun `아이템 수량 변경 후 저장 테스트`() {
        // Given
        val orderItem = transaction {
            OrderItemEntity.new {
                orderId = 1L
                menuId = 100L
                quantity = 1
                notes = null
            }
        }

        // When
        transaction {
            orderItem.quantity = 5
            orderItem.notes = "수량 변경됨"
        }
        orderItemRepository.save(orderItem)

        // Then
        val param = OrderItemFetchOneParam(
            id = orderItem.id.value
        )
        val foundItem = orderItemRepository.fetchOne(param)

        assertThat(foundItem?.quantity).isEqualTo(5)
        assertThat(foundItem?.notes).isEqualTo("수량 변경됨")
    }

    @Test
    fun `아이템 메모 추가 후 저장 테스트`() {
        // Given
        val orderItem = transaction {
            OrderItemEntity.new {
                orderId = 1L
                menuId = 100L
                quantity = 1
                notes = null
            }
        }

        // When
        transaction {
            orderItem.notes = "포장 부탁드립니다"
        }
        orderItemRepository.save(orderItem)

        // Then
        val param = OrderItemFetchOneParam(
            id = orderItem.id.value
        )
        val foundItem = orderItemRepository.fetchOne(param)

        assertThat(foundItem?.notes).isEqualTo("포장 부탁드립니다")
    }

    @Test
    fun `조건 없이 fetch 호출 시 모든 아이템 조회 테스트`() {
        // Given
        transaction {
            OrderItemEntity.new {
                orderId = 1L
                menuId = 100L
                quantity = 1
                notes = null
            }
            OrderItemEntity.new {
                orderId = 2L
                menuId = 101L
                quantity = 2
                notes = null
            }
        }

        // When
        val param = OrderItemFetchParam()
        val items = orderItemRepository.fetch(param)

        // Then
        assertThat(items).hasSize(2)
    }

    @Test
    fun `특정 주문의 모든 아이템 조회 테스트`() {
        // Given
        transaction {
            OrderItemEntity.new {
                orderId = 1L
                menuId = 100L
                quantity = 2
                notes = "매운맛"
            }
            OrderItemEntity.new {
                orderId = 1L
                menuId = 101L
                quantity = 1
                notes = "순한맛"
            }
            OrderItemEntity.new {
                orderId = 1L
                menuId = 102L
                quantity = 3
                notes = null
            }
            OrderItemEntity.new {
                orderId = 2L
                menuId = 100L
                quantity = 1
                notes = null
            }
        }

        // When
        val param = OrderItemFetchParam(
            orderId = 1L
        )
        val items = orderItemRepository.fetch(param)

        // Then
        assertThat(items).hasSize(3)
        assertThat(items.all { it.orderId == 1L }).isTrue()
    }

    @Test
    fun `특정 메뉴의 모든 주문 아이템 조회 테스트`() {
        // Given
        transaction {
            OrderItemEntity.new {
                orderId = 1L
                menuId = 100L
                quantity = 1
                notes = null
            }
            OrderItemEntity.new {
                orderId = 2L
                menuId = 100L
                quantity = 2
                notes = null
            }
            OrderItemEntity.new {
                orderId = 3L
                menuId = 100L
                quantity = 3
                notes = null
            }
            OrderItemEntity.new {
                orderId = 4L
                menuId = 101L
                quantity = 1
                notes = null
            }
        }

        // When
        val param = OrderItemFetchParam(
            menuId = 100L
        )
        val items = orderItemRepository.fetch(param)

        // Then
        assertThat(items).hasSize(3)
        assertThat(items.all { it.menuId == 100L }).isTrue()
    }
}