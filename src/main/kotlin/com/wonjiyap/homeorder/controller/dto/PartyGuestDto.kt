package com.wonjiyap.homeorder.controller.dto

import com.wonjiyap.homeorder.domain.PartyGuestEntity
import com.wonjiyap.homeorder.service.dto.PartyGuestJoinResult
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class PartyGuestJoinRequest(
    @field:NotBlank(message = "초대 코드를 입력해주세요")
    val code: String,

    @field:NotBlank(message = "닉네임을 입력해주세요")
    @field:Size(max = 20, message = "닉네임은 20자 이내로 입력해주세요")
    val nickname: String,
)

data class PartyGuestJoinResponse(
    val guestId: Long,
    val partyId: Long,
    val partyName: String,
    val nickname: String,
) {
    companion object {
        fun from(result: PartyGuestJoinResult): PartyGuestJoinResponse = PartyGuestJoinResponse(
            guestId = result.guestId,
            partyId = result.partyId,
            partyName = result.partyName,
            nickname = result.nickname,
        )
    }
}

data class PartyGuestUpdateRequest(
    @field:Size(max = 20, message = "닉네임은 20자 이내로 입력해주세요")
    val nickname: String? = null,

    val isBlocked: Boolean? = null,
)

data class PartyGuestResponse(
    val id: Long,
    val partyId: Long,
    val nickname: String,
    val isBlocked: Boolean,
    val joinedAt: Instant,
) {
    companion object {
        fun from(entity: PartyGuestEntity): PartyGuestResponse = PartyGuestResponse(
            id = entity.id.value,
            partyId = entity.partyId,
            nickname = entity.nickname,
            isBlocked = entity.isBlocked,
            joinedAt = entity.joinedAt,
        )
    }
}
