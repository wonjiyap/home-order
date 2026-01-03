package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.CategoryEntity
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.repository.CategoryRepository
import com.wonjiyap.homeorder.repository.PartyRepository
import com.wonjiyap.homeorder.repository.dto.CategoryFetchOneParam
import com.wonjiyap.homeorder.repository.dto.CategoryFetchParam
import com.wonjiyap.homeorder.repository.dto.PartyFetchOneParam
import com.wonjiyap.homeorder.service.dto.CategoryCreateParam
import com.wonjiyap.homeorder.service.dto.CategoryDeleteParam
import com.wonjiyap.homeorder.service.dto.CategoryGetParam
import com.wonjiyap.homeorder.service.dto.CategoryListParam
import com.wonjiyap.homeorder.service.dto.CategoryReorderParam
import com.wonjiyap.homeorder.service.dto.CategoryUpdateParam
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val partyRepository: PartyRepository,
) {

    fun list(param: CategoryListParam): List<CategoryEntity> {
        validatePartyOwnership(param.partyId, param.hostId)
        return categoryRepository.fetch(
            CategoryFetchParam(
                partyId = param.partyId,
                withDeleted = false,
            )
        )
    }

    fun get(param: CategoryGetParam): CategoryEntity {
        validatePartyOwnership(param.partyId, param.hostId)
        return categoryRepository.fetchOne(
            CategoryFetchOneParam(
                id = param.id,
                partyId = param.partyId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다")
    }

    fun create(param: CategoryCreateParam): CategoryEntity = transaction {
        validatePartyOwnership(param.partyId, param.hostId)
        validateCategoryNameUnique(param.partyId, param.name)

        CategoryEntity.new {
            partyId = param.partyId
            name = param.name
            displayOrder = getNextDisplayOrder(param.partyId)
            createdAt = Instant.now()
            updatedAt = Instant.now()
        }
    }

    fun update(param: CategoryUpdateParam): CategoryEntity = transaction {
        validatePartyOwnership(param.partyId, param.hostId)

        val category = categoryRepository.fetchOne(
            CategoryFetchOneParam(
                id = param.id,
                partyId = param.partyId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다")

        param.name?.let { newName ->
            if (category.name != newName) {
                validateCategoryNameUnique(param.partyId, newName)
                category.name = newName
                category.updatedAt = Instant.now()
            }
        }

        category
    }

    fun delete(param: CategoryDeleteParam) = transaction {
        validatePartyOwnership(param.partyId, param.hostId)

        val category = categoryRepository.fetchOne(
            CategoryFetchOneParam(
                id = param.id,
                partyId = param.partyId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다")

        category.deletedAt = Instant.now()
    }

    fun reorder(param: CategoryReorderParam): List<CategoryEntity> = transaction {
        validatePartyOwnership(param.partyId, param.hostId)

        val categories = categoryRepository.fetch(
            CategoryFetchParam(
                partyId = param.partyId,
                withDeleted = false,
            )
        )

        val categoryMap = categories.associateBy { it.id.value }
        val now = Instant.now()

        param.categoryIds.forEachIndexed { index, categoryId ->
            val category = categoryMap[categoryId]
                ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다: $categoryId")
            category.displayOrder = index
            category.updatedAt = now
        }

        // 전달된 ID 목록에 없는 카테고리들은 뒤쪽 순서로 유지
        val remainingCategories = categories.filter { it.id.value !in param.categoryIds }
            .sortedBy { it.displayOrder }
        remainingCategories.forEachIndexed { index, category ->
            category.displayOrder = param.categoryIds.size + index
            category.updatedAt = now
        }

        categoryRepository.fetch(
            CategoryFetchParam(
                partyId = param.partyId,
                withDeleted = false,
            )
        ).sortedBy { it.displayOrder }
    }

    private fun validatePartyOwnership(partyId: Long, hostId: Long) {
        partyRepository.fetchOne(
            PartyFetchOneParam(
                id = partyId,
                hostId = hostId,
                deleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "파티를 찾을 수 없습니다")
    }

    private fun validateCategoryNameUnique(partyId: Long, name: String) {
        val existingCategory = categoryRepository.fetchOne(
            CategoryFetchOneParam(
                partyId = partyId,
                name = name,
                exactName = true,
                withDeleted = false,
            )
        )
        if (existingCategory != null) {
            throw HomeOrderException(ErrorCode.CONFLICT, "같은 이름의 카테고리가 이미 존재합니다")
        }
    }

    private fun getNextDisplayOrder(partyId: Long): Int {
        val categories = categoryRepository.fetch(
            CategoryFetchParam(
                partyId = partyId,
                withDeleted = false,
            )
        )
        return if (categories.isEmpty()) 0 else categories.maxOf { it.displayOrder } + 1
    }
}
