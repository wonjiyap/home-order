package com.wonjiyap.homeorder.service.dto

/**
 * Category list param
 */
data class CategoryListParam(
    val partyId: Long,
    val hostId: Long,
)

/**
 * Category get param
 */
data class CategoryGetParam(
    val id: Long,
    val partyId: Long,
    val hostId: Long,
)

/**
 * Category create param
 */
data class CategoryCreateParam(
    val partyId: Long,
    val hostId: Long,
    val name: String,
)

/**
 * Category update param
 */
data class CategoryUpdateParam(
    val id: Long,
    val partyId: Long,
    val hostId: Long,
    val name: String? = null,
)

/**
 * Category reorder param
 */
data class CategoryReorderParam(
    val partyId: Long,
    val hostId: Long,
    val categoryIds: List<Long>,
)

/**
 * Category delete param
 */
data class CategoryDeleteParam(
    val id: Long,
    val partyId: Long,
    val hostId: Long,
)
