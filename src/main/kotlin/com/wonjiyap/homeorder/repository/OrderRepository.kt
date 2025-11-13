package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.OrderEntity
import com.wonjiyap.homeorder.repository.dto.OrderFetchOneParam
import com.wonjiyap.homeorder.repository.dto.OrderFetchParam
import com.wonjiyap.homeorder.tables.Orders
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class OrderRepository {

    fun fetch(param: OrderFetchParam): List<OrderEntity> = transaction {
        val conditions = mutableListOf<Op<Boolean>>()

        param.partyId?.let { conditions.add(Orders.partyId eq it) }
        param.guestId?.let { conditions.add(Orders.guestId eq it) }
        param.status?.let { conditions.add(Orders.status eq it) }

        if (conditions.isEmpty()) {
            OrderEntity.all().toList()
        } else {
            OrderEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .toList()
        }
    }

    fun fetchOne(param: OrderFetchOneParam): OrderEntity? = transaction {
        val conditions = mutableListOf<Op<Boolean>>()

        param.id?.let { conditions.add(Orders.id eq it) }
        param.partyId?.let { conditions.add(Orders.partyId eq it) }
        param.guestId?.let { conditions.add(Orders.guestId eq it) }
        param.status?.let { conditions.add(Orders.status eq it) }

        if (conditions.isEmpty()) {
            OrderEntity.all().firstOrNull()
        } else {
            OrderEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .firstOrNull()
        }
    }

    fun save(entity: OrderEntity) = transaction {
        entity.flush()
    }
}