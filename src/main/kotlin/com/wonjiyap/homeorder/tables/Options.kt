package com.wonjiyap.homeorder.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Options : LongIdTable("options") {
    val optionGroupId = long("option_group_id")
    val name = varchar("name", 100)
    val displayOrder = integer("display_order").default(0)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
    val deletedAt = timestamp("deleted_at").nullable()

    // Partial unique index is defined in migration V4 (WHERE deleted_at IS NULL)
}