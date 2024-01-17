package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.Category
import com.wonjiyap.homeorder.domain.MenuItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MenuItemRepository: JpaRepository<MenuItem,Long> {

    @Query("SELECT mi FROM MenuItem mi WHERE mi.category = :category")
    fun findByCategory(category: Category): MutableList<MenuItem>
}