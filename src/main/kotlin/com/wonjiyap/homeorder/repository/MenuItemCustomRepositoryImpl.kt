package com.wonjiyap.homeorder.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.wonjiyap.homeorder.domain.MenuItem
import com.wonjiyap.homeorder.domain.QMenuItem.menuItem
import com.wonjiyap.homeorder.repository.dto.menuItem.MenuItemFetchParam

class MenuItemCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
): MenuItemCustomRepository {

    override fun fetch(param: MenuItemFetchParam): List<MenuItem> {
        val query = queryFactory.selectFrom(menuItem)

        param.categoryId?.let { query.where(menuItem.categoryId.eq(param.categoryId)) }

        query.orderBy(menuItem.name.asc())
        return query.fetch()
    }
}