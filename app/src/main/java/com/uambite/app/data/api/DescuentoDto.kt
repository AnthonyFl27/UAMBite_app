package com.uambite.app.data.api

import kotlinx.serialization.Serializable

@Serializable
data class DescuentoRequest(
    val codigo: String,
    val porcentaje: Double,
    val fechaVencimiento: String? = null,
    val activo: Boolean,
    val localComidaId: String? = null
)

@Serializable
data class DescuentoResponse(
    val id: String,
    val codigo: String,
    val porcentaje: Double,
    val fechaVencimiento: String? = null,
    val activo: Boolean = true,
    val localComida: String? = null,
    val localComidaId: String? = null
)
