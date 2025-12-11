package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.PartyGuestEntity
import com.wonjiyap.homeorder.domain.UserEntity
import com.wonjiyap.homeorder.enums.PartyStatus
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.service.dto.CreatePartyParam
import com.wonjiyap.homeorder.service.dto.DeletePartyParam
import com.wonjiyap.homeorder.service.dto.GetPartyParam
import com.wonjiyap.homeorder.service.dto.ListPartyParam
import com.wonjiyap.homeorder.service.dto.UpdatePartyParam
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
class PartyServiceTest {

    @Autowired
    private lateinit var partyService: PartyService

    private var testUserId: Long = 0

    @BeforeEach
    fun setUp() {
        testUserId = transaction {
            UserEntity.new {
                loginId = "partytest_${System.nanoTime()}"
                password = "password123"
                nickname = "파티테스트유저"
            }.id.value
        }
    }

    @Test
    fun `파티 생성 테스트`() {
        // Given
        val param = CreatePartyParam(
            hostId = testUserId,
            name = "테스트 파티",
            description = "테스트 설명",
            location = "서울시 강남구",
        )

        // When
        val party = partyService.create(param)

        // Then
        assertThat(party.id.value).isGreaterThan(0)
        assertThat(party.hostId).isEqualTo(testUserId)
        assertThat(party.name).isEqualTo("테스트 파티")
        assertThat(party.description).isEqualTo("테스트 설명")
        assertThat(party.location).isEqualTo("서울시 강남구")
        assertThat(party.status).isEqualTo(PartyStatus.PLANNING)
        assertThat(party.deletedAt).isNull()
    }

