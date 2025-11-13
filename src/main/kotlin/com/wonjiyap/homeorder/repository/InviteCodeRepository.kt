package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.InviteCodeEntity
import com.wonjiyap.homeorder.repository.dto.InviteCodeFetchOneParam
import com.wonjiyap.homeorder.repository.dto.InviteCodeFetchParam
import com.wonjiyap.homeorder.tables.InviteCodes
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class InviteCodeRepository {

    fun fetch(param: InviteCodeFetchParam): List<InviteCodeEntity> = transaction {
        val conditions = mutableListOf<Op<Boolean>>()

        param.partyId?.let { conditions.add(InviteCodes.partyId eq it) }
        param.isActive?.let { conditions.add(InviteCodes.isActive eq it) }
        param.isExpired?.let {
            if (it) {
                conditions.add(InviteCodes.expiresAt.isNotNull())
                conditions.add(InviteCodes.expiresAt less Instant.now())
            } else {
                conditions.add(InviteCodes.expiresAt.isNull() or (InviteCodes.expiresAt greaterEq Instant.now()))
            }
        }
        if (!param.withDeleted) conditions.add(InviteCodes.deletedAt.isNull())

        if (conditions.isEmpty()) {
            InviteCodeEntity.all().toList()
        } else {
            InviteCodeEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .toList()
        }
    }

    fun fetchOne(param: InviteCodeFetchOneParam): InviteCodeEntity? = transaction {
        val conditions = mutableListOf<Op<Boolean>>()

        param.id?.let { conditions.add(InviteCodes.id eq it) }
        param.partyId?.let { conditions.add(InviteCodes.partyId eq it) }
        param.code?.let { conditions.add(InviteCodes.code eq it) }
        param.isActive?.let { conditions.add(InviteCodes.isActive eq it) }
        param.isExpired?.let {
            if (it) {
                conditions.add(InviteCodes.expiresAt.isNotNull())
                conditions.add(InviteCodes.expiresAt less Instant.now())
            } else {
                conditions.add(InviteCodes.expiresAt.isNull() or (InviteCodes.expiresAt greaterEq Instant.now()))
            }
        }
        if (!param.withDeleted) conditions.add(InviteCodes.deletedAt.isNull())

        if (conditions.isEmpty()) {
            InviteCodeEntity.all().firstOrNull()
        } else {
            InviteCodeEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .firstOrNull()
        }
    }

    fun save(entity: InviteCodeEntity) = transaction {
        entity.flush()
    }
}