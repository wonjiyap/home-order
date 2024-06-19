package com.wonjiyap.homeorder.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.wonjiyap.homeorder.domain.MenuOption
import com.wonjiyap.homeorder.domain.QMenuOption.menuOption
import com.wonjiyap.homeorder.repository.dto.menuOption.MenuOptionFetchParam

class MenuOptionCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
): MenuOptionCustomRepository {

    override fun fetch(param: MenuOptionFetchParam): List<MenuOption> {
        val query = queryFactory.selectFrom(menuOption)

        param.menuItemId?.let { query.where(menuOption.menuItemId.eq(param.menuItemId)) }

        query.orderBy(menuOption.id.asc())
        return query.fetch()
    }
}