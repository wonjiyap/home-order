package com.wonjiyap.homeorder.domain

import com.wonjiyap.homeorder.tables.Options
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class OptionEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<OptionEntity>(Options)

    var optionGroupId by Options.optionGroupId
    var name by Options.name
    var displayOrder by Options.displayOrder
    var createdAt by Options.createdAt
    var updatedAt by Options.updatedAt
}