package com.wonjiyap.homeorder.tables

import com.wonjiyap.homeorder.enums.OrderStatus
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Orders : LongIdTable("orders") {
    val partyId = long("party_id")
    val guestId = long("guest_id")
    val status = enumerationByName("status", 20, OrderStatus::class)
    val orderedAt = timestamp("ordered_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    init {
        index("idx_party_id_guest_id_status", false, partyId, guestId, status)
        index("idx_orders_status", false, status)
    }
}