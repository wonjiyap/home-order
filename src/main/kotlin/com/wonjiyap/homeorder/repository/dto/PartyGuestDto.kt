package com.wonjiyap.homeorder.repository.dto

/**
 * PartyGuest fetch
 */
data class PartyGuestFetchParam(
    val partyId: Long? = null,
    val isBlocked: Boolean? = null,
    val withDeleted: Boolean = false,
)

/**
 * PartyGuest fetch one
 */
data class PartyGuestFetchOneParam(
    val id: Long? = null,
    val partyId: Long? = null,
    val nickname: String? = null,
    val isBlocked: Boolean? = null,
    val withDeleted: Boolean = false,
)