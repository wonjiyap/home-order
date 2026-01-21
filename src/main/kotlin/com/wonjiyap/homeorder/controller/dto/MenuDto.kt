package com.wonjiyap.homeorder.controller.dto

import com.wonjiyap.homeorder.domain.MenuEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import java.time.Instant

/**
 * Menu create request
 */
data class MenuCreateRequest(
    @field:NotBlank(message = "메뉴 이름은 필수입니다")
    @field:Size(max = 20, message = "메뉴 이름은 20자 이하여야 합니다")
    val name: String,
    @field:Size(max = 200, message = "메뉴 설명은 200자 이하여야 합니다")
    val description: String? = null,
    val isRecommended: Boolean = false,
    val isSoldOut: Boolean = false,
)

/**
 * Menu update request
 */
data class MenuUpdateRequest(
    @field:Size(max = 20, message = "메뉴 이름은 20자 이하여야 합니다")
    val name: String? = null,
    @field:Size(max = 200, message = "메뉴 설명은 200자 이하여야 합니다")
    val description: String? = null,
    val isRecommended: Boolean? = null,
    val isSoldOut: Boolean? = null,
)

/**
 * Menu reorder request
 */
data class MenuReorderRequest(
    @field:NotEmpty(message = "메뉴 ID 목록은 필수입니다")
    val menuIds: List<Long>,
)

/**
 * Menu response
 */
data class MenuResponse(
    val id: Long,
    val categoryId: Long,
    val name: String,
    val description: String?,
    val isRecommended: Boolean,
    val isSoldOut: Boolean,
    val displayOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(entity: MenuEntity): MenuResponse {
            return MenuResponse(
                id = entity.id.value,
                categoryId = entity.categoryId,
                name = entity.name,
                description = entity.description,
                isRecommended = entity.isRecommended,
                isSoldOut = entity.isSoldOut,
                displayOrder = entity.displayOrder,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )
        }
    }
}
