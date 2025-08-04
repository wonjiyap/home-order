package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.UserEntity
import com.wonjiyap.homeorder.repository.dto.UserFetchOneParam
import com.wonjiyap.homeorder.tables.Users
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class UserRepository {

    fun fetchOne(param: UserFetchOneParam): UserEntity? = transaction {
        val conditions = mutableListOf<Op<Boolean>>()
        param.id?.let { conditions.add(Users.id eq it) }
        param.loginId?.let { conditions.add(Users.loginId eq it) }
        when (param.deleted) {
            true -> conditions.add(Users.deletedAt.isNotNull())
            false -> conditions.add(Users.deletedAt.isNull())
            null -> {}
        }

        if (conditions.isEmpty()) {
            UserEntity.all().firstOrNull()
        } else {
            UserEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .firstOrNull()
        }
    }

    fun save(entity: UserEntity) = transaction {
        entity.flush()
    }
}