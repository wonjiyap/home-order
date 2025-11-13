package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.OptionGroupEntity
import com.wonjiyap.homeorder.repository.dto.OptionGroupFetchOneParam
import com.wonjiyap.homeorder.repository.dto.OptionGroupFetchParam
import com.wonjiyap.homeorder.tables.OptionGroups
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class OptionGroupRepository {

    fun fetch(param: OptionGroupFetchParam): List<OptionGroupEntity> = transaction {
        val conditions = mutableListOf<Op<Boolean>>()

        param.menuId?.let { conditions.add(OptionGroups.menuId eq it) }
        param.name?.let { conditions.add(OptionGroups.name eq it) }
        param.isRequired?.let { conditions.add(OptionGroups.isRequired eq it) }
        if (!param.withDeleted) conditions.add(OptionGroups.deletedAt.isNull())

        if (conditions.isEmpty()) {
            OptionGroupEntity.all().toList()
        } else {
            OptionGroupEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .toList()
        }
    }

    fun fetchOne(param: OptionGroupFetchOneParam): OptionGroupEntity? = transaction {
        val conditions = mutableListOf<Op<Boolean>>()

        param.id?.let { conditions.add(OptionGroups.id eq it) }
        param.menuId?.let { conditions.add(OptionGroups.menuId eq it) }
        param.name?.let { conditions.add(OptionGroups.name eq it) }
        if (!param.withDeleted) conditions.add(OptionGroups.deletedAt.isNull())

        if (conditions.isEmpty()) {
            OptionGroupEntity.all().firstOrNull()
        } else {
            OptionGroupEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .firstOrNull()
        }
    }

    fun save(entity: OptionGroupEntity) = transaction {
        entity.flush()
    }
}