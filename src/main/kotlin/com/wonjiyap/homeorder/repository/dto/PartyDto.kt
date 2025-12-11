package com.wonjiyap.homeorder.repository.dto

import com.wonjiyap.homeorder.enums.PartyStatus
import java.time.Instant

/**
 * Party fetch
 */
data class PartyFetchParam(
    val hostId: Long? = null,
    val name: String? = null,
    val dateFrom: Instant? = null,
    val dateTo: Instant? = null,
    val status: PartyStatus? = null,
    val deleted: Boolean? = null,
)

/**
 * Party fetch one
 */
data class PartyFetchOneParam(
    val id: Long? = null,
    val hostId: Long? = null,
    val name: String? = null,
    val date: Instant? = null,
    val status: PartyStatus? = null,
    val statusNot: PartyStatus? = null,
    val excludeId: Long? = null,
    val deleted: Boolean? = null,
)