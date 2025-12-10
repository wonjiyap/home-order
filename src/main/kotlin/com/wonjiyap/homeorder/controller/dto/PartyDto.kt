package com.wonjiyap.homeorder.controller.dto

import com.wonjiyap.homeorder.domain.PartyEntity
import com.wonjiyap.homeorder.enums.PartyStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class CreatePartyRequest(
    @field:NotBlank(message = "파티 이름을 입력해주세요")
    @field:Size(max = 255, message = "파티 이름은 255자 이내로 입력해주세요")
    val name: String,

    @field:Size(max = 1000, message = "설명은 1000자 이내로 입력해주세요")
    val description: String? = null,

    val date: Instant? = null,

    @field:Size(max = 255, message = "장소는 255자 이내로 입력해주세요")
    val location: String? = null,
)

data class UpdatePartyRequest(
    @field:Size(max = 255, message = "파티 이름은 255자 이내로 입력해주세요")
    val name: String? = null,

    @field:Size(max = 1000, message = "설명은 1000자 이내로 입력해주세요")
    val description: String? = null,

    val date: Instant? = null,

    @field:Size(max = 255, message = "장소는 255자 이내로 입력해주세요")
    val location: String? = null,

    val status: PartyStatus? = null,
)

data class PartyResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val date: Instant?,
    val location: String?,
    val status: PartyStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(entity: PartyEntity): PartyResponse = PartyResponse(
            id = entity.id.value,
            name = entity.name,
            description = entity.description,
            date = entity.date,
            location = entity.location,
            status = entity.status,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
    }
}
