package com.wonjiyap.homeorder.controller

import com.wonjiyap.homeorder.controller.dto.MenuCreateRequest
import com.wonjiyap.homeorder.controller.dto.MenuResponse
import com.wonjiyap.homeorder.controller.dto.MenuUpdateRequest
import com.wonjiyap.homeorder.service.MenuService
import com.wonjiyap.homeorder.service.dto.MenuCreateParam
import com.wonjiyap.homeorder.service.dto.MenuDeleteParam
import com.wonjiyap.homeorder.service.dto.MenuGetParam
import com.wonjiyap.homeorder.service.dto.MenuListParam
import com.wonjiyap.homeorder.service.dto.MenuUpdateParam
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

@Tag(name = "Menu", description = "메뉴 API")
@RestController
@RequestMapping("/api/categories/{categoryId}/menus")
class MenuController(
    private val menuService: MenuService,
    private val authContext: AuthContext,
) {

    @Operation(summary = "메뉴 목록 조회")
    @GetMapping
    fun list(
        @PathVariable categoryId: Long,
    ): List<MenuResponse> {
        val menus = menuService.list(
            MenuListParam(
                categoryId = categoryId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return menus.map { MenuResponse.from(it) }
    }

    @Operation(summary = "메뉴 상세 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable categoryId: Long,
        @PathVariable id: Long,
    ): MenuResponse {
        val menu = menuService.get(
            MenuGetParam(
                id = id,
                categoryId = categoryId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return MenuResponse.from(menu)
    }

    @Operation(summary = "메뉴 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable categoryId: Long,
        @Valid @RequestBody request: MenuCreateRequest,
    ): MenuResponse {
        val menu = menuService.create(
            MenuCreateParam(
                categoryId = categoryId,
                hostId = authContext.getCurrentUserId(),
                name = request.name,
                description = request.description,
                isRecommended = request.isRecommended,
                isSoldOut = request.isSoldOut,
            )
        )
        return MenuResponse.from(menu)
    }

    @Operation(summary = "메뉴 수정")
    @PatchMapping("/{id}")
    fun update(
        @PathVariable categoryId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody request: MenuUpdateRequest,
    ): MenuResponse {
        val menu = menuService.update(
            MenuUpdateParam(
                id = id,
                categoryId = categoryId,
                hostId = authContext.getCurrentUserId(),
                name = request.name,
                description = request.description,
                isRecommended = request.isRecommended,
                isSoldOut = request.isSoldOut,
            )
        )
        return MenuResponse.from(menu)
    }

    @Operation(summary = "메뉴 삭제")
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable categoryId: Long,
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        menuService.delete(
            MenuDeleteParam(
                id = id,
                categoryId = categoryId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return ResponseEntity.noContent().build()
    }
}
