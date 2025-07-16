package com.wonjiyap.homeorder.tables

import com.wonjiyap.homeorder.enums.PartyStatus
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Parties : LongIdTable("parties") {
    val hostId = long("host_id")
    val name = varchar("name", 255)
    val description = varchar("description", 1000).nullable()
    val date = timestamp("date").nullable()
    val location = varchar("location", 255).nullable()
    val status = enumeration<PartyStatus>("status")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
    val deletedAt = timestamp("deleted_at").nullable()

    init {
        index("idx_host_id_status", false, hostId, status)
    }
}