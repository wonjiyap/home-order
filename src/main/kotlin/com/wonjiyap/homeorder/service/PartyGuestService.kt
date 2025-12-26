package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.PartyGuestEntity
import com.wonjiyap.homeorder.enums.PartyStatus
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.repository.InviteCodeRepository
import com.wonjiyap.homeorder.repository.PartyGuestRepository
import com.wonjiyap.homeorder.repository.PartyRepository
import com.wonjiyap.homeorder.repository.dto.InviteCodeFetchOneParam
import com.wonjiyap.homeorder.repository.dto.PartyFetchOneParam
import com.wonjiyap.homeorder.repository.dto.PartyGuestFetchOneParam
import com.wonjiyap.homeorder.repository.dto.PartyGuestFetchParam
import com.wonjiyap.homeorder.service.dto.PartyGuestDeleteParam
import com.wonjiyap.homeorder.service.dto.PartyGuestGetParam
import com.wonjiyap.homeorder.service.dto.PartyGuestJoinParam
import com.wonjiyap.homeorder.service.dto.PartyGuestJoinResult
import com.wonjiyap.homeorder.service.dto.PartyGuestListParam
import com.wonjiyap.homeorder.service.dto.PartyGuestUpdateParam
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PartyGuestService(
    private val partyGuestRepository: PartyGuestRepository,
    private val inviteCodeRepository: InviteCodeRepository,
    private val partyRepository: PartyRepository,
) {

    fun join(param: PartyGuestJoinParam): PartyGuestJoinResult = transaction {
        // 초대 코드 조회 (대소문자 무시)
        val inviteCode = inviteCodeRepository.fetchOne(
            InviteCodeFetchOneParam(
                code = param.code.uppercase(),
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "유효하지 않은 초대 코드입니다")

        // 초대 코드 유효성 검증
        if (!inviteCode.isValid()) {
            throw HomeOrderException(ErrorCode.BAD_REQUEST, "만료되었거나 비활성화된 초대 코드입니다")
        }

        // 파티 조회
        val party = partyRepository.fetchOne(
            PartyFetchOneParam(
                id = inviteCode.partyId,
                deleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "파티를 찾을 수 없습니다")

        // 파티 상태 검증 (OPEN 상태만 참여 가능)
        if (party.status != PartyStatus.OPEN) {
            throw HomeOrderException(ErrorCode.BAD_REQUEST, "현재 참여할 수 없는 파티입니다")
        }

        // 닉네임 중복 검증
        val existingGuest = partyGuestRepository.fetchOne(
            PartyGuestFetchOneParam(
                partyId = party.id.value,
                nickname = param.nickname,
                withDeleted = false,
            )
        )
        if (existingGuest != null) {
            throw HomeOrderException(ErrorCode.CONFLICT, "이미 사용 중인 닉네임입니다")
        }

        // 게스트 생성
        val guest = PartyGuestEntity.new {
            partyId = party.id.value
            nickname = param.nickname
            isBlocked = false
            joinedAt = Instant.now()
        }

        PartyGuestJoinResult(
            guestId = guest.id.value,
            partyId = party.id.value,
            partyName = party.name,
            nickname = guest.nickname,
        )
    }

    fun list(param: PartyGuestListParam): List<PartyGuestEntity> {
        validatePartyOwnership(param.partyId, param.hostId)
        return partyGuestRepository.fetch(
            PartyGuestFetchParam(
                partyId = param.partyId,
                withDeleted = false,
            )
        )
    }

    fun get(param: PartyGuestGetParam): PartyGuestEntity {
        validatePartyOwnership(param.partyId, param.hostId)
        return partyGuestRepository.fetchOne(
            PartyGuestFetchOneParam(
                id = param.id,
                partyId = param.partyId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "게스트를 찾을 수 없습니다")
    }

    fun update(param: PartyGuestUpdateParam): PartyGuestEntity = transaction {
        validatePartyOwnership(param.partyId, param.hostId)

        val guest = partyGuestRepository.fetchOne(
            PartyGuestFetchOneParam(
                id = param.id,
                partyId = param.partyId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "게스트를 찾을 수 없습니다")

        // 닉네임 변경 시 중복 검증
        param.nickname?.let { newNickname ->
            if (guest.nickname != newNickname) {
                val existingGuest = partyGuestRepository.fetchOne(
                    PartyGuestFetchOneParam(
                        partyId = param.partyId,
                        nickname = newNickname,
                        withDeleted = false,
                    )
                )
                if (existingGuest != null) {
                    throw HomeOrderException(ErrorCode.CONFLICT, "이미 사용 중인 닉네임입니다")
                }
            }
            guest.nickname = newNickname
        }
        param.isBlocked?.let { guest.isBlocked = it }

        guest
    }

    fun delete(param: PartyGuestDeleteParam) = transaction {
        validatePartyOwnership(param.partyId, param.hostId)

        val guest = partyGuestRepository.fetchOne(
            PartyGuestFetchOneParam(
                id = param.id,
                partyId = param.partyId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "게스트를 찾을 수 없습니다")

        guest.deletedAt = Instant.now()
    }

    private fun validatePartyOwnership(partyId: Long, hostId: Long) {
        partyRepository.fetchOne(
            PartyFetchOneParam(
                id = partyId,
                hostId = hostId,
                deleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "파티를 찾을 수 없습니다")
    }
}
