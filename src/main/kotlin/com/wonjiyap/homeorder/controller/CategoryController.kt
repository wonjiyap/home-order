package com.wonjiyap.homeorder.controller

import com.wonjiyap.homeorder.controller.dto.CategoryCreateRequest
import com.wonjiyap.homeorder.controller.dto.CategoryReorderRequest
import com.wonjiyap.homeorder.controller.dto.CategoryResponse
import com.wonjiyap.homeorder.controller.dto.CategoryUpdateRequest
import com.wonjiyap.homeorder.service.CategoryService
import com.wonjiyap.homeorder.service.dto.CategoryCreateParam
import com.wonjiyap.homeorder.service.dto.CategoryDeleteParam
import com.wonjiyap.homeorder.service.dto.CategoryGetParam
import com.wonjiyap.homeorder.service.dto.CategoryListParam
import com.wonjiyap.homeorder.service.dto.CategoryReorderParam
import com.wonjiyap.homeorder.service.dto.CategoryUpdateParam
import com.wonjiyap.homeorder.util.AuthContext
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Category", description = "카테고리 API")
@RestController
@RequestMapping("/api/parties/{partyId}/categories")
class CategoryController(
    private val categoryService: CategoryService,
    private val authContext: AuthContext,
) {

    @Operation(summary = "카테고리 목록 조회")
    @GetMapping
    fun list(
        @PathVariable partyId: Long,
    ): List<CategoryResponse> {
        val categories = categoryService.list(
            CategoryListParam(
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return categories.map { CategoryResponse.from(it) }
    }

    @Operation(summary = "카테고리 상세 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable partyId: Long,
        @PathVariable id: Long,
    ): CategoryResponse {
        val category = categoryService.get(
            CategoryGetParam(
                id = id,
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return CategoryResponse.from(category)
    }

    @Operation(summary = "카테고리 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable partyId: Long,
        @Valid @RequestBody request: CategoryCreateRequest,
    ): CategoryResponse {
        val category = categoryService.create(
            CategoryCreateParam(
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
                name = request.name,
            )
        )
        return CategoryResponse.from(category)
    }

    @Operation(summary = "카테고리 수정")
    @PatchMapping("/{id}")
    fun update(
        @PathVariable partyId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody request: CategoryUpdateRequest,
    ): CategoryResponse {
        val category = categoryService.update(
            CategoryUpdateParam(
                id = id,
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
                name = request.name,
            )
        )
        return CategoryResponse.from(category)
    }

    @Operation(summary = "카테고리 삭제")
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable partyId: Long,
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        categoryService.delete(
            CategoryDeleteParam(
                id = id,
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "카테고리 순서 변경")
    @PatchMapping("/reorder")
    fun reorder(
        @PathVariable partyId: Long,
        @Valid @RequestBody request: CategoryReorderRequest,
    ): List<CategoryResponse> {
        val categories = categoryService.reorder(
            CategoryReorderParam(
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
                categoryIds = request.categoryIds,
            )
        )
        return categories.map { CategoryResponse.from(it) }
    }
}
