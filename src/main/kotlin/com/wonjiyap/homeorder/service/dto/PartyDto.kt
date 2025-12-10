package com.wonjiyap.homeorder.service.dto

import com.wonjiyap.homeorder.enums.PartyStatus
import java.time.Instant

data class ListPartyParam(
    val hostId: Long,
    val name: String? = null,
    val status: PartyStatus? = null,
)

data class GetPartyParam(
    val id: Long,
    val hostId: Long,
)

data class CreatePartyParam(
    val hostId: Long,
    val name: String,
    val description: String? = null,
    val date: Instant? = null,
    val location: String? = null,
)

data class UpdatePartyParam(
    val id: Long,
    val hostId: Long,
    val name: String? = null,
    val description: String? = null,
    val date: Instant? = null,
    val location: String? = null,
    val status: PartyStatus? = null,
)

data class DeletePartyParam(
    val id: Long,
    val hostId: Long,
)
