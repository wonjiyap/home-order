package com.wonjiyap.homeorder.service

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
}
