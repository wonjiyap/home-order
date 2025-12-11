package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.PartyEntity
import com.wonjiyap.homeorder.enums.PartyStatus
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.repository.PartyGuestRepository
import com.wonjiyap.homeorder.repository.PartyRepository
import com.wonjiyap.homeorder.repository.dto.PartyFetchOneParam
import com.wonjiyap.homeorder.repository.dto.PartyFetchParam
import com.wonjiyap.homeorder.repository.dto.PartyGuestFetchParam
import com.wonjiyap.homeorder.service.dto.CreatePartyParam
import com.wonjiyap.homeorder.service.dto.DeletePartyParam
import com.wonjiyap.homeorder.service.dto.GetPartyParam
import com.wonjiyap.homeorder.service.dto.ListPartyParam
import com.wonjiyap.homeorder.service.dto.UpdatePartyParam
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PartyService(
    private val partyRepository: PartyRepository,
    private val partyGuestRepository: PartyGuestRepository,
) {

    fun list(param: ListPartyParam): List<PartyEntity> {
        return partyRepository.fetch(
            PartyFetchParam(
                hostId = param.hostId,
                name = param.name,
                status = param.status,
                deleted = false,
            )
        )
    }

    fun get(param: GetPartyParam): PartyEntity {
        return partyRepository.fetchOne(
            PartyFetchOneParam(
                id = param.id,
                hostId = param.hostId,
                deleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "파티를 찾을 수 없습니다")
    }

    fun create(param: CreatePartyParam): PartyEntity = transaction {
        // 날짜 검증 (날짜가 있으면 현재 시간 이후여야 함)
        param.date?.let { validateDateIsFuture(it) }

        // 중복 검증 (날짜가 있을 때만)
        param.date?.let { date ->
            validateNoDuplicate(
                hostId = param.hostId,
                name = param.name,
                date = date,
                excludeId = null,
            )
        }

        PartyEntity.new {
            hostId = param.hostId
            name = param.name
            description = param.description
            date = param.date
            location = param.location
            status = PartyStatus.PLANNING
            createdAt = Instant.now()
        }
    }

    fun update(param: UpdatePartyParam): PartyEntity = transaction {
        val party = partyRepository.fetchOne(
            PartyFetchOneParam(
                id = param.id,
                hostId = param.hostId,
                deleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "파티를 찾을 수 없습니다")

        // 상태 전이 검증
        param.status?.let { newStatus ->
            validateStatusTransition(party.status, newStatus)
        }

        // 날짜 검증 (날짜가 변경될 때만)
        param.date?.let { newDate ->
            if (party.date != newDate) {
                validateDateIsFuture(newDate)
            }
        }

        // 중복 검증 (이름 또는 날짜가 변경될 때)
        val newName = param.name ?: party.name
        val newDate = param.date ?: party.date
        if (newDate != null && (param.name != null || param.date != null)) {
            validateNoDuplicate(
                hostId = param.hostId,
                name = newName,
                date = newDate,
                excludeId = party.id.value,
            )
        }

        param.name?.let { party.name = it }
        param.description?.let { party.description = it }
        param.date?.let { party.date = it }
        param.location?.let { party.location = it }
        param.status?.let { party.status = it }
        party.updatedAt = Instant.now()

        party
    }

    fun delete(param: DeletePartyParam) = transaction {
        val party = partyRepository.fetchOne(
            PartyFetchOneParam(
                id = param.id,
                hostId = param.hostId,
                deleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "파티를 찾을 수 없습니다")

        // 삭제 가능 여부 검증
        validateCanDelete(party)

        party.deletedAt = Instant.now()
    }

    private fun validateDateIsFuture(date: Instant) {
        if (date.isBefore(Instant.now())) {
            throw HomeOrderException(ErrorCode.BAD_REQUEST, "파티 날짜는 현재 시간 이후여야 합니다")
        }
    }

    private fun validateNoDuplicate(
        hostId: Long,
        name: String,
        date: Instant,
        excludeId: Long?,
    ) {
        val duplicate = partyRepository.fetchOne(
            PartyFetchOneParam(
                hostId = hostId,
                name = name,
                date = date,
                statusNot = PartyStatus.CANCELLED,
                excludeId = excludeId,
                deleted = false,
            )
        )
        if (duplicate != null) {
            throw HomeOrderException(ErrorCode.CONFLICT, "같은 날짜에 동일한 이름의 파티가 이미 존재합니다")
        }
    }

    private fun validateStatusTransition(currentStatus: PartyStatus, newStatus: PartyStatus) {
        if (currentStatus == newStatus) return

        val allowedTransitions = when (currentStatus) {
            PartyStatus.PLANNING -> setOf(PartyStatus.OPEN, PartyStatus.CANCELLED)
            PartyStatus.OPEN -> setOf(PartyStatus.CLOSED, PartyStatus.CANCELLED)
            PartyStatus.CLOSED -> setOf(PartyStatus.OPEN, PartyStatus.CANCELLED)
            PartyStatus.CANCELLED -> emptySet()
        }

        if (newStatus !in allowedTransitions) {
            throw HomeOrderException(
                ErrorCode.BAD_REQUEST,
                "${currentStatus.name}에서 ${newStatus.name}로 변경할 수 없습니다",
            )
        }
    }

    private fun validateCanDelete(party: PartyEntity) {
        // CANCELLED 상태면 삭제 가능
        if (party.status == PartyStatus.CANCELLED) return

        // PLANNING 상태 + 게스트 없으면 삭제 가능
        if (party.status == PartyStatus.PLANNING) {
            val hasGuests = partyGuestRepository.fetch(
                PartyGuestFetchParam(
                    partyId = party.id.value,
                    withDeleted = false,
                )
            ).isNotEmpty()

            if (!hasGuests) return
        }

        throw HomeOrderException(ErrorCode.BAD_REQUEST, "파티를 삭제할 수 없습니다. 먼저 취소해주세요")
    }
}
