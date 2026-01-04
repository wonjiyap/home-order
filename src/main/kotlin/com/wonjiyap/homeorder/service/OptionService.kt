package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.OptionEntity
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.repository.CategoryRepository
import com.wonjiyap.homeorder.repository.MenuRepository
import com.wonjiyap.homeorder.repository.OptionGroupRepository
import com.wonjiyap.homeorder.repository.OptionRepository
import com.wonjiyap.homeorder.repository.PartyRepository
import com.wonjiyap.homeorder.repository.dto.CategoryFetchOneParam
import com.wonjiyap.homeorder.repository.dto.MenuFetchOneParam
import com.wonjiyap.homeorder.repository.dto.OptionFetchOneParam
import com.wonjiyap.homeorder.repository.dto.OptionFetchParam
import com.wonjiyap.homeorder.repository.dto.OptionGroupFetchOneParam
import com.wonjiyap.homeorder.repository.dto.PartyFetchOneParam
import com.wonjiyap.homeorder.service.dto.OptionCreateParam
import com.wonjiyap.homeorder.service.dto.OptionDeleteParam
import com.wonjiyap.homeorder.service.dto.OptionGetParam
import com.wonjiyap.homeorder.service.dto.OptionListParam
import com.wonjiyap.homeorder.service.dto.OptionReorderParam
import com.wonjiyap.homeorder.service.dto.OptionUpdateParam
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class OptionService(
    private val optionRepository: OptionRepository,
    private val optionGroupRepository: OptionGroupRepository,
    private val menuRepository: MenuRepository,
    private val categoryRepository: CategoryRepository,
    private val partyRepository: PartyRepository,
) {

    fun list(param: OptionListParam): List<OptionEntity> {
        validateOptionGroupOwnership(param.optionGroupId, param.hostId)

        return optionRepository.fetch(
            OptionFetchParam(
                optionGroupId = param.optionGroupId,
                withDeleted = false,
            )
        )
    }

    fun get(param: OptionGetParam): OptionEntity {
        validateOptionGroupOwnership(param.optionGroupId, param.hostId)

        return optionRepository.fetchOne(
            OptionFetchOneParam(
                id = param.id,
                optionGroupId = param.optionGroupId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "옵션을 찾을 수 없습니다")
    }

    fun create(param: OptionCreateParam): OptionEntity = transaction {
        validateOptionGroupOwnership(param.optionGroupId, param.hostId)
        validateOptionNameUnique(param.optionGroupId, param.name)

        OptionEntity.new {
            optionGroupId = param.optionGroupId
            name = param.name
            displayOrder = getNextDisplayOrder(param.optionGroupId)
            createdAt = Instant.now()
            updatedAt = Instant.now()
        }
    }

    fun update(param: OptionUpdateParam): OptionEntity = transaction {
        validateOptionGroupOwnership(param.optionGroupId, param.hostId)

        val option = optionRepository.fetchOne(
            OptionFetchOneParam(
                id = param.id,
                optionGroupId = param.optionGroupId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "옵션을 찾을 수 없습니다")

        param.name?.let { newName ->
            if (option.name != newName) {
                validateOptionNameUnique(param.optionGroupId, newName)
                option.name = newName
            }
        }

        option.updatedAt = Instant.now()
        option
    }

    fun delete(param: OptionDeleteParam) = transaction {
        validateOptionGroupOwnership(param.optionGroupId, param.hostId)

        val option = optionRepository.fetchOne(
            OptionFetchOneParam(
                id = param.id,
                optionGroupId = param.optionGroupId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "옵션을 찾을 수 없습니다")

        option.deletedAt = Instant.now()
    }

    fun reorder(param: OptionReorderParam): List<OptionEntity> = transaction {
        validateOptionGroupOwnership(param.optionGroupId, param.hostId)

        val options = optionRepository.fetch(
            OptionFetchParam(
                optionGroupId = param.optionGroupId,
                withDeleted = false,
            )
        )

        val optionMap = options.associateBy { it.id.value }
        val now = Instant.now()

        param.optionIds.forEachIndexed { index, optionId ->
            val option = optionMap[optionId]
                ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "옵션을 찾을 수 없습니다: $optionId")
            option.displayOrder = index
            option.updatedAt = now
        }

        val remainingOptions = options.filter { it.id.value !in param.optionIds }
            .sortedBy { it.displayOrder }
        remainingOptions.forEachIndexed { index, option ->
            option.displayOrder = param.optionIds.size + index
            option.updatedAt = now
        }

        optionRepository.fetch(
            OptionFetchParam(
                optionGroupId = param.optionGroupId,
                withDeleted = false,
            )
        ).sortedBy { it.displayOrder }
    }

    private fun validateOptionGroupOwnership(optionGroupId: Long, hostId: Long) {
        val optionGroup = optionGroupRepository.fetchOne(
            OptionGroupFetchOneParam(
                id = optionGroupId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "옵션 그룹을 찾을 수 없습니다")

        val menu = menuRepository.fetchOne(
            MenuFetchOneParam(
                id = optionGroup.menuId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "메뉴를 찾을 수 없습니다")

        val category = categoryRepository.fetchOne(
            CategoryFetchOneParam(
                id = menu.categoryId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다")

        partyRepository.fetchOne(
            PartyFetchOneParam(
                id = category.partyId,
                hostId = hostId,
                deleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.FORBIDDEN, "해당 옵션 그룹에 대한 권한이 없습니다")
    }

    private fun validateOptionNameUnique(optionGroupId: Long, name: String) {
        val existingOption = optionRepository.fetchOne(
            OptionFetchOneParam(
                optionGroupId = optionGroupId,
                name = name,
                withDeleted = false,
            )
        )
        if (existingOption != null) {
            throw HomeOrderException(ErrorCode.CONFLICT, "같은 이름의 옵션이 이미 존재합니다")
        }
    }

    private fun getNextDisplayOrder(optionGroupId: Long): Int {
        val options = optionRepository.fetch(
            OptionFetchParam(
                optionGroupId = optionGroupId,
                withDeleted = false,
            )
        )
        return if (options.isEmpty()) 0 else options.maxOf { it.displayOrder } + 1
    }
}
