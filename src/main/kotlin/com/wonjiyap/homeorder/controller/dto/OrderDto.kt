package com.wonjiyap.homeorder.controller.dto

import com.wonjiyap.homeorder.domain.OrderEntity
import com.wonjiyap.homeorder.enums.OrderStatus
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
 * Order response
 */
data class OrderResponse(
    val id: Long,
    val partyId: Long,
    val guestId: Long,
    val status: OrderStatus,
    val orderedAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(entity: OrderEntity): OrderResponse {
            return OrderResponse(
                id = entity.id.value,
                partyId = entity.partyId,
                guestId = entity.guestId,
                status = entity.status,
                orderedAt = entity.orderedAt,
                updatedAt = entity.updatedAt,
            )
        }
    }
}
