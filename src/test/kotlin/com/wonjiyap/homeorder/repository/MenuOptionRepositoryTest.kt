package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.Category
import com.wonjiyap.homeorder.domain.MenuItem
import com.wonjiyap.homeorder.domain.MenuOption
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import org.assertj.core.api.Assertions.*

@SpringBootTest
@Transactional
class MenuOptionRepositoryTest(
    @Autowired val categoryRepository: CategoryRepository,
    @Autowired val menuItemRepository: MenuItemRepository,
    @Autowired val menuOptionRepository: MenuOptionRepository,
) {

    private val categoryA = Category("categoryA")

    private val menuItem1 = MenuItem(categoryA, "menu item 1", "description")
    private val menuItem2 = MenuItem(categoryA, "menu item 2", "description")

    private val menuOption1 = MenuOption(menuItem1, "menu option 1")
    private val menuOption2 = MenuOption(menuItem1, "menu option 2")
    private val menuOption3 = MenuOption(menuItem2, "menu option 3")
    private val menuOption4 = MenuOption(menuItem2, "menu option 4")
    private val menuOption5 = MenuOption(menuItem2, "menu option 5")

    @BeforeEach
    fun init() {
        categoryRepository.save(categoryA)

        menuItemRepository.save(menuItem1)
        menuItemRepository.save(menuItem2)

        menuOptionRepository.save(menuOption1)
        menuOptionRepository.save(menuOption2)
        menuOptionRepository.save(menuOption3)
        menuOptionRepository.save(menuOption4)
        menuOptionRepository.save(menuOption5)
    }

    @Test
    fun canLookupMenuOptionList() {
        val menuOptionsAll = menuOptionRepository.findAll()

        assertThat(menuOptionsAll.size).isEqualTo(5)
    }

    @Test
    fun canLookupMenuOptionListByMenuItem() {
        val menuItem1Options = menuOptionRepository.findByMenuItem(menuItem1)
        val menuItem2Options = menuOptionRepository.findByMenuItem(menuItem2)

        assertThat(menuItem1Options.size).isEqualTo(2)
        assertThat(menuItem2Options.size).isEqualTo(3)
    }

    @Test
    fun canCreateMenuOption() {
        val newMenuOption = MenuOption(menuItem1, "new menu option")
        val savedMenuOption = menuOptionRepository.save(newMenuOption)

        val findMenuOption = menuOptionRepository.findById(savedMenuOption.id!!).orElseThrow()

        assertThat(findMenuOption.id).isEqualTo(savedMenuOption.id)
        assertThat(findMenuOption.description).isEqualTo(savedMenuOption.description)
        assertThat(findMenuOption).isEqualTo(savedMenuOption)
    }

    @Test
    fun canUpdateMenuOption() {
        val findMenuOption = menuOptionRepository.findById(menuOption1.id!!).orElseThrow()

        assertThat(findMenuOption.description).isEqualTo("menu option 1")

        findMenuOption.description = "new description"

        assertThat(findMenuOption.description).isEqualTo("new description")
    }

    @Test
    fun canDeleteMenuOption() {
        val allMenuOptions = menuOptionRepository.findAll()
        assertThat(allMenuOptions.size).isEqualTo(5)

        menuOptionRepository.delete(menuOption1)
        val deletedMenuOptions = menuOptionRepository.findAll()
        assertThat(deletedMenuOptions.size).isEqualTo(4)
    }
}