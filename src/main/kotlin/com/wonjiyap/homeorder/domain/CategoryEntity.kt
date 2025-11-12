package com.wonjiyap.homeorder.domain

import com.wonjiyap.homeorder.tables.Categories
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CategoryEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<CategoryEntity>(Categories)

    var partyId by Categories.partyId
    var name by Categories.name
    var displayOrder by Categories.displayOrder
    var createdAt by Categories.createdAt
    var updatedAt by Categories.updatedAt
    var deletedAt by Categories.deletedAt
}