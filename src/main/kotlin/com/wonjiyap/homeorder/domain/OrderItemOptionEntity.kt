package com.wonjiyap.homeorder.domain

import com.wonjiyap.homeorder.tables.OrderItemOptions
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class OrderItemOptionEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<OrderItemOptionEntity>(OrderItemOptions)

    var orderItemId by OrderItemOptions.orderItemId
    var optionId by OrderItemOptions.optionId
    var createdAt by OrderItemOptions.createdAt
}