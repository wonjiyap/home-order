package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.Category
import com.wonjiyap.homeorder.domain.MenuItem
import com.wonjiyap.homeorder.repository.dto.menuItem.MenuItemFetchParam
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import org.assertj.core.api.Assertions.*

@SpringBootTest
@Transactional
class MenuItemRepositoryTest(
    @Autowired val categoryRepository: CategoryRepository,
    @Autowired val menuItemRepository: MenuItemRepository,
) {

    private lateinit var categoryA: Category
    private lateinit var categoryB: Category

    private lateinit var menuItem1: MenuItem
    private lateinit var menuItem2: MenuItem
    private lateinit var menuItem3: MenuItem
    private lateinit var menuItem4: MenuItem
    private lateinit var menuItem5: MenuItem

    @BeforeEach
    fun init() {
        categoryA = Category(1, "categoryA")
        categoryB = Category(2, "categoryB")
        categoryRepository.saveAndFlush(categoryA)
        categoryRepository.saveAndFlush(categoryB)

        menuItem1 = MenuItem(1, categoryA.id, "menu item 1", "description")
        menuItem2 = MenuItem(2, categoryA.id, "menu item 2", "description")
        menuItem3 = MenuItem(3, categoryB.id, "menu item 3", "description")
        menuItem4 = MenuItem(4, categoryB.id, "menu item 4", "description")
        menuItem5 = MenuItem(5, categoryB.id, "menu item 5", "description")
        menuItemRepository.saveAndFlush(menuItem1)
        menuItemRepository.saveAndFlush(menuItem2)
        menuItemRepository.saveAndFlush(menuItem3)
        menuItemRepository.saveAndFlush(menuItem4)
        menuItemRepository.saveAndFlush(menuItem5)
    }

    @Test
    fun canLookupMenuItemList() {
        val menuItemsAll = menuItemRepository.findAll()

        assertThat(menuItemsAll.size).isEqualTo(5)
    }

    @Test
    fun canLookupMenuItemListByCategory() {
        val categoryAMenuItems = menuItemRepository.fetch(MenuItemFetchParam(categoryId = categoryA.id))
        val categoryBMenuItems = menuItemRepository.fetch(MenuItemFetchParam(categoryId = categoryB.id))

        assertThat(categoryAMenuItems.size).isEqualTo(2)
        assertThat(categoryBMenuItems.size).isEqualTo(3)
    }

    @Test
    fun canCreateMenuItem() {
        val newMenuItem = MenuItem(6, categoryA.id, "new menu item", "description")
        val savedMenuItem = menuItemRepository.save(newMenuItem)

        val findMenuItem = menuItemRepository.findById(savedMenuItem.id).orElseThrow()

        assertThat(findMenuItem.id).isEqualTo(savedMenuItem.id)
        assertThat(findMenuItem.name).isEqualTo(savedMenuItem.name)
        assertThat(findMenuItem).isEqualTo(savedMenuItem)
    }

    @Test
    fun canUpdateMenuItem() {
        val findMenuItem = menuItemRepository.findById(menuItem1.id).orElseThrow()

        assertThat(findMenuItem.name).isEqualTo("menu item 1")

        findMenuItem.name = "new name"

        assertThat(findMenuItem.name).isEqualTo("new name")
    }

    @Test
    fun canDeleteMenuItem() {
        val allMenuItems = menuItemRepository.findAll()
        assertThat(allMenuItems.size).isEqualTo(5)

        menuItemRepository.delete(menuItem1)
        val deletedMenuItems = menuItemRepository.findAll()
        assertThat(deletedMenuItems.size).isEqualTo(4)
    }
}