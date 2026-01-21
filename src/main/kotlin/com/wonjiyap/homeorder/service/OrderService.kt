package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.OrderEntity
import com.wonjiyap.homeorder.domain.OrderItemEntity
import com.wonjiyap.homeorder.domain.OrderItemOptionEntity
import com.wonjiyap.homeorder.enums.OrderStatus
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.repository.OrderItemOptionRepository
import com.wonjiyap.homeorder.repository.OrderItemRepository
import com.wonjiyap.homeorder.repository.OrderRepository
import com.wonjiyap.homeorder.repository.PartyGuestRepository
import com.wonjiyap.homeorder.repository.PartyRepository
import com.wonjiyap.homeorder.repository.dto.OrderFetchOneParam
import com.wonjiyap.homeorder.repository.dto.OrderFetchParam
import com.wonjiyap.homeorder.repository.dto.OrderItemFetchParam
import com.wonjiyap.homeorder.repository.dto.OrderItemOptionFetchParam
import com.wonjiyap.homeorder.repository.dto.PartyFetchOneParam
import com.wonjiyap.homeorder.repository.dto.PartyGuestFetchOneParam
import com.wonjiyap.homeorder.service.dto.OrderCreateParam
import com.wonjiyap.homeorder.service.dto.OrderGetParam
import com.wonjiyap.homeorder.service.dto.OrderItemOptionResult
import com.wonjiyap.homeorder.service.dto.OrderItemResult
import com.wonjiyap.homeorder.service.dto.OrderListParam
import com.wonjiyap.homeorder.service.dto.OrderResult
import com.wonjiyap.homeorder.service.dto.OrderUpdateParam
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val orderItemOptionRepository: OrderItemOptionRepository,
    private val partyRepository: PartyRepository,
    private val partyGuestRepository: PartyGuestRepository,
) {

    fun list(param: OrderListParam): List<OrderResult> {
        validatePartyOwnership(param.partyId, param.hostId)

        val orders = orderRepository.fetch(
            OrderFetchParam(partyId = param.partyId)
        )
        return orders.map { toOrderResult(it) }
    }

    fun get(param: OrderGetParam): OrderResult {
        validatePartyOwnership(param.partyId, param.hostId)

        val order = orderRepository.fetchOne(
            OrderFetchOneParam(
                id = param.id,
                partyId = param.partyId,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다")

        return toOrderResult(order)
    }

    fun create(param: OrderCreateParam): OrderResult = transaction {
        validatePartyExists(param.partyId)
        validateGuestBelongsToParty(param.guestId, param.partyId)

        val order = OrderEntity.new {
            partyId = param.partyId
            guestId = param.guestId
            status = OrderStatus.READY
            orderedAt = Instant.now()
            updatedAt = Instant.now()
        }

        param.items.forEach { itemParam ->
            val orderItem = OrderItemEntity.new {
                orderId = order.id.value
                menuId = itemParam.menuId
                quantity = itemParam.quantity
                notes = itemParam.notes
                createdAt = Instant.now()
            }

            itemParam.optionIds.forEach { optionId ->
                OrderItemOptionEntity.new {
                    orderItemId = orderItem.id.value
                    this.optionId = optionId
                    createdAt = Instant.now()
                }
            }
        }

        toOrderResult(order)
    }

    fun update(param: OrderUpdateParam): OrderResult = transaction {
        validatePartyOwnership(param.partyId, param.hostId)

        val order = orderRepository.fetchOne(
            OrderFetchOneParam(
                id = param.id,
                partyId = param.partyId,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다")

        param.status?.let { order.status = it }
        order.updatedAt = Instant.now()

        toOrderResult(order)
    }

    private fun toOrderResult(order: OrderEntity): OrderResult {
        val orderItems = orderItemRepository.fetch(
            OrderItemFetchParam(orderId = order.id.value)
        )

        val itemResults = orderItems.map { item ->
            val options = orderItemOptionRepository.fetch(
                OrderItemOptionFetchParam(orderItemId = item.id.value)
            )
            OrderItemResult(
                id = item.id.value,
                menuId = item.menuId,
                quantity = item.quantity,
                notes = item.notes,
                options = options.map { option ->
                    OrderItemOptionResult(
                        id = option.id.value,
                        optionId = option.optionId,
                    )
                },
            )
        }

        return OrderResult(
            id = order.id.value,
            partyId = order.partyId,
            guestId = order.guestId,
            status = order.status,
            items = itemResults,
            orderedAt = order.orderedAt,
            updatedAt = order.updatedAt,
        )
    }

    private fun validatePartyExists(partyId: Long) {
        partyRepository.fetchOne(
            PartyFetchOneParam(
                id = partyId,
                deleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "파티를 찾을 수 없습니다")
    }

    private fun validatePartyOwnership(partyId: Long, hostId: Long) {
        partyRepository.fetchOne(
            PartyFetchOneParam(
                id = partyId,
                hostId = hostId,
                deleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.FORBIDDEN, "해당 파티에 대한 권한이 없습니다")
    }

    private fun validateGuestBelongsToParty(guestId: Long, partyId: Long) {
        partyGuestRepository.fetchOne(
            PartyGuestFetchOneParam(
                id = guestId,
                partyId = partyId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.FORBIDDEN, "해당 파티의 게스트가 아닙니다")
    }
}
