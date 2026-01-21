package com.wonjiyap.homeorder.service.dto

import com.wonjiyap.homeorder.enums.OrderStatus

/**
 * Order list param
 */
data class OrderListParam(
    val partyId: Long,
    val hostId: Long,
)

/**
 * Order get param
 */
data class OrderGetParam(
    val id: Long,
    val partyId: Long,
    val hostId: Long,
)

/**
 * Order create param
 */
data class OrderCreateParam(
    val partyId: Long,
    val guestId: Long,
    val items: List<OrderItemCreateParam>,
)

/**
 * Order item create param
 */
data class OrderItemCreateParam(
    val menuId: Long,
    val quantity: Int = 1,
    val notes: String? = null,
    val optionIds: List<Long> = emptyList(),
)

/**
 * Order update param
 */
data class OrderUpdateParam(
    val id: Long,
    val partyId: Long,
    val hostId: Long,
    val status: OrderStatus? = null,
)

/**
 * Order item option result
 */
data class OrderItemOptionResult(
    val id: Long,
    val optionId: Long,
)

/**
 * Order item result
 */
data class OrderItemResult(
    val id: Long,
    val menuId: Long,
    val quantity: Int,
    val notes: String?,
    val options: List<OrderItemOptionResult>,
)

/**
 * Order result
 */
data class OrderResult(
    val id: Long,
    val partyId: Long,
    val guestId: Long,
    val status: OrderStatus,
    val items: List<OrderItemResult>,
    val orderedAt: java.time.Instant,
    val updatedAt: java.time.Instant,
)

