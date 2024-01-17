package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.Category
import com.wonjiyap.homeorder.domain.MenuItem
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

    private val categoryA = Category("categoryA")
    private val categoryB = Category("categoryB")

    private val menuItem1 = MenuItem(categoryA, "menu item 1", "description")
    private val menuItem2 = MenuItem(categoryA, "menu item 2", "description")
    private val menuItem3 = MenuItem(categoryB, "menu item 3", "description")
    private val menuItem4 = MenuItem(categoryB, "menu item 4", "description")
    private val menuItem5 = MenuItem(categoryB, "menu item 5", "description")

    @BeforeEach
    fun init() {
        categoryRepository.save(categoryA)
        categoryRepository.save(categoryB)

        menuItemRepository.save(menuItem1)
        menuItemRepository.save(menuItem2)
        menuItemRepository.save(menuItem3)
        menuItemRepository.save(menuItem4)
        menuItemRepository.save(menuItem5)
    }

    @Test
    fun canLookupMenuItemList() {
        val menuItemsAll = menuItemRepository.findAll()

        assertThat(menuItemsAll.size).isEqualTo(5)
    }

    @Test
    fun canLookupMenuItemListByCategory() {
        val categoryAMenuItems = menuItemRepository.findByCategory(categoryA)
        val categoryBMenuItems = menuItemRepository.findByCategory(categoryB)

        assertThat(categoryAMenuItems.size).isEqualTo(2)
        assertThat(categoryBMenuItems.size).isEqualTo(3)
    }

    @Test
    fun canCreateMenuItem() {
        val newMenuItem = MenuItem(categoryA, "new menu item", "description")
        val savedMenuItem = menuItemRepository.save(newMenuItem)

        val findMenuItem = menuItemRepository.findById(savedMenuItem.id!!).orElseThrow()

        assertThat(findMenuItem.id).isEqualTo(savedMenuItem.id)
        assertThat(findMenuItem.name).isEqualTo(savedMenuItem.name)
        assertThat(findMenuItem).isEqualTo(savedMenuItem)
    }

    @Test
    fun canUpdateMenuItem() {
        val findMenuItem = menuItemRepository.findById(menuItem1.id!!).orElseThrow()

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