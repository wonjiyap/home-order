package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.MenuItem
import com.wonjiyap.homeorder.repository.dto.menuItem.MenuItemFetchParam

interface MenuItemCustomRepository {

    fun fetch(param: MenuItemFetchParam): List<MenuItem>
}