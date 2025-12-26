package com.wonjiyap.homeorder.controller

import com.wonjiyap.homeorder.controller.dto.InviteCodeCreateRequest
import com.wonjiyap.homeorder.controller.dto.InviteCodeResponse
import com.wonjiyap.homeorder.controller.dto.InviteCodeUpdateRequest
import com.wonjiyap.homeorder.service.InviteCodeService
import com.wonjiyap.homeorder.service.dto.InviteCodeCreateParam
import com.wonjiyap.homeorder.service.dto.InviteCodeDeleteParam
import com.wonjiyap.homeorder.service.dto.InviteCodeGetParam
import com.wonjiyap.homeorder.service.dto.InviteCodeListParam
import com.wonjiyap.homeorder.service.dto.InviteCodeUpdateParam
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

@Tag(name = "InviteCode", description = "초대 코드 API")
@RestController
@RequestMapping("/api/parties/{partyId}/invite-codes")
class InviteCodeController(
    private val inviteCodeService: InviteCodeService,
    private val authContext: AuthContext,
) {

    @Operation(summary = "초대 코드 목록 조회")
    @GetMapping
    fun list(
        @PathVariable partyId: Long,
    ): List<InviteCodeResponse> {
        val inviteCodes = inviteCodeService.list(
            InviteCodeListParam(
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return inviteCodes.map { InviteCodeResponse.from(it) }
    }

    @Operation(summary = "초대 코드 상세 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable partyId: Long,
        @PathVariable id: Long,
    ): InviteCodeResponse {
        val inviteCode = inviteCodeService.get(
            InviteCodeGetParam(
                id = id,
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return InviteCodeResponse.from(inviteCode)
    }

    @Operation(summary = "초대 코드 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable partyId: Long,
        @Valid @RequestBody request: InviteCodeCreateRequest,
    ): InviteCodeResponse {
        val inviteCode = inviteCodeService.create(
            InviteCodeCreateParam(
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
                expiresAt = request.expiresAt,
            )
        )
        return InviteCodeResponse.from(inviteCode)
    }

    @Operation(summary = "초대 코드 수정")
    @PatchMapping("/{id}")
    fun update(
        @PathVariable partyId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody request: InviteCodeUpdateRequest,
    ): InviteCodeResponse {
        val inviteCode = inviteCodeService.update(
            InviteCodeUpdateParam(
                id = id,
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
                isActive = request.isActive,
                expiresAt = request.expiresAt,
            )
        )
        return InviteCodeResponse.from(inviteCode)
    }

    @Operation(summary = "초대 코드 삭제")
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable partyId: Long,
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        inviteCodeService.delete(
            InviteCodeDeleteParam(
                id = id,
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return ResponseEntity.noContent().build()
    }
}
