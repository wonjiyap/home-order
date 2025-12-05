package com.wonjiyap.homeorder.exception

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val code: Int,
    val message: String,
    val details: List<FieldError>? = null,
) {
    companion object {
        fun of(errorCode: ErrorCode, message: String): ErrorResponse {
            return ErrorResponse(
                code = errorCode.code,
                message = message,
            )
        }

        fun of(errorCode: ErrorCode, message: String, details: List<FieldError>): ErrorResponse {
            return ErrorResponse(
                code = errorCode.code,
                message = message,
                details = details,
            )
        }
    }

    data class FieldError(
        val field: String,
        val value: String?,
        val reason: String,
    )
}