package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.CategoryEntity
import com.wonjiyap.homeorder.domain.MenuEntity
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.repository.CategoryRepository
import com.wonjiyap.homeorder.repository.MenuRepository
import com.wonjiyap.homeorder.repository.PartyRepository
import com.wonjiyap.homeorder.repository.dto.CategoryFetchOneParam
import com.wonjiyap.homeorder.repository.dto.MenuFetchOneParam
import com.wonjiyap.homeorder.repository.dto.MenuFetchParam
import com.wonjiyap.homeorder.repository.dto.PartyFetchOneParam
import com.wonjiyap.homeorder.service.dto.MenuCreateParam
import com.wonjiyap.homeorder.service.dto.MenuDeleteParam
import com.wonjiyap.homeorder.service.dto.MenuGetParam
import com.wonjiyap.homeorder.service.dto.MenuListParam
import com.wonjiyap.homeorder.service.dto.MenuReorderParam
import com.wonjiyap.homeorder.service.dto.MenuUpdateParam
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class MenuService(
    private val menuRepository: MenuRepository,
    private val categoryRepository: CategoryRepository,
    private val partyRepository: PartyRepository,
) {

    fun list(param: MenuListParam): List<MenuEntity> {
        validateCategoryOwnership(param.categoryId, param.hostId)

        return menuRepository.fetch(
            MenuFetchParam(
                categoryId = param.categoryId,
                withDeleted = false,
            )
        )
    }

    fun get(param: MenuGetParam): MenuEntity {
        validateCategoryOwnership(param.categoryId, param.hostId)

        return menuRepository.fetchOne(
            MenuFetchOneParam(
                id = param.id,
                categoryId = param.categoryId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "메뉴를 찾을 수 없습니다")
    }

    fun create(param: MenuCreateParam): MenuEntity = transaction {
        validateCategoryOwnership(param.categoryId, param.hostId)
        validateMenuNameUnique(param.categoryId, param.name)

        MenuEntity.new {
            categoryId = param.categoryId
            name = param.name
            description = param.description
            isRecommended = param.isRecommended
            isSoldOut = param.isSoldOut
            displayOrder = getNextDisplayOrder(param.categoryId)
            createdAt = Instant.now()
            updatedAt = Instant.now()
        }
    }

    fun update(param: MenuUpdateParam): MenuEntity = transaction {
        validateCategoryOwnership(param.categoryId, param.hostId)

        val menu = menuRepository.fetchOne(
            MenuFetchOneParam(
                id = param.id,
                categoryId = param.categoryId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "메뉴를 찾을 수 없습니다")

        param.name?.let { newName ->
            if (menu.name != newName) {
                validateMenuNameUnique(param.categoryId, newName)
                menu.name = newName
            }
        }
        param.description?.let { menu.description = it }
        param.isRecommended?.let { menu.isRecommended = it }
        param.isSoldOut?.let { menu.isSoldOut = it }

        menu.updatedAt = Instant.now()
        menu
    }

    fun delete(param: MenuDeleteParam) = transaction {
        validateCategoryOwnership(param.categoryId, param.hostId)

        val menu = menuRepository.fetchOne(
            MenuFetchOneParam(
                id = param.id,
                categoryId = param.categoryId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "메뉴를 찾을 수 없습니다")

        menu.deletedAt = Instant.now()
    }

    fun reorder(param: MenuReorderParam): List<MenuEntity> = transaction {
        validateCategoryOwnership(param.categoryId, param.hostId)

        val menus = menuRepository.fetch(
            MenuFetchParam(
                categoryId = param.categoryId,
                withDeleted = false,
            )
        )

        val menuMap = menus.associateBy { it.id.value }
        val now = Instant.now()

        param.menuIds.forEachIndexed { index, menuId ->
            val menu = menuMap[menuId]
                ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "메뉴를 찾을 수 없습니다: $menuId")
            menu.displayOrder = index
            menu.updatedAt = now
        }

        // 전달된 ID 목록에 없는 메뉴들은 뒤쪽 순서로 유지
        val remainingMenus = menus.filter { it.id.value !in param.menuIds }
        remainingMenus.forEachIndexed { index, menu ->
            menu.displayOrder = param.menuIds.size + index
            menu.updatedAt = now
        }

        menuRepository.fetch(
            MenuFetchParam(
                categoryId = param.categoryId,
                withDeleted = false,
            )
        )
    }

    private fun validateCategoryOwnership(categoryId: Long, hostId: Long): CategoryEntity {
        val category = categoryRepository.fetchOne(
            CategoryFetchOneParam(
                id = categoryId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다")

        partyRepository.fetchOne(
            PartyFetchOneParam(
                id = category.partyId,
                hostId = hostId,
                deleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.FORBIDDEN, "해당 카테고리에 대한 권한이 없습니다")

        return category
    }

    private fun validateMenuNameUnique(categoryId: Long, name: String) {
        val existingMenu = menuRepository.fetchOne(
            MenuFetchOneParam(
                categoryId = categoryId,
                name = name,
                withDeleted = false,
            )
        )
        if (existingMenu != null) {
            throw HomeOrderException(ErrorCode.CONFLICT, "같은 이름의 메뉴가 이미 존재합니다")
        }
    }

    private fun getNextDisplayOrder(categoryId: Long): Int {
        val menus = menuRepository.fetch(
            MenuFetchParam(
                categoryId = categoryId,
                withDeleted = false,
            )
        )
        return if (menus.isEmpty()) 0 else menus.maxOf { it.displayOrder } + 1
    }
}
