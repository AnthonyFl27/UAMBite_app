package com.uambite.app.data.api

import kotlinx.serialization.Serializable

@Serializable
data class PageDto<T>(
    val content: List<T> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val size: Int = 0,
    val number: Int = 0
)
