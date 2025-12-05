package com.wonjiyap.homeorder.exception

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(HomeOrderException::class)
    fun handleHomeOrderException(e: HomeOrderException): ResponseEntity<ErrorResponse> {
        log.warn("HomeOrderException: code={}, message={}", e.errorCode.code, e.message)
        return ResponseEntity
            .status(e.errorCode.code)
            .body(ErrorResponse.of(e.errorCode, e.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val fieldErrors = e.bindingResult.fieldErrors.map { fieldError ->
            ErrorResponse.FieldError(
                field = fieldError.field,
                value = fieldError.rejectedValue?.toString(),
                reason = fieldError.defaultMessage ?: "유효하지 않은 값입니다",
            )
        }
        log.warn("ValidationException: {}", fieldErrors)
        return ResponseEntity
            .status(ErrorCode.BAD_REQUEST.code)
            .body(ErrorResponse.of(ErrorCode.BAD_REQUEST, "잘못된 입력값입니다", fieldErrors))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected exception: ", e)
        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.code)
            .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다"))
    }
}