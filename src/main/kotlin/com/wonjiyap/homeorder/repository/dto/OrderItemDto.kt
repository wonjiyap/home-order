package com.wonjiyap.homeorder.repository.dto

/**
 * OrderItem fetch
 */
data class OrderItemFetchParam(
    val orderId: Long? = null,
    val menuId: Long? = null,
)

/**
 * OrderItem fetch one
 */
data class OrderItemFetchOneParam(
    val id: Long? = null,
    val orderId: Long? = null,
    val menuId: Long? = null,
)