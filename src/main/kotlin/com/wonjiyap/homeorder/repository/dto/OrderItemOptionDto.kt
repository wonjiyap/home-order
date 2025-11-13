package com.wonjiyap.homeorder.repository.dto

/**
 * OrderItemOption fetch
 */
data class OrderItemOptionFetchParam(
    val orderItemId: Long? = null,
    val optionId: Long? = null,
)

/**
 * OrderItemOption fetch one
 */
data class OrderItemOptionFetchOneParam(
    val id: Long? = null,
    val orderItemId: Long? = null,
    val optionId: Long? = null,
)
