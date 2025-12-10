package com.wonjiyap.homeorder.controller

import com.wonjiyap.homeorder.controller.dto.CreatePartyRequest
import com.wonjiyap.homeorder.controller.dto.PartyResponse
import com.wonjiyap.homeorder.controller.dto.UpdatePartyRequest
import com.wonjiyap.homeorder.service.PartyService
import com.wonjiyap.homeorder.enums.PartyStatus
import com.wonjiyap.homeorder.service.dto.CreatePartyParam
import com.wonjiyap.homeorder.service.dto.DeletePartyParam
import com.wonjiyap.homeorder.service.dto.GetPartyParam
import com.wonjiyap.homeorder.service.dto.ListPartyParam
import com.wonjiyap.homeorder.service.dto.UpdatePartyParam
import com.wonjiyap.homeorder.util.AuthContext
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Party", description = "파티 관리 API")
@RestController
@RequestMapping("/api/parties")
class PartyController(
    private val partyService: PartyService,
    private val authContext: AuthContext,
) {

    @Operation(summary = "내 파티 목록 조회")
    @GetMapping
    fun list(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) status: PartyStatus?,
    ): List<PartyResponse> {
        val parties = partyService.list(
            ListPartyParam(
                hostId = authContext.getCurrentUserId(),
                name = name,
                status = status,
            )
        )
        return parties.map { PartyResponse.from(it) }
    }

    @Operation(summary = "파티 상세 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable id: Long,
    ): PartyResponse {
        val party = partyService.get(
            GetPartyParam(
                id = id,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return PartyResponse.from(party)
    }

    @Operation(summary = "파티 생성")
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreatePartyRequest,
    ): PartyResponse {
        val party = partyService.create(
            CreatePartyParam(
                hostId = authContext.getCurrentUserId(),
                name = request.name,
                description = request.description,
                date = request.date,
                location = request.location,
            )
        )
        return PartyResponse.from(party)
    }

    @Operation(summary = "파티 수정")
    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdatePartyRequest,
    ): PartyResponse {
        val party = partyService.update(
            UpdatePartyParam(
                id = id,
                hostId = authContext.getCurrentUserId(),
                name = request.name,
                description = request.description,
                date = request.date,
                location = request.location,
                status = request.status,
            )
        )
        return PartyResponse.from(party)
    }

    @Operation(summary = "파티 삭제")
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        partyService.delete(
            DeletePartyParam(
                id = id,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return ResponseEntity.noContent().build()
    }
}
