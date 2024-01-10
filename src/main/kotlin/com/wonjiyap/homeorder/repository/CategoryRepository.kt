package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository: JpaRepository<Category,Long> {
}