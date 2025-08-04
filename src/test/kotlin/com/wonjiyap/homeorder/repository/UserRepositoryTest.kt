package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.UserEntity
import com.wonjiyap.homeorder.repository.dto.UserFetchOneParam
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@Transactional
@Rollback
class UserRepositoryTest {

    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        userRepository = UserRepository()
    }

    @Test
    fun `ID로 사용자 조회 테스트`() {
        // Given
        val user = transaction {
            UserEntity.new {
                loginId = "testuser123"
                password = "password123"
                nickname = "테스트유저"
            }
        }
        val userId = user.id.value

        // When
        val param = UserFetchOneParam(
            id = userId,
        )
        val foundUser = userRepository.fetchOne(param)

        // Then
        assertThat(foundUser).isNotNull()
        assertThat(foundUser?.loginId).isEqualTo("testuser123")
        assertThat(foundUser?.nickname).isEqualTo("테스트유저")
    }

    @Test
    fun `loginId로 사용자 조회 테스트`() {
        // Given
        val loginId = "uniqueuser456"
        val user = transaction {
            UserEntity.new {
                this.loginId = loginId
                password = "password123"
                nickname = "유니크테스트"
            }
        }

        // When
        val param = UserFetchOneParam(
            loginId = loginId,
        )
        val foundUser = userRepository.fetchOne(param)

        // Then
        assertThat(foundUser).isNotNull()
        assertThat(foundUser?.loginId).isEqualTo(loginId)
        assertThat(foundUser?.nickname).isEqualTo("유니크테스트")
    }

    @Test
    fun `활성 사용자만 조회 테스트`() {
        // Given
        val activeUser = transaction {
            UserEntity.new {
                loginId = "activeuser"
                password = "password123"
                nickname = "활성유저"
            }
        }

        val deletedUser = transaction {
            UserEntity.new {
                loginId = "deleteduser"
                password = "password123"
                nickname = "삭제유저"
                deletedAt = Instant.now()
            }
        }

        // When - 활성 사용자만 조회
        val activeParam = UserFetchOneParam(loginId = "activeuser", deleted = false)
        val foundActiveUser = userRepository.fetchOne(activeParam)

        // 삭제된 사용자를 활성 조건으로 조회
        val deletedParam = UserFetchOneParam(loginId = "deleteduser", deleted = false)
        val notFoundUser = userRepository.fetchOne(deletedParam)

        // Then
        assertThat(foundActiveUser).isNotNull()
        assertThat(foundActiveUser?.nickname).isEqualTo("활성유저")
        assertThat(notFoundUser).isNull() // 삭제된 사용자는 조회 안됨
    }

    @Test
    fun `삭제된 사용자 조회 테스트`() {
        // Given
        val user = transaction {
            UserEntity.new {
                loginId = "deleteduser2"
                password = "password123"
                nickname = "삭제유저2"
                deletedAt = Instant.now()
            }
        }

        // When - 삭제된 사용자만 조회
        val deletedParam = UserFetchOneParam(loginId = "deleteduser2", deleted = true)
        val foundDeletedUser = userRepository.fetchOne(deletedParam)

        // 전체 조회 (deleted = null)
        val allParam = UserFetchOneParam(loginId = "deleteduser2", deleted = null)
        val foundUser = userRepository.fetchOne(allParam)

        // Then
        assertThat(foundDeletedUser).isNotNull()
        assertThat(foundDeletedUser?.isDeleted()).isTrue()
        assertThat(foundUser).isNotNull() // 전체 조회에서는 나와야 함
    }

    @Test
    fun `존재하지 않는 사용자 조회시 null 반환 테스트`() {
        // Given
        val nonExistentId = 99999L

        // When
        val param = UserFetchOneParam(id = nonExistentId)
        val foundUser = userRepository.fetchOne(param)

        // Then
        assertThat(foundUser).isNull()
    }

    @Test
    fun `조건 없이 조회시 첫 번째 사용자 반환 테스트`() {
        // Given
        transaction {
            UserEntity.new {
                loginId = "user1"
                password = "password123"
                nickname = "유저1"
            }
            UserEntity.new {
                loginId = "user2"
                password = "password123"
                nickname = "유저2"
            }
        }

        // When
        val param = UserFetchOneParam() // 모든 조건이 null
        val foundUser = userRepository.fetchOne(param)

        // Then
        assertThat(foundUser).isNotNull() // 아무거나 하나는 나와야 함
    }

    @Test
    fun `사용자 정보 수정 후 저장 테스트`() {
        // Given
        val user = transaction {
            UserEntity.new {
                loginId = "updatetest"
                password = "oldpassword"
                nickname = "옛날닉네임"
            }
        }
        userRepository.save(user)
        val originalUpdatedAt = user.updatedAt

        // When
        Thread.sleep(1) // updatedAt 차이를 위해
        user.nickname = "새로운닉네임"
        user.password = "newpassword"
        user.updatedAt = Instant.now()

        userRepository.save(user)

        // Then - DB에서 다시 조회해서 확인
        val param = UserFetchOneParam(id = user.id.value)
        val updatedUser = userRepository.fetchOne(param)

        assertThat(updatedUser?.nickname).isEqualTo("새로운닉네임")
        assertThat(updatedUser?.password).isEqualTo("newpassword")
        assertThat(updatedUser?.updatedAt).isAfter(originalUpdatedAt)
        assertThat(updatedUser?.loginId).isEqualTo("updatetest") // 변경되지 않음
    }

    @Test
    fun `사용자 Soft Delete 후 저장 테스트`() {
        // Given
        val user = transaction {
            UserEntity.new {
                loginId = "softdeletetest"
                password = "password123"
                nickname = "삭제될유저"
            }
        }
        val userId = user.id.value

        // When - Soft Delete 수행
        user.deletedAt = Instant.now()
        userRepository.save(user)

        // Then - 활성 사용자로는 조회 안됨
        val activeParam = UserFetchOneParam(id = userId, deleted = false)
        val activeUser = userRepository.fetchOne(activeParam)
        assertThat(activeUser).isNull()

        // 삭제된 사용자로는 조회됨
        val deletedParam = UserFetchOneParam(id = userId, deleted = true)
        val deletedUser = userRepository.fetchOne(deletedParam)
        assertThat(deletedUser).isNotNull()
        assertThat(deletedUser?.isDeleted()).isTrue()
    }

    @Test
    fun `복합 조건 조회 테스트`() {
        // Given
        val user = transaction {
            UserEntity.new {
                loginId = "combotest"
                password = "password123"
                nickname = "조합테스트"
            }
        }
        val userId = user.id.value

        // When - ID와 loginId, deleted 조건 모두 사용
        val param = UserFetchOneParam(
            id = userId,
            loginId = "combotest",
            deleted = false
        )
        val foundUser = userRepository.fetchOne(param)

        // Then
        assertThat(foundUser).isNotNull()
        assertThat(foundUser?.id?.value).isEqualTo(userId)
        assertThat(foundUser?.loginId).isEqualTo("combotest")
        assertThat(foundUser?.isDeleted()).isFalse()
    }

    @Test
    fun `잘못된 조건 조합시 null 반환 테스트`() {
        // Given
        val user = transaction {
            UserEntity.new {
                loginId = "wrongcombo"
                password = "password123"
                nickname = "잘못된조합"
            }
        }

        // When - 존재하는 ID + 잘못된 loginId
        val param = UserFetchOneParam(
            id = user.id.value,
            loginId = "nonexistent"
        )
        val foundUser = userRepository.fetchOne(param)

        // Then
        assertThat(foundUser).isNull() // 조건이 맞지 않으면 null
    }
}