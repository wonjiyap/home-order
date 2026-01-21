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

