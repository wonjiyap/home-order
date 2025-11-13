package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.PartyEntity
import com.wonjiyap.homeorder.enums.PartyStatus
import com.wonjiyap.homeorder.repository.dto.PartyFetchOneParam
import com.wonjiyap.homeorder.repository.dto.PartyFetchParam
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest
@Transactional
@Rollback
class PartyRepositoryTest {

    private lateinit var partyRepository: PartyRepository

    @BeforeEach
    fun setUp() {
        partyRepository = PartyRepository()
    }

    @Test
    fun `ID로 단일 파티 조회 테스트`() {
        // Given
        val party = transaction {
            PartyEntity.new {
                hostId = 123L
                name = "테스트 파티"
                description = "단일 조회 테스트"
                status = PartyStatus.PLANNING
            }
        }
        val partyId = party.id.value

        // When
        val param = PartyFetchOneParam(
            id = partyId
        )
        val foundParty = partyRepository.fetchOne(param)

        // Then
        assertThat(foundParty).isNotNull()
        assertThat(foundParty?.name).isEqualTo("테스트 파티")
        assertThat(foundParty?.hostId).isEqualTo(123L)
        assertThat(foundParty?.status).isEqualTo(PartyStatus.PLANNING)
    }

    @Test
    fun `존재하지 않는 ID로 조회시 null 반환 테스트`() {
        // Given
        val nonExistentId = 99999L

        // When
        val param = PartyFetchOneParam(
            id = nonExistentId
        )
        val foundParty = partyRepository.fetchOne(param)

        // Then
        assertThat(foundParty).isNull()
    }

    @Test
    fun `hostId로 파티 목록 조회 테스트`() {
        // Given
        val hostId = 456L
        transaction {
            PartyEntity.new {
                this.hostId = hostId
                name = "호스트 파티 1"
                status = PartyStatus.PLANNING
            }
            PartyEntity.new {
                this.hostId = hostId
                name = "호스트 파티 2"
                status = PartyStatus.OPEN
            }
            PartyEntity.new {
                this.hostId = 789L  // 다른 호스트
                name = "다른 호스트 파티"
                status = PartyStatus.OPEN
            }
        }

        // When
        val param = PartyFetchParam(
            hostId = hostId
        )
        val parties = partyRepository.fetch(param)

        // Then
        assertThat(parties).hasSize(2)
        assertThat(parties.map { it.name }).containsExactlyInAnyOrder(
            "호스트 파티 1",
            "호스트 파티 2"
        )
        assertThat(parties.all { it.hostId == hostId }).isTrue()
    }

    @Test
    fun `파티명으로 검색 테스트`() {
        // Given
        transaction {
            PartyEntity.new {
                hostId = 100L
                name = "생일 파티"
                status = PartyStatus.PLANNING
            }
            PartyEntity.new {
                hostId = 200L
                name = "크리스마스 파티"
                status = PartyStatus.OPEN
            }
            PartyEntity.new {
                hostId = 300L
                name = "회사 파티"
                status = PartyStatus.OPEN
            }
        }

        // When
        val param = PartyFetchParam(
            name = "생일 파티"
        )
        val parties = partyRepository.fetch(param)

        // Then
        assertThat(parties).hasSize(1)
        assertThat(parties.first().name).isEqualTo("생일 파티")
        assertThat(parties.first().hostId).isEqualTo(100L)
    }

    @Test
    fun `파티 상태로 검색 테스트`() {
        // Given
        transaction {
            PartyEntity.new {
                hostId = 111L
                name = "기획중 파티"
                status = PartyStatus.PLANNING
            }
            PartyEntity.new {
                hostId = 222L
                name = "오픈 파티 1"
                status = PartyStatus.OPEN
            }
            PartyEntity.new {
                hostId = 333L
                name = "오픈 파티 2"
                status = PartyStatus.OPEN
            }
            PartyEntity.new {
                hostId = 444L
                name = "마감 파티"
                status = PartyStatus.CLOSED
            }
        }

        // When
        val param = PartyFetchParam(
            status = PartyStatus.OPEN
        )
        val parties = partyRepository.fetch(param)

        // Then
        assertThat(parties).hasSize(2)
        assertThat(parties.map { it.name }).containsExactlyInAnyOrder(
            "오픈 파티 1",
            "오픈 파티 2"
        )
        assertThat(parties.all { it.status == PartyStatus.OPEN }).isTrue()
    }

    @Test
    fun `날짜 범위로 검색 테스트`() {
        // Given
        val baseDate = Instant.parse("2025-07-20T10:00:00Z")
        val beforeDate = baseDate.minus(1, ChronoUnit.DAYS)
        val afterDate = baseDate.plus(1, ChronoUnit.DAYS)

        transaction {
            PartyEntity.new {
                hostId = 111L
                name = "어제 파티"
                date = beforeDate
                status = PartyStatus.PLANNING
            }
            PartyEntity.new {
                hostId = 222L
                name = "오늘 파티"
                date = baseDate
                status = PartyStatus.OPEN
            }
            PartyEntity.new {
                hostId = 333L
                name = "내일 파티"
                date = afterDate
                status = PartyStatus.PLANNING
            }
            PartyEntity.new {
                hostId = 444L
                name = "날짜 없는 파티"
                date = null
                status = PartyStatus.PLANNING
            }
        }

        // When - 오늘부터 내일까지
        val param = PartyFetchParam(
            dateFrom = baseDate,
            dateTo = afterDate
        )
        val parties = partyRepository.fetch(param)

        // Then
        assertThat(parties).hasSize(2)
        assertThat(parties.map { it.name }).containsExactlyInAnyOrder(
            "오늘 파티",
            "내일 파티"
        )
    }

