package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.UserEntity
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.repository.UserRepository
import com.wonjiyap.homeorder.repository.dto.UserFetchOneParam
import com.wonjiyap.homeorder.service.dto.LoginParam
import com.wonjiyap.homeorder.service.dto.LoginResult
import com.wonjiyap.homeorder.service.dto.SignupParam
import com.wonjiyap.homeorder.util.JwtUtil
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
) {

    fun signup(param: SignupParam): UserEntity {
        val existingUser = userRepository.fetchOne(
            UserFetchOneParam(
                loginId = param.loginId,
                deleted = false,
            )
        )
        if (existingUser != null) {
            throw HomeOrderException(ErrorCode.CONFLICT, "이미 사용중인 아이디입니다")
        }

        return transaction {
            UserEntity.new {
                loginId = param.loginId
                password = passwordEncoder.encode(param.password)
                nickname = param.nickname
            }
        }
    }

    fun login(param: LoginParam): LoginResult {
        val user = userRepository.fetchOne(
            UserFetchOneParam(
                loginId = param.loginId,
                deleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다")

        if (!passwordEncoder.matches(param.password, user.password)) {
            throw HomeOrderException(ErrorCode.BAD_REQUEST, "비밀번호가 일치하지 않습니다")
        }

        val token = jwtUtil.generateToken(user.id.value)
        return LoginResult(accessToken = token)
    }
}