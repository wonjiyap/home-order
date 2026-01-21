package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.CategoryEntity
import com.wonjiyap.homeorder.domain.MenuEntity
import com.wonjiyap.homeorder.domain.OptionEntity
import com.wonjiyap.homeorder.domain.OptionGroupEntity
import com.wonjiyap.homeorder.domain.PartyEntity
import com.wonjiyap.homeorder.domain.PartyGuestEntity
import com.wonjiyap.homeorder.domain.UserEntity
import com.wonjiyap.homeorder.enums.OrderStatus
import com.wonjiyap.homeorder.enums.PartyStatus
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.repository.OrderItemOptionRepository
import com.wonjiyap.homeorder.repository.OrderItemRepository
import com.wonjiyap.homeorder.repository.dto.OrderItemFetchParam
import com.wonjiyap.homeorder.repository.dto.OrderItemOptionFetchParam
import com.wonjiyap.homeorder.service.dto.OrderCreateParam
import com.wonjiyap.homeorder.service.dto.OrderGetParam
import com.wonjiyap.homeorder.service.dto.OrderItemCreateParam
import com.wonjiyap.homeorder.service.dto.OrderListParam
import com.wonjiyap.homeorder.service.dto.OrderUpdateParam
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
class OrderServiceTest {

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var orderItemRepository: OrderItemRepository

    @Autowired
    private lateinit var orderItemOptionRepository: OrderItemOptionRepository

    private var testUserId: Long = 0
    private var testPartyId: Long = 0
    private var testGuestId: Long = 0
    private var testCategoryId: Long = 0
    private var testMenuId: Long = 0
    private var testOptionGroupId: Long = 0
    private var testOptionId: Long = 0

