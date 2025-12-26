package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.PartyEntity
import com.wonjiyap.homeorder.domain.UserEntity
import com.wonjiyap.homeorder.enums.PartyStatus
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.service.dto.InviteCodeCreateParam
import com.wonjiyap.homeorder.service.dto.InviteCodeDeleteParam
import com.wonjiyap.homeorder.service.dto.InviteCodeGetParam
import com.wonjiyap.homeorder.service.dto.InviteCodeListParam
import com.wonjiyap.homeorder.service.dto.InviteCodeUpdateParam
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest
@Transactional
@Rollback
class InviteCodeServiceTest {

    @Autowired
    private lateinit var inviteCodeService: InviteCodeService

    private var testUserId: Long = 0
    private var testPartyId: Long = 0

    @BeforeEach
    fun setUp() {
        testUserId = transaction {
            UserEntity.new {
                loginId = "invitecodetest_${System.nanoTime()}"
                password = "password123"
                nickname = "초대코드테스트유저"
            }.id.value
        }
        testPartyId = transaction {
            PartyEntity.new {
                hostId = testUserId
                name = "테스트 파티"
                status = PartyStatus.PLANNING
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }
    }

    @Test
    fun `초대 코드 생성 테스트`() {
        // Given
        val param = InviteCodeCreateParam(
            partyId = testPartyId,
            hostId = testUserId,
        )

        // When
        val inviteCode = inviteCodeService.create(param)

        // Then
        assertThat(inviteCode.id.value).isGreaterThan(0)
        assertThat(inviteCode.partyId).isEqualTo(testPartyId)
        assertThat(inviteCode.code).hasSize(8)
        assertThat(inviteCode.isActive).isTrue()
        assertThat(inviteCode.expiresAt).isNull()
        assertThat(inviteCode.deletedAt).isNull()
    }

    @Test
    fun `만료 시간과 함께 초대 코드 생성 테스트`() {
        // Given
        val expiresAt = Instant.now().plus(7, ChronoUnit.DAYS)
        val param = InviteCodeCreateParam(
            partyId = testPartyId,
            hostId = testUserId,
            expiresAt = expiresAt,
        )

        // When
        val inviteCode = inviteCodeService.create(param)

        // Then
        assertThat(inviteCode.expiresAt).isEqualTo(expiresAt)
    }

    @Test
    fun `과거 만료 시간으로 생성시 예외 발생 테스트`() {
        // Given
        val pastExpiresAt = Instant.now().minus(1, ChronoUnit.DAYS)

        // When & Then
        assertThatThrownBy {
            inviteCodeService.create(
                InviteCodeCreateParam(
                    partyId = testPartyId,
                    hostId = testUserId,
                    expiresAt = pastExpiresAt,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST)
            .hasMessageContaining("만료 시간은 현재 시간 이후여야 합니다")
    }

    @Test
    fun `존재하지 않는 파티에 초대 코드 생성시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            inviteCodeService.create(
                InviteCodeCreateParam(
                    partyId = 999999L,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("파티를 찾을 수 없습니다")
    }

    @Test
    fun `다른 사용자 파티에 초대 코드 생성시 예외 발생 테스트`() {
        // Given
        val otherUserId = transaction {
            UserEntity.new {
                loginId = "otheruser_${System.nanoTime()}"
                password = "password123"
                nickname = "다른유저"
            }.id.value
        }

        // When & Then
        assertThatThrownBy {
            inviteCodeService.create(
                InviteCodeCreateParam(
                    partyId = testPartyId,
                    hostId = otherUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `초대 코드 목록 조회 테스트`() {
        // Given - 파티당 활성 초대 코드는 1개만 가능하므로 첫 번째를 비활성화 후 두 번째 생성
        val inviteCode1 = inviteCodeService.create(
            InviteCodeCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )
        inviteCodeService.update(
            InviteCodeUpdateParam(
                id = inviteCode1.id.value,
                partyId = testPartyId,
                hostId = testUserId,
                isActive = false,
            )
        )
        inviteCodeService.create(
            InviteCodeCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // When
        val inviteCodes = inviteCodeService.list(
            InviteCodeListParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // Then - 비활성 1개 + 활성 1개 = 2개
        assertThat(inviteCodes).hasSize(2)
    }

    @Test
    fun `삭제된 초대 코드는 목록에서 제외됨 테스트`() {
        // Given - 파티당 활성 초대 코드는 1개만 가능
        val inviteCode1 = inviteCodeService.create(
            InviteCodeCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )
        
        // 삭제
        inviteCodeService.delete(
            InviteCodeDeleteParam(
                id = inviteCode1.id.value,
                partyId = testPartyId,
                hostId = testUserId,
            )
        )
        
        // 새 초대 코드 생성
        val inviteCode2 = inviteCodeService.create(
            InviteCodeCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // When
        val inviteCodes = inviteCodeService.list(
            InviteCodeListParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // Then - 삭제된 코드는 제외되고 새 코드만 조회됨
        assertThat(inviteCodes).hasSize(1)
        assertThat(inviteCodes[0].id.value).isEqualTo(inviteCode2.id.value)
    }

    @Test
    fun `초대 코드 상세 조회 테스트`() {
        // Given
        val created = inviteCodeService.create(
            InviteCodeCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // When
        val inviteCode = inviteCodeService.get(
            InviteCodeGetParam(
                id = created.id.value,
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(inviteCode.id.value).isEqualTo(created.id.value)
        assertThat(inviteCode.code).isEqualTo(created.code)
    }

    @Test
    fun `존재하지 않는 초대 코드 조회시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            inviteCodeService.get(
                InviteCodeGetParam(
                    id = 999999L,
                    partyId = testPartyId,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("초대 코드를 찾을 수 없습니다")
    }

    @Test
    fun `초대 코드 비활성화 테스트`() {
        // Given
        val created = inviteCodeService.create(
            InviteCodeCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // When
        val updated = inviteCodeService.update(
            InviteCodeUpdateParam(
                id = created.id.value,
                partyId = testPartyId,
                hostId = testUserId,
                isActive = false,
            )
        )

        // Then
        assertThat(updated.isActive).isFalse()
    }

    @Test
    fun `초대 코드 만료 시간 변경 테스트`() {
        // Given
        val created = inviteCodeService.create(
            InviteCodeCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )
        val newExpiresAt = Instant.now().plus(14, ChronoUnit.DAYS)

        // When
        val updated = inviteCodeService.update(
            InviteCodeUpdateParam(
                id = created.id.value,
                partyId = testPartyId,
                hostId = testUserId,
                expiresAt = newExpiresAt,
            )
        )

        // Then
        assertThat(updated.expiresAt).isEqualTo(newExpiresAt)
    }

    @Test
    fun `수정시 과거 만료 시간으로 변경하면 예외 발생 테스트`() {
        // Given
        val created = inviteCodeService.create(
            InviteCodeCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )
        val pastExpiresAt = Instant.now().minus(1, ChronoUnit.DAYS)

        // When & Then
        assertThatThrownBy {
            inviteCodeService.update(
                InviteCodeUpdateParam(
                    id = created.id.value,
                    partyId = testPartyId,
                    hostId = testUserId,
                    expiresAt = pastExpiresAt,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST)
    }

    @Test
    fun `존재하지 않는 초대 코드 수정시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            inviteCodeService.update(
                InviteCodeUpdateParam(
                    id = 999999L,
                    partyId = testPartyId,
                    hostId = testUserId,
                    isActive = false,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `초대 코드 삭제 테스트 (Soft Delete)`() {
        // Given
        val created = inviteCodeService.create(
            InviteCodeCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // When
        inviteCodeService.delete(
            InviteCodeDeleteParam(
                id = created.id.value,
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // Then - 삭제된 초대 코드는 조회되지 않음
        assertThatThrownBy {
            inviteCodeService.get(
                InviteCodeGetParam(
                    id = created.id.value,
                    partyId = testPartyId,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `존재하지 않는 초대 코드 삭제시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            inviteCodeService.delete(
                InviteCodeDeleteParam(
                    id = 999999L,
                    partyId = testPartyId,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `만료된 초대 코드는 isExpired가 true 테스트`() {
        // Given
        val expiresAt = Instant.now().minus(1, ChronoUnit.SECONDS)
        val inviteCode = inviteCodeService.create(
            InviteCodeCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // 직접 만료 시간 설정 (테스트용)
        transaction {
            inviteCode.expiresAt = expiresAt
        }

        // When
        val fetched = inviteCodeService.get(
            InviteCodeGetParam(
                id = inviteCode.id.value,
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(fetched.isExpired()).isTrue()
        assertThat(fetched.isValid()).isFalse()
    }

    @Test
    fun `비활성화된 초대 코드는 isValid가 false 테스트`() {
        // Given
        val inviteCode = inviteCodeService.create(
            InviteCodeCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )
        inviteCodeService.update(
            InviteCodeUpdateParam(
                id = inviteCode.id.value,
                partyId = testPartyId,
                hostId = testUserId,
                isActive = false,
            )
        )

        // When
        val fetched = inviteCodeService.get(
            InviteCodeGetParam(
                id = inviteCode.id.value,
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(fetched.isActive).isFalse()
        assertThat(fetched.isValid()).isFalse()
    }

    @Test
    fun `초대 코드는 8자리 대문자 알파벳과 숫자 조합 테스트`() {
        // Given & When
        val inviteCode = inviteCodeService.create(
            InviteCodeCreateParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(inviteCode.code).hasSize(8)
        assertThat(inviteCode.code).matches("^[A-Z0-9]+$")
    }
}
