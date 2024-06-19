package com.wonjiyap.homeorder.domain

import com.wonjiyap.homeorder.enums.OrderStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Entity
@Table(name = "order_item")
class OrderItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0,

    @Column(name = "menu_item_id", nullable = false)
    var menuItemId: Long = 0,

    @Column(name = "menu_option_id", nullable = false)
    var menuOptionId: Long = 0,

    @Column(name = "quantity", nullable = false)
    var quantity: Int,

    @Column(name = "order_status", nullable = false)
    @Enumerated(EnumType.STRING)
    var orderStatus: OrderStatus = OrderStatus.PROCESSING,

    @CreatedDate
    @Column(name = "create_date", updatable = false)
    var createdDate: LocalDateTime,

    @LastModifiedDate
    @Column(name = "last_modified_date")
    var lastModifiedDate: LocalDateTime,
)