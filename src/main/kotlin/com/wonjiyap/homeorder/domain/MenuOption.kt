package com.wonjiyap.homeorder.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "menu_option")
class MenuOption(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0,

    @Column(name = "menu_item_id", nullable = false)
    var menuItemId: Long = 0,

    @Column(nullable = false)
    var description: String,
)