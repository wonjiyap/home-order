package com.wonjiyap.homeorder.repository.dto

/**
 * Category fetch
 */
data class CategoryFetchParam(
    val partyId: Long? = null,
    val name: String? = null,
    val withDeleted: Boolean = false,
)

/**
 * Category fetch one
 */
data class CategoryFetchOneParam(
    val id: Long? = null,
    val partyId: Long? = null,
    val name: String? = null,
    val withDeleted: Boolean = false,
)
