package com.wonjiyap.homeorder.repository.dto

/**
 * OptionGroup fetch
 */
data class OptionGroupFetchParam(
    val menuId: Long? = null,
    val name: String? = null,
    val isRequired: Boolean? = null,
    val withDeleted: Boolean = false,
)

/**
 * OptionGroup fetch one
 */
data class OptionGroupFetchOneParam(
    val id: Long? = null,
    val menuId: Long? = null,
    val name: String? = null,
    val withDeleted: Boolean = false,
)