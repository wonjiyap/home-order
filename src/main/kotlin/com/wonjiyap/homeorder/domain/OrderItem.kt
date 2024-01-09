package com.wonjiyap.homeorder.domain

import com.wonjiyap.homeorder.enums.OrderStatus
import jakarta.persistence.*
import lombok.ToString
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Entity
@ToString(of = ["id", "quantity", "status"])
class OrderItem(
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "menu_item_id")
        var menuItem: MenuItem,
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "menu_option_id")
        var menuOption: MenuOption,
        @Column(nullable = false)
        var quantity: Int,
        @Column(nullable = false)
        @Enumerated(EnumType.STRING)
        var orderStatus: OrderStatus = OrderStatus.PROCESSING,
        @CreatedDate
        @Column(updatable = false)
        var createdDate: LocalDateTime,
        @LastModifiedDate
        var lastModifiedDate: LocalDateTime,
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null
) {
}