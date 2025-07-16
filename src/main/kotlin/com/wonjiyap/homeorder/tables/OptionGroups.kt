package com.wonjiyap.homeorder.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object OptionGroups : LongIdTable("option_groups") {
    val menuId = long("menu_id")
    val name = varchar("name", 100)
    val isRequired = bool("is_required").default(false)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex("unique_menu_option_group", menuId, name)
    }
}