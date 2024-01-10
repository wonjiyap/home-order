package com.wonjiyap.homeorder.domain

import jakarta.persistence.*
import lombok.ToString

@Entity
@ToString(of = ["id", "name"])
class Category(
        @Column(nullable = false)
        var name: String,
        @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL])
        var menuItems: MutableList<MenuItem> = ArrayList(),
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,
) {

}