package com.wonjiyap.homeorder.util

import com.wonjiyap.homeorder.config.JwtProperties
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtil(
    private val jwtProperties: JwtProperties,
) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    fun generateToken(userId: Long): String {
        val now = Date()
        val expiration = Date(now.time + jwtProperties.expiration)

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun getUserIdFromToken(token: String): Long {
        return try {
            val claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload

            claims.subject.toLong()
        } catch (e: ExpiredJwtException) {
            throw HomeOrderException(ErrorCode.UNAUTHORIZED, "만료된 토큰입니다")
        } catch (e: JwtException) {
            throw HomeOrderException(ErrorCode.UNAUTHORIZED, "유효하지 않은 토큰입니다")
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }
}
