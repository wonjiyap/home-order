package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.OptionGroupEntity
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.repository.CategoryRepository
import com.wonjiyap.homeorder.repository.MenuRepository
import com.wonjiyap.homeorder.repository.OptionGroupRepository
import com.wonjiyap.homeorder.repository.PartyRepository
import com.wonjiyap.homeorder.repository.dto.CategoryFetchOneParam
import com.wonjiyap.homeorder.repository.dto.MenuFetchOneParam
import com.wonjiyap.homeorder.repository.dto.OptionGroupFetchOneParam
import com.wonjiyap.homeorder.repository.dto.OptionGroupFetchParam
import com.wonjiyap.homeorder.repository.dto.PartyFetchOneParam
import com.wonjiyap.homeorder.service.dto.OptionGroupCreateParam
import com.wonjiyap.homeorder.service.dto.OptionGroupDeleteParam
import com.wonjiyap.homeorder.service.dto.OptionGroupGetParam
import com.wonjiyap.homeorder.service.dto.OptionGroupListParam
import com.wonjiyap.homeorder.service.dto.OptionGroupUpdateParam
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class OptionGroupService(
    private val optionGroupRepository: OptionGroupRepository,
    private val menuRepository: MenuRepository,
    private val categoryRepository: CategoryRepository,
    private val partyRepository: PartyRepository,
) {

    fun list(param: OptionGroupListParam): List<OptionGroupEntity> {
        validateMenuOwnership(param.menuId, param.hostId)

        return optionGroupRepository.fetch(
            OptionGroupFetchParam(
                menuId = param.menuId,
                withDeleted = false,
            )
        )
    }

    fun get(param: OptionGroupGetParam): OptionGroupEntity {
        validateMenuOwnership(param.menuId, param.hostId)

        return optionGroupRepository.fetchOne(
            OptionGroupFetchOneParam(
                id = param.id,
                menuId = param.menuId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "옵션 그룹을 찾을 수 없습니다")
    }

    fun create(param: OptionGroupCreateParam): OptionGroupEntity = transaction {
        validateMenuOwnership(param.menuId, param.hostId)
        validateOptionGroupNameUnique(param.menuId, param.name)

        OptionGroupEntity.new {
            menuId = param.menuId
            name = param.name
            isRequired = param.isRequired
            createdAt = Instant.now()
            updatedAt = Instant.now()
        }
    }

    fun update(param: OptionGroupUpdateParam): OptionGroupEntity = transaction {
        validateMenuOwnership(param.menuId, param.hostId)

        val optionGroup = optionGroupRepository.fetchOne(
            OptionGroupFetchOneParam(
                id = param.id,
                menuId = param.menuId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "옵션 그룹을 찾을 수 없습니다")

        param.name?.let { newName ->
            if (optionGroup.name != newName) {
                validateOptionGroupNameUnique(param.menuId, newName)
                optionGroup.name = newName
            }
        }
        param.isRequired?.let { optionGroup.isRequired = it }

        optionGroup.updatedAt = Instant.now()
        optionGroup
    }

    fun delete(param: OptionGroupDeleteParam) = transaction {
        validateMenuOwnership(param.menuId, param.hostId)

        val optionGroup = optionGroupRepository.fetchOne(
            OptionGroupFetchOneParam(
                id = param.id,
                menuId = param.menuId,
                withDeleted = false,
            )
        ) ?: throw HomeOrderException(ErrorCode.NOT_FOUND, "옵션 그룹을 찾을 수 없습니다")

        optionGroup.deletedAt = Instant.now()
    }

    private fun validateMenuOwnership(menuId: Long, hostId: Long) {
        val menu = menuRepository.fetchOne(
            MenuFetchOneParam(
                id = menuId,
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
        ) ?: throw HomeOrderException(ErrorCode.FORBIDDEN, "해당 메뉴에 대한 권한이 없습니다")
    }

    private fun validateOptionGroupNameUnique(menuId: Long, name: String) {
        val existingOptionGroup = optionGroupRepository.fetchOne(
            OptionGroupFetchOneParam(
                menuId = menuId,
                name = name,
                withDeleted = false,
            )
        )
        if (existingOptionGroup != null) {
            throw HomeOrderException(ErrorCode.CONFLICT, "같은 이름의 옵션 그룹이 이미 존재합니다")
        }
    }
}
