package com.wonjiyap.homeorder.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:NotBlank(message = "아이디를 입력해주세요")
    @field:Size(min = 4, max = 20, message = "아이디는 4~20자로 입력해주세요")
    val loginId: String,

    @field:NotBlank(message = "비밀번호를 입력해주세요")
    @field:Size(min = 8, max = 20, message = "비밀번호는 8~20자로 입력해주세요")
    val password: String,

    @field:NotBlank(message = "닉네임을 입력해주세요")
    @field:Size(min = 2, max = 20, message = "닉네임은 2~20자로 입력해주세요")
    val nickname: String,
)

data class SignupResponse(
    val userId: Long,
)

data class LoginRequest(
    @field:NotBlank(message = "아이디를 입력해주세요")
    val loginId: String,

    @field:NotBlank(message = "비밀번호를 입력해주세요")
    val password: String,
)

data class LoginResponse(
    val accessToken: String,
)
