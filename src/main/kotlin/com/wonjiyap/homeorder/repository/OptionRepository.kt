package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.OptionEntity
import com.wonjiyap.homeorder.repository.dto.OptionFetchOneParam
import com.wonjiyap.homeorder.repository.dto.OptionFetchParam
import com.wonjiyap.homeorder.tables.Options
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class OptionRepository {

    fun fetch(param: OptionFetchParam): List<OptionEntity> = transaction {
        val conditions = mutableListOf<Op<Boolean>>()

        param.optionGroupId?.let { conditions.add(Options.optionGroupId eq it) }
        param.name?.let { conditions.add(Options.name eq it) }
        if (!param.withDeleted) conditions.add(Options.deletedAt.isNull())

        if (conditions.isEmpty()) {
            OptionEntity.all().toList()
        } else {
            OptionEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .toList()
        }
    }

    fun fetchOne(param: OptionFetchOneParam): OptionEntity? = transaction {
        val conditions = mutableListOf<Op<Boolean>>()

        param.id?.let { conditions.add(Options.id eq it) }
        param.optionGroupId?.let { conditions.add(Options.optionGroupId eq it) }
        param.name?.let { conditions.add(Options.name eq it) }
        if (!param.withDeleted) conditions.add(Options.deletedAt.isNull())

        if (conditions.isEmpty()) {
            OptionEntity.all().firstOrNull()
        } else {
            OptionEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .firstOrNull()
        }
    }

    fun save(entity: OptionEntity) = transaction {
        entity.flush()
    }
}