    @BeforeEach
    fun setUp() {
        testUserId = transaction {
            UserEntity.new {
                loginId = "ordertest_${System.nanoTime()}"
                password = "password123"
                nickname = "주문테스트유저"
            }.id.value
        }
        testPartyId = transaction {
            PartyEntity.new {
                hostId = testUserId
                name = "테스트 파티"
                status = PartyStatus.OPEN
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }
        testGuestId = transaction {
            PartyGuestEntity.new {
                partyId = testPartyId
                nickname = "테스트게스트"
                isBlocked = false
                joinedAt = Instant.now()
            }.id.value
        }
        testCategoryId = transaction {
            CategoryEntity.new {
                partyId = testPartyId
                name = "테스트 카테고리"
                displayOrder = 0
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }
        testMenuId = transaction {
            MenuEntity.new {
                categoryId = testCategoryId
                name = "테스트 메뉴"
                displayOrder = 0
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }
        testOptionGroupId = transaction {
            OptionGroupEntity.new {
                menuId = testMenuId
                name = "테스트 옵션그룹"
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }
        testOptionId = transaction {
            OptionEntity.new {
                optionGroupId = testOptionGroupId
                name = "테스트 옵션"
                displayOrder = 0
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }
    }

    @Test
    fun `주문 생성 테스트`() {
        // Given
        val param = OrderCreateParam(
            partyId = testPartyId,
            guestId = testGuestId,
            items = listOf(
                OrderItemCreateParam(
                    menuId = testMenuId,
                    quantity = 2,
                    notes = "맵지 않게 해주세요",
                )
            ),
        )

        // When
        val order = orderService.create(param)

        // Then
        assertThat(order.id).isGreaterThan(0)
        assertThat(order.partyId).isEqualTo(testPartyId)
        assertThat(order.guestId).isEqualTo(testGuestId)
        assertThat(order.status).isEqualTo(OrderStatus.READY)
    }

    @Test
    fun `주문 생성 시 OrderItem 생성 확인 테스트`() {
        // Given
        val param = OrderCreateParam(
            partyId = testPartyId,
            guestId = testGuestId,
            items = listOf(
                OrderItemCreateParam(
                    menuId = testMenuId,
                    quantity = 2,
                    notes = "맵지 않게 해주세요",
                )
            ),
        )

        // When
        val order = orderService.create(param)

        // Then
        val orderItems = orderItemRepository.fetch(
            OrderItemFetchParam(orderId = order.id)
        )
        assertThat(orderItems).hasSize(1)
        assertThat(orderItems[0].menuId).isEqualTo(testMenuId)
        assertThat(orderItems[0].quantity).isEqualTo(2)
        assertThat(orderItems[0].notes).isEqualTo("맵지 않게 해주세요")
    }

    @Test
    fun `주문 생성 시 OrderItemOption 생성 확인 테스트`() {
        // Given
        val param = OrderCreateParam(
            partyId = testPartyId,
            guestId = testGuestId,
            items = listOf(
                OrderItemCreateParam(
                    menuId = testMenuId,
                    quantity = 1,
                    optionIds = listOf(testOptionId),
                )
            ),
        )

        // When
        val order = orderService.create(param)

        // Then
        val orderItems = orderItemRepository.fetch(
            OrderItemFetchParam(orderId = order.id)
        )
        val orderItemOptions = orderItemOptionRepository.fetch(
            OrderItemOptionFetchParam(orderItemId = orderItems[0].id.value)
        )
        assertThat(orderItemOptions).hasSize(1)
        assertThat(orderItemOptions[0].optionId).isEqualTo(testOptionId)
    }

    @Test
    fun `존재하지 않는 파티에 주문 생성 시 예외 발생 테스트`() {
        // Given
        val param = OrderCreateParam(
            partyId = 999999L,
            guestId = testGuestId,
            items = listOf(
                OrderItemCreateParam(menuId = testMenuId),
            ),
        )

        // When & Then
        assertThatThrownBy { orderService.create(param) }
            .isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("파티를 찾을 수 없습니다")
    }

    @Test
    fun `파티의 게스트가 아닌 사용자가 주문 생성 시 예외 발생 테스트`() {
        // Given
        val param = OrderCreateParam(
            partyId = testPartyId,
            guestId = 999999L,
            items = listOf(
                OrderItemCreateParam(menuId = testMenuId),
            ),
        )

        // When & Then
        assertThatThrownBy { orderService.create(param) }
            .isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN)
            .hasMessageContaining("해당 파티의 게스트가 아닙니다")
    }

    @Test
    fun `주문 목록 조회 테스트`() {
        // Given
        orderService.create(
            OrderCreateParam(
                partyId = testPartyId,
                guestId = testGuestId,
                items = listOf(OrderItemCreateParam(menuId = testMenuId)),
            )
        )
        orderService.create(
            OrderCreateParam(
                partyId = testPartyId,
                guestId = testGuestId,
                items = listOf(OrderItemCreateParam(menuId = testMenuId)),
            )
        )

        // When
        val orders = orderService.list(
            OrderListParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(orders).hasSize(2)
    }

    @Test
    fun `다른 사용자 파티의 주문 목록 조회 시 예외 발생 테스트`() {
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
            orderService.list(
                OrderListParam(
                    partyId = testPartyId,
                    hostId = otherUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN)
    }

    @Test
    fun `주문 상세 조회 테스트`() {
        // Given
        val created = orderService.create(
            OrderCreateParam(
                partyId = testPartyId,
                guestId = testGuestId,
                items = listOf(OrderItemCreateParam(menuId = testMenuId)),
            )
        )

        // When
        val order = orderService.get(
            OrderGetParam(
                id = created.id,
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(order.id).isEqualTo(created.id)
        assertThat(order.partyId).isEqualTo(testPartyId)
        assertThat(order.guestId).isEqualTo(testGuestId)
    }

    @Test
    fun `존재하지 않는 주문 조회 시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            orderService.get(
                OrderGetParam(
                    id = 999999L,
                    partyId = testPartyId,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("주문을 찾을 수 없습니다")
    }

    @Test
    fun `주문 상태 수정 테스트`() {
        // Given
        val created = orderService.create(
            OrderCreateParam(
                partyId = testPartyId,
                guestId = testGuestId,
                items = listOf(OrderItemCreateParam(menuId = testMenuId)),
            )
        )

        // When
        val updated = orderService.update(
            OrderUpdateParam(
                id = created.id,
                partyId = testPartyId,
                hostId = testUserId,
                status = OrderStatus.COMPLETED,
            )
        )

        // Then
        assertThat(updated.status).isEqualTo(OrderStatus.COMPLETED)
    }

    @Test
    fun `존재하지 않는 주문 수정 시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            orderService.update(
                OrderUpdateParam(
                    id = 999999L,
                    partyId = testPartyId,
                    hostId = testUserId,
                    status = OrderStatus.COMPLETED,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

}
