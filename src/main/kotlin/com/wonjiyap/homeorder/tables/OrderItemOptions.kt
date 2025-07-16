package com.wonjiyap.homeorder.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object OrderItemOptions : LongIdTable("order_item_options") {
    val orderItemId = long("order_item_id")
    val optionId = long("option_id")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex("unique_order_item_option", orderItemId, optionId)
    }
}