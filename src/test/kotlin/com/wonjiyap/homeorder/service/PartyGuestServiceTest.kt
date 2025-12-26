package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.InviteCodeEntity
import com.wonjiyap.homeorder.domain.PartyEntity
import com.wonjiyap.homeorder.domain.UserEntity
import com.wonjiyap.homeorder.enums.PartyStatus
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.service.dto.PartyGuestDeleteParam
import com.wonjiyap.homeorder.service.dto.PartyGuestGetParam
import com.wonjiyap.homeorder.service.dto.PartyGuestJoinParam
import com.wonjiyap.homeorder.service.dto.PartyGuestListParam
import com.wonjiyap.homeorder.service.dto.PartyGuestUpdateParam
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

@SpringBootTest
@Transactional
@Rollback
class PartyGuestServiceTest {

    @Autowired
    private lateinit var partyGuestService: PartyGuestService

    private var testUserId: Long = 0
    private var testPartyId: Long = 0
    private var testInviteCode: String = ""

    @BeforeEach
    fun setUp() {
        testUserId = transaction {
            UserEntity.new {
                loginId = "guesttest_${System.nanoTime()}"
                password = "password123"
                nickname = "게스트테스트호스트"
            }.id.value
        }
        testPartyId = transaction {
            PartyEntity.new {
                hostId = testUserId
                name = "테스트 파티"
                status = PartyStatus.OPEN
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }
        testInviteCode = transaction {
            InviteCodeEntity.new {
                partyId = testPartyId
                code = "TEST${System.nanoTime().toString().takeLast(4)}"
                isActive = true
                createdAt = Instant.now()
            }.code
        }
    }

    // ========== join 테스트 ==========

    @Test
    fun `초대 코드로 파티 참여 테스트`() {
        // Given
        val param = PartyGuestJoinParam(
            code = testInviteCode,
            nickname = "테스트게스트",
        )

        // When
        val result = partyGuestService.join(param)

        // Then
        assertThat(result.guestId).isGreaterThan(0)
        assertThat(result.partyId).isEqualTo(testPartyId)
        assertThat(result.partyName).isEqualTo("테스트 파티")
        assertThat(result.nickname).isEqualTo("테스트게스트")
    }

    @Test
    fun `소문자 초대 코드로도 참여 가능 테스트`() {
        // Given
        val param = PartyGuestJoinParam(
            code = testInviteCode.lowercase(),
            nickname = "소문자게스트",
        )

        // When
        val result = partyGuestService.join(param)

        // Then
        assertThat(result.nickname).isEqualTo("소문자게스트")
    }

    @Test
    fun `존재하지 않는 초대 코드로 참여시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            partyGuestService.join(
                PartyGuestJoinParam(
                    code = "INVALID1",
                    nickname = "테스트게스트",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("유효하지 않은 초대 코드입니다")
    }

    @Test
    fun `비활성화된 초대 코드로 참여시 예외 발생 테스트`() {
        // Given
        transaction {
            InviteCodeEntity.findById(
                InviteCodeEntity.find {
                    com.wonjiyap.homeorder.tables.InviteCodes.code eq testInviteCode
                }.first().id
            )?.isActive = false
        }

        // When & Then
        assertThatThrownBy {
            partyGuestService.join(
                PartyGuestJoinParam(
                    code = testInviteCode,
                    nickname = "테스트게스트",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST)
            .hasMessageContaining("만료되었거나 비활성화된 초대 코드입니다")
    }

    @Test
    fun `만료된 초대 코드로 참여시 예외 발생 테스트`() {
        // Given
        transaction {
            InviteCodeEntity.find {
                com.wonjiyap.homeorder.tables.InviteCodes.code eq testInviteCode
            }.first().expiresAt = Instant.now().minusSeconds(3600)
        }

        // When & Then
        assertThatThrownBy {
            partyGuestService.join(
                PartyGuestJoinParam(
                    code = testInviteCode,
                    nickname = "테스트게스트",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST)
    }

    @Test
    fun `PLANNING 상태 파티에 참여시 예외 발생 테스트`() {
        // Given
        transaction {
            PartyEntity.findById(testPartyId)?.status = PartyStatus.PLANNING
        }

        // When & Then
        assertThatThrownBy {
            partyGuestService.join(
                PartyGuestJoinParam(
                    code = testInviteCode,
                    nickname = "테스트게스트",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST)
            .hasMessageContaining("현재 참여할 수 없는 파티입니다")
    }

    @Test
    fun `CLOSED 상태 파티에 참여시 예외 발생 테스트`() {
        // Given
        transaction {
            PartyEntity.findById(testPartyId)?.status = PartyStatus.CLOSED
        }

        // When & Then
        assertThatThrownBy {
            partyGuestService.join(
                PartyGuestJoinParam(
                    code = testInviteCode,
                    nickname = "테스트게스트",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST)
    }

    @Test
    fun `중복 닉네임으로 참여시 예외 발생 테스트`() {
        // Given
        partyGuestService.join(
            PartyGuestJoinParam(
                code = testInviteCode,
                nickname = "중복닉네임",
            )
        )

        // When & Then
        assertThatThrownBy {
            partyGuestService.join(
                PartyGuestJoinParam(
                    code = testInviteCode,
                    nickname = "중복닉네임",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT)
            .hasMessageContaining("이미 사용 중인 닉네임입니다")
    }

    // ========== list 테스트 ==========

    @Test
    fun `게스트 목록 조회 테스트`() {
        // Given
        partyGuestService.join(
            PartyGuestJoinParam(
                code = testInviteCode,
                nickname = "게스트1",
            )
        )
        partyGuestService.join(
            PartyGuestJoinParam(
                code = testInviteCode,
                nickname = "게스트2",
            )
        )

        // When
        val guests = partyGuestService.list(
            PartyGuestListParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(guests).hasSize(2)
        assertThat(guests.map { it.nickname }).containsExactlyInAnyOrder("게스트1", "게스트2")
    }

    @Test
    fun `다른 호스트가 게스트 목록 조회시 예외 발생 테스트`() {
        // Given
        val otherUserId = transaction {
            UserEntity.new {
                loginId = "otherhost_${System.nanoTime()}"
                password = "password123"
                nickname = "다른호스트"
            }.id.value
        }

        // When & Then
        assertThatThrownBy {
            partyGuestService.list(
                PartyGuestListParam(
                    partyId = testPartyId,
                    hostId = otherUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    // ========== get 테스트 ==========

    @Test
    fun `게스트 상세 조회 테스트`() {
        // Given
        val joinResult = partyGuestService.join(
            PartyGuestJoinParam(
                code = testInviteCode,
                nickname = "조회테스트",
            )
        )

        // When
        val guest = partyGuestService.get(
            PartyGuestGetParam(
                id = joinResult.guestId,
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(guest.id.value).isEqualTo(joinResult.guestId)
        assertThat(guest.nickname).isEqualTo("조회테스트")
    }

    @Test
    fun `존재하지 않는 게스트 조회시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            partyGuestService.get(
                PartyGuestGetParam(
                    id = 999999L,
                    partyId = testPartyId,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("게스트를 찾을 수 없습니다")
    }

    // ========== update 테스트 ==========

    @Test
    fun `게스트 닉네임 수정 테스트`() {
        // Given
        val joinResult = partyGuestService.join(
            PartyGuestJoinParam(
                code = testInviteCode,
                nickname = "원래닉네임",
            )
        )

        // When
        val updated = partyGuestService.update(
            PartyGuestUpdateParam(
                id = joinResult.guestId,
                partyId = testPartyId,
                hostId = testUserId,
                nickname = "새닉네임",
            )
        )

        // Then
        assertThat(updated.nickname).isEqualTo("새닉네임")
    }

    @Test
    fun `게스트 차단 테스트`() {
        // Given
        val joinResult = partyGuestService.join(
            PartyGuestJoinParam(
                code = testInviteCode,
                nickname = "차단테스트",
            )
        )

        // When
        val updated = partyGuestService.update(
            PartyGuestUpdateParam(
                id = joinResult.guestId,
                partyId = testPartyId,
                hostId = testUserId,
                isBlocked = true,
            )
        )

        // Then
        assertThat(updated.isBlocked).isTrue()
    }

    @Test
    fun `닉네임 중복 수정시 예외 발생 테스트`() {
        // Given
        partyGuestService.join(
            PartyGuestJoinParam(
                code = testInviteCode,
                nickname = "기존닉네임",
            )
        )
        val joinResult = partyGuestService.join(
            PartyGuestJoinParam(
                code = testInviteCode,
                nickname = "수정대상",
            )
        )

        // When & Then
        assertThatThrownBy {
            partyGuestService.update(
                PartyGuestUpdateParam(
                    id = joinResult.guestId,
                    partyId = testPartyId,
                    hostId = testUserId,
                    nickname = "기존닉네임",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT)
    }

    @Test
    fun `같은 닉네임으로 수정시 중복 검증 스킵 테스트`() {
        // Given
        val joinResult = partyGuestService.join(
            PartyGuestJoinParam(
                code = testInviteCode,
                nickname = "동일닉네임",
            )
        )

        // When
        val updated = partyGuestService.update(
            PartyGuestUpdateParam(
                id = joinResult.guestId,
                partyId = testPartyId,
                hostId = testUserId,
                nickname = "동일닉네임",
            )
        )

        // Then
        assertThat(updated.nickname).isEqualTo("동일닉네임")
    }

    // ========== delete 테스트 ==========

    @Test
    fun `게스트 삭제 테스트 (Soft Delete)`() {
        // Given
        val joinResult = partyGuestService.join(
            PartyGuestJoinParam(
                code = testInviteCode,
                nickname = "삭제테스트",
            )
        )

        // When
        partyGuestService.delete(
            PartyGuestDeleteParam(
                id = joinResult.guestId,
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // Then
        assertThatThrownBy {
            partyGuestService.get(
                PartyGuestGetParam(
                    id = joinResult.guestId,
                    partyId = testPartyId,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `삭제된 게스트는 목록에서 제외됨 테스트`() {
        // Given
        val guest1 = partyGuestService.join(
            PartyGuestJoinParam(
                code = testInviteCode,
                nickname = "남는게스트",
            )
        )
        val guest2 = partyGuestService.join(
            PartyGuestJoinParam(
                code = testInviteCode,
                nickname = "삭제게스트",
            )
        )
        partyGuestService.delete(
            PartyGuestDeleteParam(
                id = guest2.guestId,
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // When
        val guests = partyGuestService.list(
            PartyGuestListParam(
                partyId = testPartyId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(guests).hasSize(1)
        assertThat(guests[0].nickname).isEqualTo("남는게스트")
    }

    @Test
    fun `존재하지 않는 게스트 삭제시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            partyGuestService.delete(
                PartyGuestDeleteParam(
                    id = 999999L,
                    partyId = testPartyId,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }
}
