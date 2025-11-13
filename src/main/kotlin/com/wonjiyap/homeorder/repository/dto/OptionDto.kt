package com.wonjiyap.homeorder.repository.dto

/**
 * Option fetch
 */
data class OptionFetchParam(
    val optionGroupId: Long? = null,
    val name: String? = null,
    val withDeleted: Boolean = false,
)

/**
 * Option fetch one
 */
data class OptionFetchOneParam(
    val id: Long? = null,
    val optionGroupId: Long? = null,
    val name: String? = null,
    val withDeleted: Boolean = false,
)
