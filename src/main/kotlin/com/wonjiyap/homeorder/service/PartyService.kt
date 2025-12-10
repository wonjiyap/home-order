package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.PartyEntity
import com.wonjiyap.homeorder.enums.PartyStatus
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.repository.PartyRepository
import com.wonjiyap.homeorder.repository.dto.PartyFetchOneParam
import com.wonjiyap.homeorder.repository.dto.PartyFetchParam
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

        party.deletedAt = Instant.now()
    }
}
