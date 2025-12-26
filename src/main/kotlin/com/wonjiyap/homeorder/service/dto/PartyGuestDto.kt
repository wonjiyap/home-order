package com.wonjiyap.homeorder.service.dto

data class PartyGuestJoinParam(
    val code: String,
    val nickname: String,
)

data class PartyGuestListParam(
    val partyId: Long,
    val hostId: Long,
)

data class PartyGuestGetParam(
    val id: Long,
    val partyId: Long,
    val hostId: Long,
)

data class PartyGuestUpdateParam(
    val id: Long,
    val partyId: Long,
    val hostId: Long,
    val nickname: String? = null,
    val isBlocked: Boolean? = null,
)

data class PartyGuestDeleteParam(
    val id: Long,
    val partyId: Long,
    val hostId: Long,
)

data class PartyGuestJoinResult(
    val guestId: Long,
    val partyId: Long,
    val partyName: String,
    val nickname: String,
)
