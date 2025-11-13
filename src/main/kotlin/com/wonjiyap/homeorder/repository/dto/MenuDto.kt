package com.wonjiyap.homeorder.repository.dto

/**
 * Menu fetch
 */
data class MenuFetchParam(
    val categoryId: Long? = null,
    val name: String? = null,
    val isRecommended: Boolean? = null,
    val isSoldOut: Boolean? = null,
    val withDeleted: Boolean = false,
)

/**
 * Menu fetch one
 */
data class MenuFetchOneParam(
    val id: Long? = null,
    val categoryId: Long? = null,
    val name: String? = null,
    val withDeleted: Boolean = false,
)