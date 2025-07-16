package com.wonjiyap.homeorder.domain

import com.wonjiyap.homeorder.tables.Parties
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class PartyEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<PartyEntity>(Parties)

    var hostId by Parties.hostId
    var name by Parties.name
    var description by Parties.description
    var date by Parties.date
    var location by Parties.location
    var status by Parties.status
    var createdAt by Parties.createdAt
    var updatedAt by Parties.updatedAt
    var deletedAt by Parties.deletedAt

    fun isDeleted(): Boolean = deletedAt != null
}