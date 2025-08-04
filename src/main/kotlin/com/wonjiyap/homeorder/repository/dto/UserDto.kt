package com.wonjiyap.homeorder.repository.dto

/**
 * User fetch one
 */
data class UserFetchOneParam(
    val id: Long? = null,
    val loginId: String? = null,
    val deleted: Boolean? = null,
)