package com.wonjiyap.homeorder.service.dto

data class OptionGroupListParam(
    val menuId: Long,
    val hostId: Long,
)

data class OptionGroupGetParam(
    val id: Long,
    val menuId: Long,
    val hostId: Long,
)

data class OptionGroupCreateParam(
    val menuId: Long,
    val hostId: Long,
    val name: String,
    val isRequired: Boolean = false,
)

data class OptionGroupUpdateParam(
    val id: Long,
    val menuId: Long,
    val hostId: Long,
    val name: String? = null,
    val isRequired: Boolean? = null,
)

data class OptionGroupDeleteParam(
    val id: Long,
    val menuId: Long,
    val hostId: Long,
)
