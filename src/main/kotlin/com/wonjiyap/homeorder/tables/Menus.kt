package com.wonjiyap.homeorder.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Menus : LongIdTable("menus") {
    val categoryId = long("category_id")
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val isRecommended = bool("is_recommended").default(false)
    val isSoldOut = bool("is_sold_out").default(false)
    val displayOrder = integer("display_order").default(0)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
    val deletedAt = timestamp("deleted_at").nullable()

    // Partial unique index is defined in migration V3 (WHERE deleted_at IS NULL)
}