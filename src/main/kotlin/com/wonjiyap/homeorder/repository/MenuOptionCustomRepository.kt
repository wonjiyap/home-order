package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.MenuOption
import com.wonjiyap.homeorder.repository.dto.menuOption.MenuOptionFetchParam

interface MenuOptionCustomRepository {

    fun fetch(param: MenuOptionFetchParam): List<MenuOption>
}