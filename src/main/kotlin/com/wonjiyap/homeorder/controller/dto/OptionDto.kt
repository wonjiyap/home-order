package com.wonjiyap.homeorder.controller.dto

import com.wonjiyap.homeorder.domain.OptionEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class OptionCreateRequest(
    @field:NotBlank(message = "옵션 이름은 필수입니다")
    @field:Size(max = 20, message = "옵션 이름은 20자 이하여야 합니다")
    val name: String,
)

data class OptionUpdateRequest(
    @field:Size(max = 20, message = "옵션 이름은 20자 이하여야 합니다")
    val name: String? = null,
)

data class OptionReorderRequest(
    val optionIds: List<Long>,
)

data class OptionResponse(
    val id: Long,
    val optionGroupId: Long,
    val name: String,
    val displayOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(entity: OptionEntity): OptionResponse {
            return OptionResponse(
                id = entity.id.value,
                optionGroupId = entity.optionGroupId,
                name = entity.name,
                displayOrder = entity.displayOrder,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )
        }
    }
}
