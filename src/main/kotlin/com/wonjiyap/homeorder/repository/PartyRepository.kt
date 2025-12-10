package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.PartyEntity
import com.wonjiyap.homeorder.repository.dto.PartyFetchOneParam
import com.wonjiyap.homeorder.repository.dto.PartyFetchParam
import com.wonjiyap.homeorder.tables.Parties
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class PartyRepository {

    fun fetch(param: PartyFetchParam): List<PartyEntity> = transaction {
        val conditions = mutableListOf<Op<Boolean>>()
        param.hostId?.let { conditions.add(Parties.hostId eq it) }
        param.name?.let { searchName ->
            conditions.add(
                Parties.name.lowerCase() like "%${searchName.lowercase()}%"
            )
        }
        param.status?.let { conditions.add(Parties.status eq it) }
        param.dateFrom?.let { conditions.add(Parties.date greaterEq it) }
        param.dateTo?.let { conditions.add(Parties.date lessEq it) }
        param.deleted?.let { deleted ->
            if (deleted) {
                conditions.add(Parties.deletedAt.isNotNull())
            } else {
                conditions.add(Parties.deletedAt.isNull())
            }
        }

        if (conditions.isEmpty()) {
            PartyEntity.all().toList()
        } else {
            PartyEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .toList()
        }
    }

    fun fetchOne(param: PartyFetchOneParam): PartyEntity? = transaction {
        val conditions = mutableListOf<Op<Boolean>>()
        param.id?.let { conditions.add(Parties.id eq it) }
        param.hostId?.let { conditions.add(Parties.hostId eq it) }
        param.deleted?.let { deleted ->
            if (deleted) {
                conditions.add(Parties.deletedAt.isNotNull())
            } else {
                conditions.add(Parties.deletedAt.isNull())
            }
        }

        if (conditions.isEmpty()) {
            PartyEntity.all().firstOrNull()
        } else {
            PartyEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .firstOrNull()
        }
    }

    fun save(entity: PartyEntity) = transaction {
        entity.flush()
    }
}