package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.MenuEntity
import com.wonjiyap.homeorder.repository.dto.MenuFetchOneParam
import com.wonjiyap.homeorder.repository.dto.MenuFetchParam
import com.wonjiyap.homeorder.tables.Menus
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class MenuRepository {

    fun fetch(param: MenuFetchParam): List<MenuEntity> = transaction {
        val conditions = mutableListOf<Op<Boolean>>()

        param.categoryId?.let { conditions.add(Menus.categoryId eq it) }
        param.name?.let { conditions.add(Menus.name eq it) }
        param.isRecommended?.let { conditions.add(Menus.isRecommended eq it) }
        param.isSoldOut?.let { conditions.add(Menus.isSoldOut eq it) }
        if (!param.withDeleted) conditions.add(Menus.deletedAt.isNull())

        if (conditions.isEmpty()) {
            MenuEntity.all().toList()
        } else {
            MenuEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .toList()
        }
    }

    fun fetchOne(param: MenuFetchOneParam): MenuEntity? = transaction {
        val conditions = mutableListOf<Op<Boolean>>()

        param.id?.let { conditions.add(Menus.id eq it) }
        param.categoryId?.let { conditions.add(Menus.categoryId eq it) }
        param.name?.let { conditions.add(Menus.name eq it) }
        if (!param.withDeleted) conditions.add(Menus.deletedAt.isNull())

        if (conditions.isEmpty()) {
            MenuEntity.all().firstOrNull()
        } else {
            MenuEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .firstOrNull()
        }
    }

    fun save(entity: MenuEntity) = transaction {
        entity.flush()
    }
}