    @Test
    fun `날짜 시작점만 지정한 검색 테스트`() {
        // Given
        val baseDate = Instant.parse("2025-07-20T10:00:00Z")
        val beforeDate = baseDate.minus(1, ChronoUnit.DAYS)
        val afterDate = baseDate.plus(1, ChronoUnit.DAYS)

        transaction {
            PartyEntity.new {
                hostId = 111L
                name = "어제 파티"
                date = beforeDate
                status = PartyStatus.PLANNING
            }
            PartyEntity.new {
                hostId = 222L
                name = "오늘 파티"
                date = baseDate
                status = PartyStatus.OPEN
            }
            PartyEntity.new {
                hostId = 333L
                name = "내일 파티"
                date = afterDate
                status = PartyStatus.PLANNING
            }
        }

        // When - 오늘 이후 파티들
        val param = PartyFetchParam(
            dateFrom = baseDate
        )
        val parties = partyRepository.fetch(param)

        // Then
        assertThat(parties).hasSize(2)
        assertThat(parties.map { it.name }).containsExactlyInAnyOrder(
            "오늘 파티",
            "내일 파티"
        )
    }

    @Test
    fun `복합 조건 검색 테스트`() {
        // Given
        val hostId = 555L
        val targetDate = Instant.parse("2025-07-25T15:00:00Z")

        transaction {
            PartyEntity.new {
                this.hostId = hostId
                name = "조건 맞는 파티"
                date = targetDate
                status = PartyStatus.OPEN
            }
            PartyEntity.new {
                this.hostId = hostId
                name = "상태 다른 파티"
                date = targetDate
                status = PartyStatus.CLOSED  // 상태가 다름
            }
            PartyEntity.new {
                this.hostId = 666L  // 호스트가 다름
                name = "호스트 다른 파티"
                date = targetDate
                status = PartyStatus.OPEN
            }
        }

        // When
        val param = PartyFetchParam(
            hostId = hostId,
            status = PartyStatus.OPEN,
            dateFrom = targetDate.minus(1, ChronoUnit.HOURS),
            dateTo = targetDate.plus(1, ChronoUnit.HOURS)
        )
        val parties = partyRepository.fetch(param)

        // Then
        assertThat(parties).hasSize(1)
        assertThat(parties.first().name).isEqualTo("조건 맞는 파티")
        assertThat(parties.first().hostId).isEqualTo(hostId)
        assertThat(parties.first().status).isEqualTo(PartyStatus.OPEN)
    }

    @Test
    fun `조건 없이 전체 조회 테스트`() {
        // Given
        transaction {
            PartyEntity.new {
                hostId = 111L
                name = "파티 1"
                status = PartyStatus.PLANNING
            }
            PartyEntity.new {
                hostId = 222L
                name = "파티 2"
                status = PartyStatus.OPEN
            }
        }

        // When
        val param = PartyFetchParam()  // 모든 조건이 null
        val parties = partyRepository.fetch(param)

        // Then
        assertThat(parties).hasSizeGreaterThanOrEqualTo(2)
    }

    @Test
    fun `파티 저장 테스트`() {
        // Given
        val party = transaction {
            PartyEntity.new {
                hostId = 777L
                name = "저장 테스트 파티"
                description = "저장이 잘 되는지 테스트"
                location = "서울 강남구"
                status = PartyStatus.PLANNING
            }
        }

        // When
        assertDoesNotThrow {
            partyRepository.save(party)
        }

        // Then - 실제로 저장되었는지 확인
        val param = PartyFetchOneParam(
            id = party.id.value
        )
        val savedParty = partyRepository.fetchOne(param)
        assertThat(savedParty).isNotNull()
        assertThat(savedParty?.name).isEqualTo("저장 테스트 파티")
        assertThat(savedParty?.description).isEqualTo("저장이 잘 되는지 테스트")
        assertThat(savedParty?.location).isEqualTo("서울 강남구")
    }

    @Test
    fun `파티 정보 수정 후 저장 테스트`() {
        // Given
        val party = transaction {
            PartyEntity.new {
                hostId = 888L
                name = "수정 전 파티"
                description = "수정 전 설명"
                status = PartyStatus.PLANNING
            }
        }

        // When
        Thread.sleep(1) // updatedAt 차이를 위해
        party.name = "수정 후 파티"
        party.description = "수정 후 설명"
        party.status = PartyStatus.OPEN
        party.updatedAt = Instant.now()

        partyRepository.save(party)

        // Then
        val param = PartyFetchOneParam(
            id = party.id.value
        )
        val updatedParty = partyRepository.fetchOne(param)

        assertThat(updatedParty?.name).isEqualTo("수정 후 파티")
        assertThat(updatedParty?.description).isEqualTo("수정 후 설명")
        assertThat(updatedParty?.status).isEqualTo(PartyStatus.OPEN)
        assertThat(updatedParty?.hostId).isEqualTo(888L) // 변경되지 않음
    }
}