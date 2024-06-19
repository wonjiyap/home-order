package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.MenuItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MenuItemRepository: JpaRepository<MenuItem,Long>, MenuItemCustomRepository