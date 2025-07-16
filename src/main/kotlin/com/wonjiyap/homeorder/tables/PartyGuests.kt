package com.wonjiyap.homeorder.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object PartyGuests : LongIdTable("party_guests") {
    val partyId = long("party_id")
    val nickname = varchar("nickname", 100)
    val isBlocked = bool("is_blocked").default(false)
    val joinedAt = timestamp("joined_at").defaultExpression(CurrentTimestamp)
    val deletedAt = timestamp("deleted_at").nullable()

    init {
        uniqueIndex("unique_party_nickname", partyId, nickname)
    }
}