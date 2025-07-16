package com.wonjiyap.homeorder.domain

import com.wonjiyap.homeorder.tables.OrderItems
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class OrderItemEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<OrderItemEntity>(OrderItems)

    var orderId by OrderItems.orderId
    var menuId by OrderItems.menuId
    var quantity by OrderItems.quantity
    var notes by OrderItems.notes
    var createdAt by OrderItems.createdAt
}