package com.wonjiyap.homeorder.controller

import com.wonjiyap.homeorder.controller.dto.LoginRequest
import com.wonjiyap.homeorder.controller.dto.LoginResponse
import com.wonjiyap.homeorder.controller.dto.SignupRequest
import com.wonjiyap.homeorder.controller.dto.SignupResponse
import com.wonjiyap.homeorder.service.AuthService
import com.wonjiyap.homeorder.service.dto.LoginParam
import com.wonjiyap.homeorder.service.dto.SignupParam
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(@Valid @RequestBody request: SignupRequest): SignupResponse {
        val user = authService.signup(
            SignupParam(
                loginId = request.loginId,
                password = request.password,
                nickname = request.nickname,
            )
        )
        return SignupResponse(userId = user.id.value)
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): LoginResponse {
        val result = authService.login(
            LoginParam(
                loginId = request.loginId,
                password = request.password,
            )
        )
        return LoginResponse(accessToken = result.accessToken)
    }
}