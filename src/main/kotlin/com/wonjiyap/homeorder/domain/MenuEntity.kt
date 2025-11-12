package com.wonjiyap.homeorder.domain

import com.wonjiyap.homeorder.tables.Menus
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class MenuEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<MenuEntity>(Menus)

    var categoryId by Menus.categoryId
    var name by Menus.name
    var description by Menus.description
    var isRecommended by Menus.isRecommended
    var isSoldOut by Menus.isSoldOut
    var displayOrder by Menus.displayOrder
    var createdAt by Menus.createdAt
    var updatedAt by Menus.updatedAt
    var deletedAt by Menus.deletedAt
}