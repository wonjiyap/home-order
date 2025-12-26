package com.wonjiyap.homeorder.controller.dto

import com.wonjiyap.homeorder.domain.InviteCodeEntity
import java.time.Instant

data class InviteCodeCreateRequest(
    val expiresAt: Instant? = null,
)

data class InviteCodeUpdateRequest(
    val isActive: Boolean? = null,
    val expiresAt: Instant? = null,
)

data class InviteCodeResponse(
    val id: Long,
    val partyId: Long,
    val code: String,
    val isActive: Boolean,
    val isExpired: Boolean,
    val isValid: Boolean,
    val createdAt: Instant,
    val expiresAt: Instant?,
) {
    companion object {
        fun from(entity: InviteCodeEntity): InviteCodeResponse = InviteCodeResponse(
            id = entity.id.value,
            partyId = entity.partyId,
            code = entity.code,
            isActive = entity.isActive,
            isExpired = entity.isExpired(),
            isValid = entity.isValid(),
            createdAt = entity.createdAt,
            expiresAt = entity.expiresAt,
        )
    }
}
