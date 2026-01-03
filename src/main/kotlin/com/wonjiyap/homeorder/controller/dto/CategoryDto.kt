package com.wonjiyap.homeorder.controller.dto

import com.wonjiyap.homeorder.domain.CategoryEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

/**
 * Category create request
 */
data class CategoryCreateRequest(
    @field:NotBlank(message = "카테고리 이름은 필수입니다")
    @field:Size(max = 20, message = "카테고리 이름은 20자 이하여야 합니다")
    val name: String,
)

/**
 * Category update request
 */
data class CategoryUpdateRequest(
    @field:Size(max = 20, message = "카테고리 이름은 20자 이하여야 합니다")
    val name: String? = null,
)

/**
 * Category reorder request
 */
data class CategoryReorderRequest(
    val categoryIds: List<Long>,
)

/**
 * Category response
 */
data class CategoryResponse(
    val id: Long,
    val partyId: Long,
    val name: String,
    val displayOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(entity: CategoryEntity): CategoryResponse {
            return CategoryResponse(
                id = entity.id.value,
                partyId = entity.partyId,
                name = entity.name,
                displayOrder = entity.displayOrder,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )
        }
    }
}
