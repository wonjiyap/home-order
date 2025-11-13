package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.OrderItemEntity
import com.wonjiyap.homeorder.repository.dto.OrderItemFetchOneParam
import com.wonjiyap.homeorder.repository.dto.OrderItemFetchParam
import com.wonjiyap.homeorder.tables.OrderItems
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class OrderItemRepository {

    fun fetch(param: OrderItemFetchParam): List<OrderItemEntity> = transaction {
        val conditions = mutableListOf<Op<Boolean>>()

        param.orderId?.let { conditions.add(OrderItems.orderId eq it) }
        param.menuId?.let {conditions.add(OrderItems.menuId eq it)}

        if (conditions.isEmpty()) {
            OrderItemEntity.all().toList()
        } else {
            OrderItemEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .toList()
        }
    }

    fun fetchOne(param: OrderItemFetchOneParam): OrderItemEntity? = transaction {
        val conditions = mutableListOf<Op<Boolean>>()

        param.id?.let { conditions.add(OrderItems.id eq it) }
        param.orderId?.let { conditions.add(OrderItems.orderId eq it) }
        param.menuId?.let {conditions.add(OrderItems.menuId eq it)}

        if (conditions.isEmpty()) {
            OrderItemEntity.all().firstOrNull()
        } else {
            OrderItemEntity
                .find { conditions.reduce { acc, condition -> acc and condition } }
                .firstOrNull()
        }
    }

    fun save(entity: OrderItemEntity) = transaction {
        entity.flush()
    }
}