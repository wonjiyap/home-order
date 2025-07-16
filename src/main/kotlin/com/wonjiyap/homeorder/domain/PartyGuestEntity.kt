package com.wonjiyap.homeorder.domain

import com.wonjiyap.homeorder.tables.PartyGuests
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class PartyGuestEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<PartyGuestEntity>(PartyGuests)

    var partyId by PartyGuests.partyId
    var nickname by PartyGuests.nickname
    var isBlocked by PartyGuests.isBlocked
    var joinedAt by PartyGuests.joinedAt
    var deletedAt by PartyGuests.deletedAt

    fun isDeleted(): Boolean = deletedAt != null
}