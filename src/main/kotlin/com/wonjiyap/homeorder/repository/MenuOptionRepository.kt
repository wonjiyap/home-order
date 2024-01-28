package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.MenuItem
import com.wonjiyap.homeorder.domain.MenuOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MenuOptionRepository: JpaRepository<MenuOption,Long> {

    @Query("SELECT mo FROM MenuOption mo WHERE mo.menuItem = :menuItem")
    fun findByMenuItem(menuItem: MenuItem): MutableList<MenuOption>
}