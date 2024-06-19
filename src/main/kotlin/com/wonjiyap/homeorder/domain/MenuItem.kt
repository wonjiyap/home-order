package com.wonjiyap.homeorder.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table

@Entity
@Table(name = "menu_item")
class MenuItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0,

    @Column(name = "category_id", nullable = false)
    var categoryId: Long = 0,

    @Column(name = "name", nullable = false)
    var name: String,

    @Lob
    @Column(name = "description")
    var description: String,
)