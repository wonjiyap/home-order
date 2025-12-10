package com.wonjiyap.homeorder.interceptor

import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.util.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor(
    private val jwtUtil: JwtUtil,
) : HandlerInterceptor {

    companion object {
        const val USER_ID_ATTRIBUTE = "userId"
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val token = extractToken(request)
            ?: throw HomeOrderException(ErrorCode.UNAUTHORIZED, "토큰이 필요합니다")

        val userId = jwtUtil.getUserIdFromToken(token)
        request.setAttribute(USER_ID_ATTRIBUTE, userId)

        return true
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader(AUTHORIZATION_HEADER) ?: return null
        if (!header.startsWith(BEARER_PREFIX)) return null
        return header.substring(BEARER_PREFIX.length)
    }
}
