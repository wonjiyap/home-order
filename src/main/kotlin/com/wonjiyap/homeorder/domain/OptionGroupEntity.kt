package com.wonjiyap.homeorder.domain

import com.wonjiyap.homeorder.tables.OptionGroups
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class OptionGroupEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<OptionGroupEntity>(OptionGroups)

    var menuId by OptionGroups.menuId
    var name by OptionGroups.name
    var isRequired by OptionGroups.isRequired
    var createdAt by OptionGroups.createdAt
    var updatedAt by OptionGroups.updatedAt
    var deletedAt by OptionGroups.deletedAt
}