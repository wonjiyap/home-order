package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.service.dto.LoginParam
import com.wonjiyap.homeorder.service.dto.SignupParam
import com.wonjiyap.homeorder.util.JwtUtil
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@Rollback
class AuthServiceTest {

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    @Test
    fun `회원가입 성공 테스트`() {
        // Given
        val param = SignupParam(
            loginId = "newuser",
            password = "password123",
            nickname = "새유저",
        )

        // When
        val user = authService.signup(param)

        // Then
        assertThat(user.id.value).isGreaterThan(0)
        assertThat(user.loginId).isEqualTo("newuser")
        assertThat(user.nickname).isEqualTo("새유저")
    }

    @Test
    fun `회원가입시 비밀번호 암호화 테스트`() {
        // Given
        val rawPassword = "password123"
        val param = SignupParam(
            loginId = "encrypttest",
            password = rawPassword,
            nickname = "암호화테스트",
        )

        // When
        val user = authService.signup(param)

        // Then
        assertThat(user.password).isNotEqualTo(rawPassword) // 평문이 아님
        assertThat(passwordEncoder.matches(rawPassword, user.password)).isTrue() // BCrypt 매칭
    }

    @Test
    fun `중복 아이디 회원가입 실패 테스트`() {
        // Given
        val loginId = "duplicateuser"
        authService.signup(
            SignupParam(
                loginId = loginId,
                password = "password123",
                nickname = "첫번째유저",
            )
        )

        // When & Then
        assertThatThrownBy {
            authService.signup(
                SignupParam(
                    loginId = loginId,
                    password = "password456",
                    nickname = "두번째유저",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT)
            .hasMessageContaining("이미 사용중인 아이디")
    }

    @Test
    fun `로그인 성공 테스트`() {
        // Given
        val loginId = "logintest"
        val password = "password123"
        authService.signup(
            SignupParam(
                loginId = loginId,
                password = password,
                nickname = "로그인테스트",
            )
        )

        // When
        val result = authService.login(
            LoginParam(
                loginId = loginId,
                password = password,
            )
        )

        // Then
        assertThat(result.accessToken).isNotBlank()
        assertThat(jwtUtil.validateToken(result.accessToken)).isTrue()
    }

    @Test
    fun `로그인시 JWT에 userId 포함 테스트`() {
        // Given
        val loginId = "jwttest"
        val password = "password123"
        val user = authService.signup(
            SignupParam(
                loginId = loginId,
                password = password,
                nickname = "JWT테스트",
            )
        )

        // When
        val result = authService.login(
            LoginParam(
                loginId = loginId,
                password = password,
            )
        )

        // Then
        val extractedUserId = jwtUtil.getUserIdFromToken(result.accessToken)
        assertThat(extractedUserId).isEqualTo(user.id.value)
    }

    @Test
    fun `존재하지 않는 사용자 로그인 실패 테스트`() {
        // Given
        val param = LoginParam(
            loginId = "nonexistent",
            password = "password123",
        )

        // When & Then
        assertThatThrownBy {
            authService.login(param)
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("사용자를 찾을 수 없습니다")
    }

    @Test
    fun `잘못된 비밀번호 로그인 실패 테스트`() {
        // Given
        val loginId = "wrongpassword"
        val correctPassword = "correctpass"
        val wrongPassword = "wrongpass"
        authService.signup(
            SignupParam(
                loginId = loginId,
                password = correctPassword,
                nickname = "비밀번호테스트",
            )
        )

        // When & Then
        assertThatThrownBy {
            authService.login(
                LoginParam(
                    loginId = loginId,
                    password = wrongPassword,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST)
            .hasMessageContaining("비밀번호가 일치하지 않습니다")
    }
}