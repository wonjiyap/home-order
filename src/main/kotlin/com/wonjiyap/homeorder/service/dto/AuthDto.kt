package com.wonjiyap.homeorder.service.dto

data class SignupParam(
    val loginId: String,
    val password: String,
    val nickname: String,
)

data class LoginParam(
    val loginId: String,
    val password: String,
)

data class LoginResult(
    val accessToken: String,
)
