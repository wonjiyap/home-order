package com.wonjiyap.homeorder.controller

import com.wonjiyap.homeorder.controller.dto.OptionCreateRequest
import com.wonjiyap.homeorder.controller.dto.OptionReorderRequest
import com.wonjiyap.homeorder.controller.dto.OptionResponse
import com.wonjiyap.homeorder.controller.dto.OptionUpdateRequest
import com.wonjiyap.homeorder.service.OptionService
import com.wonjiyap.homeorder.service.dto.OptionCreateParam
import com.wonjiyap.homeorder.service.dto.OptionDeleteParam
import com.wonjiyap.homeorder.service.dto.OptionGetParam
import com.wonjiyap.homeorder.service.dto.OptionListParam
import com.wonjiyap.homeorder.service.dto.OptionReorderParam
import com.wonjiyap.homeorder.service.dto.OptionUpdateParam
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

@Tag(name = "Option", description = "옵션 API")
@RestController
@RequestMapping("/api/option-groups/{optionGroupId}/options")
class OptionController(
    private val optionService: OptionService,
    private val authContext: AuthContext,
) {

    @Operation(summary = "옵션 목록 조회")
    @GetMapping
    fun list(
        @PathVariable optionGroupId: Long,
    ): List<OptionResponse> {
        val options = optionService.list(
            OptionListParam(
                optionGroupId = optionGroupId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return options.map { OptionResponse.from(it) }
    }

    @Operation(summary = "옵션 상세 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable optionGroupId: Long,
        @PathVariable id: Long,
    ): OptionResponse {
        val option = optionService.get(
            OptionGetParam(
                id = id,
                optionGroupId = optionGroupId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return OptionResponse.from(option)
    }

    @Operation(summary = "옵션 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable optionGroupId: Long,
        @Valid @RequestBody request: OptionCreateRequest,
    ): OptionResponse {
        val option = optionService.create(
            OptionCreateParam(
                optionGroupId = optionGroupId,
                hostId = authContext.getCurrentUserId(),
                name = request.name,
            )
        )
        return OptionResponse.from(option)
    }

    @Operation(summary = "옵션 수정")
    @PatchMapping("/{id}")
    fun update(
        @PathVariable optionGroupId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody request: OptionUpdateRequest,
    ): OptionResponse {
        val option = optionService.update(
            OptionUpdateParam(
                id = id,
                optionGroupId = optionGroupId,
                hostId = authContext.getCurrentUserId(),
                name = request.name,
            )
        )
        return OptionResponse.from(option)
    }

    @Operation(summary = "옵션 삭제")
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable optionGroupId: Long,
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        optionService.delete(
            OptionDeleteParam(
                id = id,
                optionGroupId = optionGroupId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "옵션 순서 변경")
    @PatchMapping("/reorder")
    fun reorder(
        @PathVariable optionGroupId: Long,
        @Valid @RequestBody request: OptionReorderRequest,
    ): List<OptionResponse> {
        val options = optionService.reorder(
            OptionReorderParam(
                optionGroupId = optionGroupId,
                hostId = authContext.getCurrentUserId(),
                optionIds = request.optionIds,
            )
        )
        return options.map { OptionResponse.from(it) }
    }
}
