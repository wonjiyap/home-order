package com.wonjiyap.homeorder.service.dto

import java.time.Instant

data class InviteCodeListParam(
    val partyId: Long,
    val hostId: Long,
)

data class InviteCodeGetParam(
    val id: Long,
    val partyId: Long,
    val hostId: Long,
)

data class InviteCodeCreateParam(
    val partyId: Long,
    val hostId: Long,
    val expiresAt: Instant? = null,
)

data class InviteCodeUpdateParam(
    val id: Long,
    val partyId: Long,
    val hostId: Long,
    val isActive: Boolean? = null,
    val expiresAt: Instant? = null,
)

data class InviteCodeDeleteParam(
    val id: Long,
    val partyId: Long,
    val hostId: Long,
)
