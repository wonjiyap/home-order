package com.wonjiyap.homeorder.service.dto

/**
 * Menu list param
 */
data class MenuListParam(
    val categoryId: Long,
    val hostId: Long,
)

/**
 * Menu get param
 */
data class MenuGetParam(
    val id: Long,
    val categoryId: Long,
    val hostId: Long,
)

/**
 * Menu create param
 */
data class MenuCreateParam(
    val categoryId: Long,
    val hostId: Long,
    val name: String,
    val description: String? = null,
    val isRecommended: Boolean = false,
    val isSoldOut: Boolean = false,
)

/**
 * Menu update param
 */
data class MenuUpdateParam(
    val id: Long,
    val categoryId: Long,
    val hostId: Long,
    val name: String? = null,
    val description: String? = null,
    val isRecommended: Boolean? = null,
    val isSoldOut: Boolean? = null,
)

/**
 * Menu delete param
 */
data class MenuDeleteParam(
    val id: Long,
    val categoryId: Long,
    val hostId: Long,
)

/**
 * Menu reorder param
 */
data class MenuReorderParam(
    val categoryId: Long,
    val hostId: Long,
    val menuIds: List<Long>,
)
