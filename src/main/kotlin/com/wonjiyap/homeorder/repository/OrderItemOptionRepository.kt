package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.OrderItemOptionEntity
import com.wonjiyap.homeorder.repository.dto.OrderItemOptionFetchOneParam
import com.wonjiyap.homeorder.repository.dto.OrderItemOptionFetchParam
import com.wonjiyap.homeorder.tables.OrderItemOptions
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class OrderItemOptionRepository {

    fun fetch(param: OrderItemOptionFetchParam): List<OrderItemOptionEntity> = transaction  {
        val conditions = mutableListOf<Op<Boolean>>()

        param.orderItemId?.let {conditions.add(OrderItemOptions.orderItemId eq  it)}
        param.optionId?.let {conditions.add(OrderItemOptions.optionId eq it)}

        if (conditions.isEmpty()) {
            OrderItemOptionEntity.all().toList()
        } else {
            OrderItemOptionEntity
                .find { conditions.reduce { acc, condition -> acc and  condition } }
                .toList()
        }
    }

    fun fetchOne(param: OrderItemOptionFetchOneParam): OrderItemOptionEntity? = transaction  {
        val conditions = mutableListOf<Op<Boolean>>()

        param.id?.let {conditions.add(OrderItemOptions.id eq it)}
        param.orderItemId?.let {conditions.add(OrderItemOptions.orderItemId eq  it)}
        param.optionId?.let {conditions.add(OrderItemOptions.optionId eq it)}

        if (conditions.isEmpty()) {
            OrderItemOptionEntity.all().firstOrNull()
        } else {
            OrderItemOptionEntity
                .find { conditions.reduce { acc, condition -> acc and  condition } }
                .firstOrNull()
        }
    }

    fun save(entity: OrderItemOptionEntity) = transaction {
        entity.flush()
    }
}