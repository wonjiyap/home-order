package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.Category
import com.wonjiyap.homeorder.domain.MenuItem
import com.wonjiyap.homeorder.domain.MenuOption
import com.wonjiyap.homeorder.repository.dto.menuOption.MenuOptionFetchParam
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

    private lateinit var categoryA: Category

    private lateinit var  menuItem1: MenuItem
    private lateinit var  menuItem2: MenuItem

    private lateinit var  menuOption1: MenuOption
    private lateinit var  menuOption2: MenuOption
    private lateinit var  menuOption3: MenuOption
    private lateinit var  menuOption4: MenuOption
    private lateinit var  menuOption5: MenuOption

    @BeforeEach
    fun init() {
        categoryA = Category(1, "category 1")
        categoryRepository.saveAndFlush(categoryA)

        menuItem1 = MenuItem(1, categoryA.id, "menu item 1", "description")
        menuItem2 = MenuItem(2, categoryA.id, "menu item 2", "description")
        menuItemRepository.saveAndFlush(menuItem1)
        menuItemRepository.saveAndFlush(menuItem2)

        menuOption1 = MenuOption(1, menuItem1.id, "menu option 1")
        menuOption2 = MenuOption(2, menuItem1.id, "menu option 2")
        menuOption3 = MenuOption(3, menuItem2.id, "menu option 3")
        menuOption4 = MenuOption(4, menuItem2.id, "menu option 4")
        menuOption5 = MenuOption(5, menuItem2.id, "menu option 5")
        menuOptionRepository.saveAndFlush(menuOption1)
        menuOptionRepository.saveAndFlush(menuOption2)
        menuOptionRepository.saveAndFlush(menuOption3)
        menuOptionRepository.saveAndFlush(menuOption4)
        menuOptionRepository.saveAndFlush(menuOption5)
    }

    @Test
    fun canLookupMenuOptionList() {
        val menuOptionsAll = menuOptionRepository.findAll()

        assertThat(menuOptionsAll.size).isEqualTo(5)
    }

    @Test
    fun canLookupMenuOptionListByMenuItem() {
        val menuItem1Options = menuOptionRepository.fetch(MenuOptionFetchParam(menuItemId = menuItem1.id))
        val menuItem2Options = menuOptionRepository.fetch(MenuOptionFetchParam(menuItemId = menuItem2.id))

        assertThat(menuItem1Options.size).isEqualTo(2)
        assertThat(menuItem2Options.size).isEqualTo(3)
    }

    @Test
    fun canCreateMenuOption() {
        val newMenuOption = MenuOption(6, menuItem1.id, "new menu option")
        val savedMenuOption = menuOptionRepository.save(newMenuOption)

        val findMenuOption = menuOptionRepository.findById(savedMenuOption.id).orElseThrow()

        assertThat(findMenuOption.id).isEqualTo(savedMenuOption.id)
        assertThat(findMenuOption.description).isEqualTo(savedMenuOption.description)
        assertThat(findMenuOption).isEqualTo(savedMenuOption)
    }

    @Test
    fun canUpdateMenuOption() {
        val findMenuOption = menuOptionRepository.findById(menuOption1.id).orElseThrow()

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