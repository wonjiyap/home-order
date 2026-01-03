package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.CategoryEntity
import com.wonjiyap.homeorder.repository.dto.CategoryFetchOneParam
import com.wonjiyap.homeorder.repository.dto.CategoryFetchParam
import com.wonjiyap.homeorder.tables.Categories
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class CategoryRepository {

    fun fetch(param: CategoryFetchParam): List<CategoryEntity> = transaction {
        val conditions = mutableListOf<Op<Boolean>>()

        param.partyId?.let { conditions.add(Categories.partyId eq it) }
        param.name?.let { conditions.add(Categories.name.lowerCase() like "%${it.lowercase()}%") }
        if (!param.withDeleted) conditions.add(Categories.deletedAt.isNull())

        if (conditions.isEmpty()) {
            CategoryEntity.all().toList()
        } else {
            CategoryEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .toList()
        }
    }

    fun fetchOne(param: CategoryFetchOneParam): CategoryEntity? = transaction {
        val conditions = mutableListOf<Op<Boolean>>()

        param.id?.let { conditions.add(Categories.id eq it) }
        param.partyId?.let { conditions.add(Categories.partyId eq it) }
        param.name?.let { name ->
            if (param.exactName) {
                conditions.add(Categories.name.lowerCase() eq name.lowercase())
            } else {
                conditions.add(Categories.name.lowerCase() like "%${name.lowercase()}%")
            }
        }
        if (!param.withDeleted) conditions.add(Categories.deletedAt.isNull())

        if (conditions.isEmpty()) {
            CategoryEntity.all().firstOrNull()
        } else {
            CategoryEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .firstOrNull()
        }
    }

    fun save(entity: CategoryEntity) = transaction {
        entity.flush()
    }
}