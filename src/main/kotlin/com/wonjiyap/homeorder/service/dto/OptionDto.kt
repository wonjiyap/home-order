package com.wonjiyap.homeorder.service.dto

data class OptionListParam(
    val optionGroupId: Long,
    val hostId: Long,
)

data class OptionGetParam(
    val id: Long,
    val optionGroupId: Long,
    val hostId: Long,
)

data class OptionCreateParam(
    val optionGroupId: Long,
    val hostId: Long,
    val name: String,
)

data class OptionUpdateParam(
    val id: Long,
    val optionGroupId: Long,
    val hostId: Long,
    val name: String? = null,
)

data class OptionDeleteParam(
    val id: Long,
    val optionGroupId: Long,
    val hostId: Long,
)

data class OptionReorderParam(
    val optionGroupId: Long,
    val hostId: Long,
    val optionIds: List<Long>,
)
