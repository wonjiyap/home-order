package com.wonjiyap.homeorder.controller

import com.wonjiyap.homeorder.controller.dto.OptionGroupCreateRequest
import com.wonjiyap.homeorder.controller.dto.OptionGroupResponse
import com.wonjiyap.homeorder.controller.dto.OptionGroupUpdateRequest
import com.wonjiyap.homeorder.service.OptionGroupService
import com.wonjiyap.homeorder.service.dto.OptionGroupCreateParam
import com.wonjiyap.homeorder.service.dto.OptionGroupDeleteParam
import com.wonjiyap.homeorder.service.dto.OptionGroupGetParam
import com.wonjiyap.homeorder.service.dto.OptionGroupListParam
import com.wonjiyap.homeorder.service.dto.OptionGroupUpdateParam
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

@Tag(name = "OptionGroup", description = "옵션 그룹 API")
@RestController
@RequestMapping("/api/menus/{menuId}/option-groups")
class OptionGroupController(
    private val optionGroupService: OptionGroupService,
    private val authContext: AuthContext,
) {

    @Operation(summary = "옵션 그룹 목록 조회")
    @GetMapping
    fun list(
        @PathVariable menuId: Long,
    ): List<OptionGroupResponse> {
        val optionGroups = optionGroupService.list(
            OptionGroupListParam(
                menuId = menuId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return optionGroups.map { OptionGroupResponse.from(it) }
    }

    @Operation(summary = "옵션 그룹 상세 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable menuId: Long,
        @PathVariable id: Long,
    ): OptionGroupResponse {
        val optionGroup = optionGroupService.get(
            OptionGroupGetParam(
                id = id,
                menuId = menuId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return OptionGroupResponse.from(optionGroup)
    }

    @Operation(summary = "옵션 그룹 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable menuId: Long,
        @Valid @RequestBody request: OptionGroupCreateRequest,
    ): OptionGroupResponse {
        val optionGroup = optionGroupService.create(
            OptionGroupCreateParam(
                menuId = menuId,
                hostId = authContext.getCurrentUserId(),
                name = request.name,
                isRequired = request.isRequired,
            )
        )
        return OptionGroupResponse.from(optionGroup)
    }

    @Operation(summary = "옵션 그룹 수정")
    @PatchMapping("/{id}")
    fun update(
        @PathVariable menuId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody request: OptionGroupUpdateRequest,
    ): OptionGroupResponse {
        val optionGroup = optionGroupService.update(
            OptionGroupUpdateParam(
                id = id,
                menuId = menuId,
                hostId = authContext.getCurrentUserId(),
                name = request.name,
                isRequired = request.isRequired,
            )
        )
        return OptionGroupResponse.from(optionGroup)
    }

    @Operation(summary = "옵션 그룹 삭제")
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable menuId: Long,
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        optionGroupService.delete(
            OptionGroupDeleteParam(
                id = id,
                menuId = menuId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return ResponseEntity.noContent().build()
    }
}
