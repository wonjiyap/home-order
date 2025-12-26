package com.wonjiyap.homeorder.controller

import com.wonjiyap.homeorder.controller.dto.PartyGuestJoinRequest
import com.wonjiyap.homeorder.controller.dto.PartyGuestJoinResponse
import com.wonjiyap.homeorder.controller.dto.PartyGuestResponse
import com.wonjiyap.homeorder.controller.dto.PartyGuestUpdateRequest
import com.wonjiyap.homeorder.service.PartyGuestService
import com.wonjiyap.homeorder.service.dto.PartyGuestDeleteParam
import com.wonjiyap.homeorder.service.dto.PartyGuestGetParam
import com.wonjiyap.homeorder.service.dto.PartyGuestJoinParam
import com.wonjiyap.homeorder.service.dto.PartyGuestListParam
import com.wonjiyap.homeorder.service.dto.PartyGuestUpdateParam
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

@Tag(name = "PartyGuest", description = "파티 게스트 API")
@RestController
@RequestMapping("/api/parties")
class PartyGuestController(
    private val partyGuestService: PartyGuestService,
    private val authContext: AuthContext,
) {

    @Operation(summary = "초대 코드로 파티 참여 (인증 불필요)")
    @PostMapping("/join")
    @ResponseStatus(HttpStatus.CREATED)
    fun join(
        @Valid @RequestBody request: PartyGuestJoinRequest,
    ): PartyGuestJoinResponse {
        val result = partyGuestService.join(
            PartyGuestJoinParam(
                code = request.code,
                nickname = request.nickname,
            )
        )
        return PartyGuestJoinResponse.from(result)
    }

    @Operation(summary = "게스트 목록 조회")
    @GetMapping("/{partyId}/guests")
    fun list(
        @PathVariable partyId: Long,
    ): List<PartyGuestResponse> {
        val guests = partyGuestService.list(
            PartyGuestListParam(
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return guests.map { PartyGuestResponse.from(it) }
    }

    @Operation(summary = "게스트 상세 조회")
    @GetMapping("/{partyId}/guests/{id}")
    fun get(
        @PathVariable partyId: Long,
        @PathVariable id: Long,
    ): PartyGuestResponse {
        val guest = partyGuestService.get(
            PartyGuestGetParam(
                id = id,
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return PartyGuestResponse.from(guest)
    }

    @Operation(summary = "게스트 정보 수정")
    @PatchMapping("/{partyId}/guests/{id}")
    fun update(
        @PathVariable partyId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody request: PartyGuestUpdateRequest,
    ): PartyGuestResponse {
        val guest = partyGuestService.update(
            PartyGuestUpdateParam(
                id = id,
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
                nickname = request.nickname,
                isBlocked = request.isBlocked,
            )
        )
        return PartyGuestResponse.from(guest)
    }

    @Operation(summary = "게스트 삭제")
    @DeleteMapping("/{partyId}/guests/{id}")
    fun delete(
        @PathVariable partyId: Long,
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        partyGuestService.delete(
            PartyGuestDeleteParam(
                id = id,
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return ResponseEntity.noContent().build()
    }
}
