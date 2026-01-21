package com.wonjiyap.homeorder.controller

import com.wonjiyap.homeorder.controller.dto.OrderCreateRequest
import com.wonjiyap.homeorder.controller.dto.OrderResponse
import com.wonjiyap.homeorder.controller.dto.OrderUpdateRequest
import com.wonjiyap.homeorder.service.OrderService
import com.wonjiyap.homeorder.service.dto.OrderCreateParam
import com.wonjiyap.homeorder.service.dto.OrderGetParam
import com.wonjiyap.homeorder.service.dto.OrderItemCreateParam
import com.wonjiyap.homeorder.service.dto.OrderListParam
import com.wonjiyap.homeorder.service.dto.OrderUpdateParam
import com.wonjiyap.homeorder.util.AuthContext
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Order", description = "주문 API")
@RestController
@RequestMapping("/api/parties/{partyId}/orders")
class OrderController(
    private val orderService: OrderService,
    private val authContext: AuthContext,
) {

    @Operation(summary = "주문 목록 조회")
    @GetMapping
    fun list(
        @PathVariable partyId: Long,
    ): List<OrderResponse> {
        val orders = orderService.list(
            OrderListParam(
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return orders.map { OrderResponse.from(it) }
    }

    @Operation(summary = "주문 상세 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable partyId: Long,
        @PathVariable id: Long,
    ): OrderResponse {
        val order = orderService.get(
            OrderGetParam(
                id = id,
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
            )
        )
        return OrderResponse.from(order)
    }

    @Operation(summary = "주문 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable partyId: Long,
        @Valid @RequestBody request: OrderCreateRequest,
    ): OrderResponse {
        val order = orderService.create(
            OrderCreateParam(
                partyId = partyId,
                guestId = request.guestId,
                items = request.items.map { item ->
                    OrderItemCreateParam(
                        menuId = item.menuId,
                        quantity = item.quantity,
                        notes = item.notes,
                        optionIds = item.optionIds,
                    )
                },
            )
        )
        return OrderResponse.from(order)
    }

    @Operation(summary = "주문 수정")
    @PatchMapping("/{id}")
    fun update(
        @PathVariable partyId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody request: OrderUpdateRequest,
    ): OrderResponse {
        val order = orderService.update(
            OrderUpdateParam(
                id = id,
                partyId = partyId,
                hostId = authContext.getCurrentUserId(),
                status = request.status,
            )
        )
        return OrderResponse.from(order)
    }

}
