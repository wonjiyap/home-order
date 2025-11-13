package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.PartyGuestEntity
import com.wonjiyap.homeorder.repository.dto.PartyGuestFetchOneParam
import com.wonjiyap.homeorder.repository.dto.PartyGuestFetchParam
import org.assertj.core.api.Assertions.assertThat
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
class PartyGuestRepositoryTest {

    @Autowired
    private lateinit var partyGuestRepository: PartyGuestRepository

    @BeforeEach
    fun setUp() {
        partyGuestRepository = PartyGuestRepository()
    }

    @Test
    fun `파티 게스트 생성 및 저장 테스트`() {
        // Given & When
        val guest = transaction {
            PartyGuestEntity.new {
                partyId = 1L
                nickname = "테스트유저"
                isBlocked = false
            }
        }
        partyGuestRepository.save(guest)

        // Then
        val param = PartyGuestFetchOneParam(
            id = guest.id.value
        )
        val foundGuest = partyGuestRepository.fetchOne(param)

        assertThat(foundGuest).isNotNull()
        assertThat(foundGuest?.nickname).isEqualTo("테스트유저")
        assertThat(foundGuest?.isBlocked).isFalse()
    }

    @Test
    fun `파티 ID로 게스트 목록 조회 테스트`() {
        // Given
        transaction {
            PartyGuestEntity.new {
                partyId = 1L
                nickname = "유저1"
                isBlocked = false
            }
            PartyGuestEntity.new {
                partyId = 1L
                nickname = "유저2"
                isBlocked = false
            }
            PartyGuestEntity.new {
                partyId = 2L
                nickname = "유저3"
                isBlocked = false
            }
        }

        // When
        val param = PartyGuestFetchParam(
            partyId = 1L,
        )
        val guests = partyGuestRepository.fetch(param)

        // Then
        assertThat(guests).hasSize(2)
        assertThat(guests.map { it.nickname }).containsExactlyInAnyOrder("유저1", "유저2")
    }

    @Test
    fun `차단 여부로 게스트 목록 조회 테스트`() {
        // Given
        transaction {
            PartyGuestEntity.new {
                partyId = 1L
                nickname = "일반유저"
                isBlocked = false
            }
            PartyGuestEntity.new {
                partyId = 1L
                nickname = "차단유저"
                isBlocked = true
            }
        }

        // When
        val param = PartyGuestFetchParam(
            partyId = 1L,
            isBlocked = true
        )
        val blockedGuests = partyGuestRepository.fetch(param)

        // Then
        assertThat(blockedGuests).hasSize(1)
        assertThat(blockedGuests.first().nickname).isEqualTo("차단유저")
        assertThat(blockedGuests.first().isBlocked).isTrue()
    }

    @Test
    fun `파티 ID와 차단 여부 조합 조회 테스트`() {
        // Given
        transaction {
            PartyGuestEntity.new {
                partyId = 1L
                nickname = "파티1-일반"
                isBlocked = false
            }
            PartyGuestEntity.new {
                partyId = 1L
                nickname = "파티1-차단"
                isBlocked = true
            }
            PartyGuestEntity.new {
                partyId = 2L
                nickname = "파티2-차단"
                isBlocked = true
            }
        }

        // When
        val param = PartyGuestFetchParam(
            partyId = 1L,
            isBlocked = false
        )
        val guests = partyGuestRepository.fetch(param)

        // Then
        assertThat(guests).hasSize(1)
        assertThat(guests.first().nickname).isEqualTo("파티1-일반")
    }

    @Test
    fun `ID로 단일 게스트 조회 테스트`() {
        // Given
        val guest = transaction {
            PartyGuestEntity.new {
                partyId = 1L
                nickname = "테스트유저"
                isBlocked = false
            }
        }

        // When
        val param = PartyGuestFetchOneParam(id = guest.id.value)
        val foundGuest = partyGuestRepository.fetchOne(param)

        // Then
        assertThat(foundGuest).isNotNull()
        assertThat(foundGuest?.id?.value).isEqualTo(guest.id.value)
        assertThat(foundGuest?.nickname).isEqualTo("테스트유저")
    }

