package com.wonjiyap.homeorder.controller.dto

import com.wonjiyap.homeorder.domain.OptionGroupEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class OptionGroupCreateRequest(
    @field:NotBlank(message = "옵션 그룹 이름은 필수입니다")
    @field:Size(max = 20, message = "옵션 그룹 이름은 20자 이하여야 합니다")
    val name: String,
    val isRequired: Boolean = false,
)

data class OptionGroupUpdateRequest(
    @field:Size(max = 20, message = "옵션 그룹 이름은 20자 이하여야 합니다")
    val name: String? = null,
    val isRequired: Boolean? = null,
)

data class OptionGroupResponse(
    val id: Long,
    val menuId: Long,
    val name: String,
    val isRequired: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(entity: OptionGroupEntity): OptionGroupResponse {
            return OptionGroupResponse(
                id = entity.id.value,
                menuId = entity.menuId,
                name = entity.name,
                isRequired = entity.isRequired,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )
        }
    }
}
