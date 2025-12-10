package com.wonjiyap.homeorder.util

import com.wonjiyap.homeorder.interceptor.AuthInterceptor
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Component
class AuthContext {

    fun getCurrentUserId(): Long {
        val request = getCurrentRequest()
        return request.getAttribute(AuthInterceptor.USER_ID_ATTRIBUTE) as Long
    }

    private fun getCurrentRequest(): HttpServletRequest {
        val attributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        return attributes.request
    }
}
