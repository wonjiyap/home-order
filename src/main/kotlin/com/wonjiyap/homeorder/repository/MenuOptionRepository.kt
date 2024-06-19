package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.MenuOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MenuOptionRepository: JpaRepository<MenuOption,Long>, MenuOptionCustomRepository