    @Test
    fun `파티 ID와 닉네임으로 게스트 조회 테스트`() {
        // Given
        transaction {
            PartyGuestEntity.new {
                partyId = 1L
                nickname = "찾을유저"
                isBlocked = false
            }
            PartyGuestEntity.new {
                partyId = 1L
                nickname = "다른유저"
                isBlocked = false
            }
            PartyGuestEntity.new {
                partyId = 2L
                nickname = "찾을유저"
                isBlocked = false
            }
        }

        // When
        val param = PartyGuestFetchOneParam(
            partyId = 1L,
            nickname = "찾을유저"
        )
        val foundGuest = partyGuestRepository.fetchOne(param)

        // Then
        assertThat(foundGuest).isNotNull()
        assertThat(foundGuest?.partyId).isEqualTo(1L)
        assertThat(foundGuest?.nickname).isEqualTo("찾을유저")
    }

    @Test
    fun `존재하지 않는 게스트 조회 시 null 반환 테스트`() {
        // When
        val param = PartyGuestFetchOneParam(id = 9999L)
        val foundGuest = partyGuestRepository.fetchOne(param)

        // Then
        assertThat(foundGuest).isNull()
    }

    @Test
    fun `게스트 정보 수정 후 저장 테스트`() {
        // Given
        val guest = transaction {
            PartyGuestEntity.new {
                partyId = 1L
                nickname = "원래유저"
                isBlocked = false
            }
        }

        // When
        transaction {
            guest.nickname = "수정된유저"
            guest.isBlocked = true
        }
        partyGuestRepository.save(guest)

        // Then
        val param = PartyGuestFetchOneParam(id = guest.id.value)
        val foundGuest = partyGuestRepository.fetchOne(param)

        assertThat(foundGuest?.nickname).isEqualTo("수정된유저")
        assertThat(foundGuest?.isBlocked).isTrue()
    }

    @Test
    fun `게스트 soft delete 테스트`() {
        // Given
        val guest = transaction {
            PartyGuestEntity.new {
                partyId = 1L
                nickname = "삭제될유저"
                isBlocked = false
            }
        }
        val guestId = guest.id.value

        // When - deletedAt 설정하고 save
        transaction {
            guest.deletedAt = Instant.now()
        }
        partyGuestRepository.save(guest)

        // Then - fetch에서 조회되지 않음
        val param = PartyGuestFetchOneParam(id = guestId)
        val foundGuest = partyGuestRepository.fetchOne(param)
        assertThat(foundGuest).isNull()

        // 하지만 실제로는 DB에 존재함
        transaction {
            val entity = PartyGuestEntity.findById(guestId)
            assertThat(entity).isNotNull()
            assertThat(entity?.deletedAt).isNotNull()
        }
    }

    @Test
    fun `삭제된 게스트는 fetch 목록에서 제외되는지 테스트`() {
        // Given
        transaction {
            PartyGuestEntity.new {
                partyId = 1L
                nickname = "활성유저1"
                isBlocked = false
            }
            PartyGuestEntity.new {
                partyId = 1L
                nickname = "활성유저2"
                isBlocked = false
            }
            val deletedGuest = PartyGuestEntity.new {
                partyId = 1L
                nickname = "삭제유저"
                isBlocked = false
            }
            deletedGuest.deletedAt = Instant.now()
        }

        // When
        val param = PartyGuestFetchParam(partyId = 1L)
        val guests = partyGuestRepository.fetch(param)

        // Then
        assertThat(guests).hasSize(2)
        assertThat(guests.map { it.nickname }).containsExactlyInAnyOrder("활성유저1", "활성유저2")
    }

    @Test
    fun `모든 조건으로 단일 게스트 조회 테스트`() {
        // Given
        transaction {
            PartyGuestEntity.new {
                partyId = 1L
                nickname = "타겟유저"
                isBlocked = true
            }
            PartyGuestEntity.new {
                partyId = 1L
                nickname = "다른유저"
                isBlocked = false
            }
        }

        // When
        val param = PartyGuestFetchOneParam(
            partyId = 1L,
            nickname = "타겟유저",
            isBlocked = true
        )
        val foundGuest = partyGuestRepository.fetchOne(param)

        // Then
        assertThat(foundGuest).isNotNull()
        assertThat(foundGuest?.nickname).isEqualTo("타겟유저")
        assertThat(foundGuest?.isBlocked).isTrue()
    }

    @Test
    fun `조건 없이 fetch 호출 시 모든 게스트 조회 테스트`() {
        // Given
        transaction {
            PartyGuestEntity.new {
                partyId = 1L
                nickname = "유저1"
                isBlocked = false
            }
            PartyGuestEntity.new {
                partyId = 2L
                nickname = "유저2"
                isBlocked = true
            }
        }

        // When
        val param = PartyGuestFetchParam()
        val guests = partyGuestRepository.fetch(param)

        // Then
        assertThat(guests).hasSize(2)
    }
}