package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.Category
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import org.assertj.core.api.Assertions.*

@SpringBootTest
@Transactional
class CategoryRepositoryTest(
    @Autowired private val categoryRepository: CategoryRepository
) {

    @Test
    fun canLookUpCategoryList() {
        val category1 = Category(1, "category 1")
        val category2 = Category(2, "category 2")
        categoryRepository.save(category1)
        categoryRepository.save(category2)

        val allCategories = categoryRepository.findAll()
        assertThat(allCategories.size).isEqualTo(2)
    }

    @Test
    fun canCreateCategory() {
        val category = Category(1, "category")
        val savedCategory = categoryRepository.save(category)

        val findCategory = categoryRepository.findById(savedCategory.id).orElseThrow()

        assertThat(findCategory.id).isEqualTo(savedCategory.id)
        assertThat(findCategory.name).isEqualTo(savedCategory.name)
        assertThat(findCategory).isEqualTo(savedCategory)
    }

    @Test
    fun canUpdateCategory() {
        val category = Category(1, "category")
        val savedCategory = categoryRepository.save(category)
        val findCategory = categoryRepository.findById(savedCategory.id).orElseThrow()

        findCategory.name = "new name"

        assertThat(findCategory.name).isEqualTo("new name")
    }

    @Test
    fun canDeleteCategory() {
        val category1 = Category(1, "category 1")
        val category2 = Category(2, "category 2")
        categoryRepository.save(category1)
        categoryRepository.save(category2)
        val allCategories = categoryRepository.findAll()
        assertThat(allCategories.size).isEqualTo(2)

        categoryRepository.delete(category1)
        val deleteCategories = categoryRepository.findAll()
        assertThat(deleteCategories.size).isEqualTo(1)
    }
}