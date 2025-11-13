package com.wonjiyap.homeorder.repository.dto

/**
 * InviteCode fetch
 */
data class InviteCodeFetchParam(
    val partyId: Long? = null,
    val isActive: Boolean? = null,
    val isExpired: Boolean? = null,
    val withDeleted: Boolean = false,
)

/**
 * InviteCode fetch one
 */
data class InviteCodeFetchOneParam(
    val id: Long? = null,
    val partyId: Long? = null,
    val code: String? = null,
    val isActive: Boolean? = null,
    val isExpired: Boolean? = null,
    val withDeleted: Boolean = false,
)