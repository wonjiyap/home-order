package com.wonjiyap.homeorder.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object InviteCodes : LongIdTable("invite_codes") {
    val partyId = long("party_id")
    val code = varchar("code", 100).uniqueIndex()
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val expiresAt = timestamp("expires_at").nullable()
    val deletedAt = timestamp("deleted_at").nullable()

    init {
        index("idx_party_id_code", false, partyId, code)
    }
}