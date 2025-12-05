package com.wonjiyap.homeorder.exception

open class HomeOrderException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.message,
) : RuntimeException(message)