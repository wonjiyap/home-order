package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.InviteCodeEntity
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.repository.InviteCodeRepository
import com.wonjiyap.homeorder.repository.PartyRepository
import com.wonjiyap.homeorder.repository.dto.InviteCodeFetchOneParam
import com.wonjiyap.homeorder.repository.dto.InviteCodeFetchParam
import com.wonjiyap.homeorder.repository.dto.PartyFetchOneParam
import com.wonjiyap.homeorder.service.dto.InviteCodeCreateParam
import com.wonjiyap.homeorder.service.dto.InviteCodeDeleteParam
import com.wonjiyap.homeorder.service.dto.InviteCodeGetParam
import com.wonjiyap.homeorder.service.dto.InviteCodeListParam
import com.wonjiyap.homeorder.service.dto.InviteCodeUpdateParam
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class InviteCodeService(
    private val inviteCodeRepository: InviteCodeRepository,
    private val partyRepository: PartyRepository,
) {

    fun list(param: InviteCodeListParam): List<InviteCodeEntity> {
        validatePartyOwnership(param.partyId, param.hostId)
        return inviteCodeRepository.fetch(
            InviteCodeFetchParam(
                partyId = param.partyId,
                withDeleted = false,
            )
        )
    }

    fun get(param: InviteCodeGetParam): InviteCodeEntity {
        validatePartyOwnership(param.partyId, param.hostId)
        return inviteCodeRepository.fetchOne(
            InviteCodeFetchOneParam(
                id = param.id,
                partyId = param.partyId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "초대 코드를 찾을 수 없습니다")
    }

    fun create(param: InviteCodeCreateParam): InviteCodeEntity = transaction {
        validatePartyOwnership(param.partyId, param.hostId)

        param.expiresAt?.let { validateExpiresAtIsFuture(it) }

        InviteCodeEntity.new {
            partyId = param.partyId
            code = generateCode()
            isActive = true
            createdAt = Instant.now()
            expiresAt = param.expiresAt
        }
    }

    fun update(param: InviteCodeUpdateParam): InviteCodeEntity = transaction {
        validatePartyOwnership(param.partyId, param.hostId)

        val inviteCode = inviteCodeRepository.fetchOne(
            InviteCodeFetchOneParam(
                id = param.id,
                partyId = param.partyId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "초대 코드를 찾을 수 없습니다")

        param.expiresAt?.let { newExpiresAt ->
            if (inviteCode.expiresAt != newExpiresAt) {
                validateExpiresAtIsFuture(newExpiresAt)
            }
        }
        param.isActive?.let { inviteCode.isActive = it }
        param.expiresAt?.let { inviteCode.expiresAt = it }

        inviteCode
    }

    fun delete(param: InviteCodeDeleteParam) = transaction {
        validatePartyOwnership(param.partyId, param.hostId)

        val inviteCode = inviteCodeRepository.fetchOne(
            InviteCodeFetchOneParam(
                id = param.id,
                partyId = param.partyId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "초대 코드를 찾을 수 없습니다")

        inviteCode.isActive = false
        inviteCode.deletedAt = Instant.now()
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

    private fun validateExpiresAtIsFuture(expiresAt: Instant) {
        if (expiresAt.isBefore(Instant.now())) {
            throw HomeOrderException(ErrorCode.BAD_REQUEST, "만료 시간은 현재 시간 이후여야 합니다")
        }
    }

    private fun generateCode(): String {
        return UUID.randomUUID().toString().replace("-", "").take(8).uppercase()
    }
}
