package com.wonjiyap.homeorder.domain

import com.wonjiyap.homeorder.tables.Users
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserEntity>(Users)

    var loginId by Users.loginId
    var password by Users.password
    var nickname by Users.nickname
    var createdAt by Users.createdAt
    var updatedAt by Users.updatedAt
    var deletedAt by Users.deletedAt

    fun isDeleted(): Boolean = deletedAt != null
}