    @Test
    fun `내 파티 목록 조회 테스트`() {
        // Given
        partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "첫번째 파티",
            )
        )
        partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "두번째 파티",
            )
        )

        // When
        val parties = partyService.list(
            ListPartyParam(hostId = testUserId)
        )

        // Then
        assertThat(parties).hasSize(2)
        assertThat(parties.map { it.name }).containsExactlyInAnyOrder("첫번째 파티", "두번째 파티")
    }

    @Test
    fun `파티 이름으로 검색 테스트`() {
        // Given
        partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "생일 파티",
            )
        )
        partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "집들이 파티",
            )
        )
        partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "송년회",
            )
        )

        // When
        val parties = partyService.list(
            ListPartyParam(
                hostId = testUserId,
                name = "파티",
            )
        )

        // Then
        assertThat(parties).hasSize(2)
        assertThat(parties.map { it.name }).containsExactlyInAnyOrder("생일 파티", "집들이 파티")
    }

    @Test
    fun `파티 상태로 검색 테스트`() {
        // Given
        val party1 = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "준비중 파티",
            )
        )
        val party2 = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "오픈 파티",
            )
        )
        partyService.update(
            UpdatePartyParam(
                id = party2.id.value,
                hostId = testUserId,
                status = PartyStatus.OPEN,
            )
        )

        // When
        val openParties = partyService.list(
            ListPartyParam(
                hostId = testUserId,
                status = PartyStatus.OPEN,
            )
        )

        // Then
        assertThat(openParties).hasSize(1)
        assertThat(openParties[0].name).isEqualTo("오픈 파티")
    }

    @Test
    fun `다른 사용자 파티는 조회되지 않음 테스트`() {
        // Given
        val otherUserId = transaction {
            UserEntity.new {
                loginId = "otheruser_${System.nanoTime()}"
                password = "password123"
                nickname = "다른유저"
            }.id.value
        }
        partyService.create(
            CreatePartyParam(
                hostId = otherUserId,
                name = "다른 사용자 파티",
            )
        )

        // When
        val parties = partyService.list(
            ListPartyParam(hostId = testUserId)
        )

        // Then
        assertThat(parties).isEmpty()
    }

    @Test
    fun `파티 상세 조회 테스트`() {
        // Given
        val created = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "상세 조회 파티",
                description = "상세 설명",
            )
        )

        // When
        val party = partyService.get(
            GetPartyParam(
                id = created.id.value,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(party.id.value).isEqualTo(created.id.value)
        assertThat(party.name).isEqualTo("상세 조회 파티")
    }

    @Test
    fun `존재하지 않는 파티 조회시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            partyService.get(
                GetPartyParam(
                    id = 999999L,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("파티를 찾을 수 없습니다")
    }

    @Test
    fun `다른 사용자 파티 조회시 예외 발생 테스트`() {
        // Given
        val otherUserId = transaction {
            UserEntity.new {
                loginId = "otheruser2_${System.nanoTime()}"
                password = "password123"
                nickname = "다른유저2"
            }.id.value
        }
        val party = partyService.create(
            CreatePartyParam(
                hostId = otherUserId,
                name = "다른 사용자 파티",
            )
        )

        // When & Then
        assertThatThrownBy {
            partyService.get(
                GetPartyParam(
                    id = party.id.value,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `파티 수정 테스트`() {
        // Given
        val created = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "수정 전 파티",
            )
        )

        // When
        val updated = partyService.update(
            UpdatePartyParam(
                id = created.id.value,
                hostId = testUserId,
                name = "수정 후 파티",
                description = "새로운 설명",
                location = "부산시 해운대구",
            )
        )

        // Then
        assertThat(updated.name).isEqualTo("수정 후 파티")
        assertThat(updated.description).isEqualTo("새로운 설명")
        assertThat(updated.location).isEqualTo("부산시 해운대구")
    }

    @Test
    fun `파티 상태 변경 테스트`() {
        // Given
        val created = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "상태 변경 파티",
            )
        )
        assertThat(created.status).isEqualTo(PartyStatus.PLANNING)

        // When
        val updated = partyService.update(
            UpdatePartyParam(
                id = created.id.value,
                hostId = testUserId,
                status = PartyStatus.OPEN,
            )
        )

        // Then
        assertThat(updated.status).isEqualTo(PartyStatus.OPEN)
    }

    @Test
    fun `파티 부분 수정시 null 필드는 변경되지 않음 테스트`() {
        // Given
        val created = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "원래 이름",
                description = "원래 설명",
                location = "원래 장소",
            )
        )

        // When - 이름만 수정
        val updated = partyService.update(
            UpdatePartyParam(
                id = created.id.value,
                hostId = testUserId,
                name = "새 이름",
            )
        )

        // Then
        assertThat(updated.name).isEqualTo("새 이름")
        assertThat(updated.description).isEqualTo("원래 설명") // 변경 안됨
        assertThat(updated.location).isEqualTo("원래 장소") // 변경 안됨
    }

    @Test
    fun `파티 삭제 테스트 (Soft Delete)`() {
        // Given
        val created = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "삭제할 파티",
            )
        )

        // When
        partyService.delete(
            DeletePartyParam(
                id = created.id.value,
                hostId = testUserId,
            )
        )

        // Then - 삭제된 파티는 조회되지 않음
        assertThatThrownBy {
            partyService.get(
                GetPartyParam(
                    id = created.id.value,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `삭제된 파티는 목록에서 제외됨 테스트`() {
        // Given
        val party1 = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "남아있는 파티",
            )
        )
        val party2 = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "삭제될 파티",
            )
        )
        partyService.delete(
            DeletePartyParam(
                id = party2.id.value,
                hostId = testUserId,
            )
        )

        // When
        val parties = partyService.list(
            ListPartyParam(hostId = testUserId)
        )

        // Then
        assertThat(parties).hasSize(1)
        assertThat(parties[0].name).isEqualTo("남아있는 파티")
    }

    // ========== 검증 테스트 ==========

    @Test
    fun `생성시 과거 날짜면 예외 발생 테스트`() {
        // Given
        val pastDate = Instant.now().minus(1, ChronoUnit.DAYS)

        // When & Then
        assertThatThrownBy {
            partyService.create(
                CreatePartyParam(
                    hostId = testUserId,
                    name = "과거 날짜 파티",
                    date = pastDate,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST)
    }

    @Test
    fun `생성시 날짜가 null이면 날짜 검증 스킵 테스트`() {
        // Given & When
        val party = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "날짜 없는 파티",
                date = null,
            )
        )

        // Then
        assertThat(party.date).isNull()
    }

    @Test
    fun `생성시 같은 날짜와 이름 중복이면 예외 발생 테스트`() {
        // Given
        val futureDate = Instant.now().plus(7, ChronoUnit.DAYS)
        partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "중복 파티",
                date = futureDate,
            )
        )

        // When & Then
        assertThatThrownBy {
            partyService.create(
                CreatePartyParam(
                    hostId = testUserId,
                    name = "중복 파티",
                    date = futureDate,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT)
    }

    @Test
    fun `생성시 같은 이름이지만 다른 날짜면 중복 아님 테스트`() {
        // Given
        val date1 = Instant.now().plus(7, ChronoUnit.DAYS)
        val date2 = Instant.now().plus(14, ChronoUnit.DAYS)
        partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "같은 이름 파티",
                date = date1,
            )
        )

        // When
        val party2 = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "같은 이름 파티",
                date = date2,
            )
        )

        // Then
        assertThat(party2.name).isEqualTo("같은 이름 파티")
    }

    @Test
    fun `생성시 날짜가 null이면 중복 검증 스킵 테스트`() {
        // Given
        partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "날짜 없는 파티",
                date = null,
            )
        )

        // When - 같은 이름이지만 날짜가 null이면 중복 검증 안 함
        val party2 = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "날짜 없는 파티",
                date = null,
            )
        )

        // Then
        assertThat(party2.name).isEqualTo("날짜 없는 파티")
    }

    @Test
    fun `생성시 취소된 파티와 같은 이름과 날짜면 중복 아님 테스트`() {
        // Given
        val futureDate = Instant.now().plus(7, ChronoUnit.DAYS)
        val party1 = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "취소될 파티",
                date = futureDate,
            )
        )
        partyService.update(
            UpdatePartyParam(
                id = party1.id.value,
                hostId = testUserId,
                status = PartyStatus.CANCELLED,
            )
        )

        // When - 취소된 파티는 중복 대상에서 제외
        val party2 = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "취소될 파티",
                date = futureDate,
            )
        )

        // Then
        assertThat(party2.name).isEqualTo("취소될 파티")
    }

    @Test
    fun `수정시 날짜 변경될 때만 날짜 검증 테스트`() {
        // Given
        val futureDate = Instant.now().plus(7, ChronoUnit.DAYS)
        val party = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "수정 테스트 파티",
                date = futureDate,
            )
        )

        // When - 이름만 수정 (날짜 변경 안 함)
        val updated = partyService.update(
            UpdatePartyParam(
                id = party.id.value,
                hostId = testUserId,
                name = "이름만 변경",
            )
        )

        // Then
        assertThat(updated.name).isEqualTo("이름만 변경")
    }

    @Test
    fun `수정시 과거 날짜로 변경하면 예외 발생 테스트`() {
        // Given
        val futureDate = Instant.now().plus(7, ChronoUnit.DAYS)
        val party = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "날짜 변경 파티",
                date = futureDate,
            )
        )
        val pastDate = Instant.now().minus(1, ChronoUnit.DAYS)

        // When & Then
        assertThatThrownBy {
            partyService.update(
                UpdatePartyParam(
                    id = party.id.value,
                    hostId = testUserId,
                    date = pastDate,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST)
    }

    @Test
    fun `수정시 중복 검증 자기 자신 제외 테스트`() {
        // Given
        val futureDate = Instant.now().plus(7, ChronoUnit.DAYS)
        val party = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "자기 자신 파티",
                date = futureDate,
            )
        )

        // When - 같은 이름과 날짜로 수정 (자기 자신)
        val updated = partyService.update(
            UpdatePartyParam(
                id = party.id.value,
                hostId = testUserId,
                name = "자기 자신 파티",
                date = futureDate,
            )
        )

        // Then
        assertThat(updated.name).isEqualTo("자기 자신 파티")
    }

    @Test
    fun `CANCELLED 상태에서 다른 상태로 변경시 예외 발생 테스트`() {
        // Given
        val party = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "취소 상태 파티",
            )
        )
        partyService.update(
            UpdatePartyParam(
                id = party.id.value,
                hostId = testUserId,
                status = PartyStatus.CANCELLED,
            )
        )

        // When & Then
        assertThatThrownBy {
            partyService.update(
                UpdatePartyParam(
                    id = party.id.value,
                    hostId = testUserId,
                    status = PartyStatus.OPEN,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST)
    }

    @Test
    fun `PLANNING에서 CLOSED로 직접 변경시 예외 발생 테스트`() {
        // Given
        val party = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "상태 전이 파티",
            )
        )

        // When & Then
        assertThatThrownBy {
            partyService.update(
                UpdatePartyParam(
                    id = party.id.value,
                    hostId = testUserId,
                    status = PartyStatus.CLOSED,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST)
    }

    @Test
    fun `CLOSED에서 OPEN으로 재오픈 가능 테스트`() {
        // Given
        val party = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "재오픈 파티",
            )
        )
        partyService.update(
            UpdatePartyParam(
                id = party.id.value,
                hostId = testUserId,
                status = PartyStatus.OPEN,
            )
        )
        partyService.update(
            UpdatePartyParam(
                id = party.id.value,
                hostId = testUserId,
                status = PartyStatus.CLOSED,
            )
        )

        // When
        val reopened = partyService.update(
            UpdatePartyParam(
                id = party.id.value,
                hostId = testUserId,
                status = PartyStatus.OPEN,
            )
        )

        // Then
        assertThat(reopened.status).isEqualTo(PartyStatus.OPEN)
    }

    @Test
    fun `OPEN 상태에서 삭제시 예외 발생 테스트`() {
        // Given
        val party = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "오픈 상태 파티",
            )
        )
        partyService.update(
            UpdatePartyParam(
                id = party.id.value,
                hostId = testUserId,
                status = PartyStatus.OPEN,
            )
        )

        // When & Then
        assertThatThrownBy {
            partyService.delete(
                DeletePartyParam(
                    id = party.id.value,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST)
    }

    @Test
    fun `CANCELLED 상태면 삭제 가능 테스트`() {
        // Given
        val party = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "취소 후 삭제 파티",
            )
        )
        partyService.update(
            UpdatePartyParam(
                id = party.id.value,
                hostId = testUserId,
                status = PartyStatus.CANCELLED,
            )
        )

        // When
        partyService.delete(
            DeletePartyParam(
                id = party.id.value,
                hostId = testUserId,
            )
        )

        // Then - 삭제 후 조회 안 됨
        assertThatThrownBy {
            partyService.get(
                GetPartyParam(
                    id = party.id.value,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `PLANNING 상태 + 게스트 없으면 삭제 가능 테스트`() {
        // Given
        val party = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "게스트 없는 파티",
            )
        )

        // When
        partyService.delete(
            DeletePartyParam(
                id = party.id.value,
                hostId = testUserId,
            )
        )

        // Then
        assertThatThrownBy {
            partyService.get(
                GetPartyParam(
                    id = party.id.value,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `PLANNING 상태 + 게스트 있으면 삭제 불가 테스트`() {
        // Given
        val party = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "게스트 있는 파티",
            )
        )

        // 게스트 추가
        transaction {
            PartyGuestEntity.new {
                partyId = party.id.value
                nickname = "테스트게스트"
                isBlocked = false
            }
        }

        // When & Then
        assertThatThrownBy {
            partyService.delete(
                DeletePartyParam(
                    id = party.id.value,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST)
    }

    @Test
    fun `게스트가 있어도 CANCELLED 상태면 삭제 가능 테스트`() {
        // Given
        val party = partyService.create(
            CreatePartyParam(
                hostId = testUserId,
                name = "취소된 게스트 있는 파티",
            )
        )

        // 게스트 추가
        transaction {
            PartyGuestEntity.new {
                partyId = party.id.value
                nickname = "테스트게스트"
                isBlocked = false
            }
        }

        // 취소
        partyService.update(
            UpdatePartyParam(
                id = party.id.value,
                hostId = testUserId,
                status = PartyStatus.CANCELLED,
            )
        )

        // When
        partyService.delete(
            DeletePartyParam(
                id = party.id.value,
                hostId = testUserId,
            )
        )

        // Then
        assertThatThrownBy {
            partyService.get(
                GetPartyParam(
                    id = party.id.value,
                    hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }
}
