package com.wonjiyap.homeorder.repository.dto

import com.wonjiyap.homeorder.enums.OrderStatus

/**
 * Order fetch
 */
data class OrderFetchParam(
    val partyId: Long? = null,
    val guestId: Long? = null,
    val status: OrderStatus? = null,
)

/**
 * Order fetch one
 */
data class OrderFetchOneParam(
    val id: Long? = null,
    val partyId: Long? = null,
    val guestId: Long? = null,
    val status: OrderStatus? = null,
)