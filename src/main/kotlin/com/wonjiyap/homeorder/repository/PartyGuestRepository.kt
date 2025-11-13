package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.PartyGuestEntity
import com.wonjiyap.homeorder.repository.dto.PartyGuestFetchOneParam
import com.wonjiyap.homeorder.repository.dto.PartyGuestFetchParam
import com.wonjiyap.homeorder.tables.PartyGuests
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class PartyGuestRepository {

    fun fetch(param: PartyGuestFetchParam): List<PartyGuestEntity> = transaction {
        val conditions = mutableListOf<Op<Boolean>>()

        param.partyId?.let { conditions.add(PartyGuests.partyId eq it) }
        param.isBlocked?.let { conditions.add(PartyGuests.isBlocked eq it) }
        if (!param.withDeleted) conditions.add(PartyGuests.deletedAt.isNull())

        if (conditions.isEmpty()) {
            PartyGuestEntity.all().toList()
        } else {
            PartyGuestEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .toList()
        }
    }

    fun fetchOne(param: PartyGuestFetchOneParam): PartyGuestEntity? = transaction {
        val conditions = mutableListOf<Op<Boolean>>()

        param.id?.let { conditions.add(PartyGuests.id eq it) }
        param.partyId?.let { conditions.add(PartyGuests.partyId eq it) }
        param.nickname?.let { conditions.add(PartyGuests.nickname.lowerCase() like "%${it.lowercase()}%") }
        param.isBlocked?.let { conditions.add(PartyGuests.isBlocked eq it) }
        if (!param.withDeleted) conditions.add(PartyGuests.deletedAt.isNull())

        if (conditions.isEmpty()) {
            PartyGuestEntity.all().firstOrNull()
        } else {
            PartyGuestEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .firstOrNull()
        }
    }

    fun save(entity: PartyGuestEntity) = transaction {
        entity.flush()
    }
}