package com.wonjiyap.homeorder.domain

import jakarta.persistence.*
import lombok.ToString

@Entity
@ToString(of = ["id", "name", "description"])
class MenuItem(
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "category_id")
        var category: Category,
        @Column(nullable = false)
        var name: String,
        @Lob
        var description: String,
        @OneToMany(mappedBy = "menuItem")
        var menuOptions: MutableList<MenuOption> = ArrayList(),
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null
) {
}