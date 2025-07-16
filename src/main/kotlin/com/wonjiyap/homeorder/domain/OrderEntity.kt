package com.wonjiyap.homeorder.domain

import com.wonjiyap.homeorder.tables.Orders
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class OrderEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<OrderEntity>(Orders)

    var partyId by Orders.partyId
    var guestId by Orders.guestId
    var status by Orders.status
    var orderedAt by Orders.orderedAt
    var updatedAt by Orders.updatedAt
}