package com.wonjiyap.homeorder.domain

import jakarta.persistence.*
import lombok.ToString

@Entity
@ToString(of = ["id", "description"])
class MenuOption(
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "menu_item_id")
        var menuItem: MenuItem,
        @Lob
        @Column(nullable = false)
        var description: String,
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null
) {
}