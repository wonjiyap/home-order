package com.wonjiyap.homeorder.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object OrderItems : LongIdTable("order_items") {
    val orderId = long("order_id")
    val menuId = long("menu_id")
    val quantity = integer("quantity").default(1)
    val notes = text("notes").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex("unique_order_menu", orderId, menuId)
    }
}