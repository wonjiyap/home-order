package com.wonjiyap.homeorder.domain

import com.wonjiyap.homeorder.tables.InviteCodes
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.Instant

class InviteCodeEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<InviteCodeEntity>(InviteCodes)

    var partyId by InviteCodes.partyId
    var code by InviteCodes.code
    var isActive by InviteCodes.isActive
    var createdAt by InviteCodes.createdAt
    var expiresAt by InviteCodes.expiresAt
    var deletedAt by InviteCodes.deletedAt

    fun isExpired(): Boolean = expiresAt?.let { it < Instant.now() } ?: false

    fun isValid(): Boolean = isActive && !isExpired() && deletedAt == null
}