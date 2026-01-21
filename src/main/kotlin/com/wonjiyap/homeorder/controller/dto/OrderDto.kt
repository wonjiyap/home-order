package com.wonjiyap.homeorder.controller.dto

import com.wonjiyap.homeorder.enums.OrderStatus
import com.wonjiyap.homeorder.service.dto.OrderResult
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

/**
 * Order create request
 */
data class OrderCreateRequest(
    @field:NotNull(message = "게스트 ID는 필수입니다")
    val guestId: Long,
    @field:NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다")
    @field:Valid
    val items: List<OrderItemCreateRequest>,
)

/**
 * Order item create request
 */
data class OrderItemCreateRequest(
    @field:NotNull(message = "메뉴 ID는 필수입니다")
    val menuId: Long,
    @field:Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    val quantity: Int = 1,
    @field:Size(max = 100, message = "주문 메모는 100자 이하여야 합니다")
    val notes: String? = null,
    val optionIds: List<Long> = emptyList(),
)

/**
 * Order update request
 */
data class OrderUpdateRequest(
    val status: OrderStatus? = null,
)

/**
 * Order item option response
 */
data class OrderItemOptionResponse(
    val id: Long,
    val optionId: Long,
)

/**
 * Order item response
 */
data class OrderItemResponse(
    val id: Long,
    val menuId: Long,
    val quantity: Int,
    val notes: String?,
    val options: List<OrderItemOptionResponse>,
)

/**
 * Order response
 */
data class OrderResponse(
    val id: Long,
    val partyId: Long,
    val guestId: Long,
    val status: OrderStatus,
    val items: List<OrderItemResponse>,
    val orderedAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(result: OrderResult): OrderResponse {
            return OrderResponse(
                id = result.id,
                partyId = result.partyId,
                guestId = result.guestId,
                status = result.status,
                items = result.items.map { item ->
                    OrderItemResponse(
                        id = item.id,
                        menuId = item.menuId,
                        quantity = item.quantity,
                        notes = item.notes,
                        options = item.options.map { option ->
                            OrderItemOptionResponse(
                                id = option.id,
                                optionId = option.optionId,
                            )
                        },
                    )
                },
                orderedAt = result.orderedAt,
                updatedAt = result.updatedAt,
            )
        }
    }
}
