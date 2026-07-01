package com.uambite.app.data.api

import kotlinx.serialization.Serializable

@Serializable
data class FranjaHorariaRequest(
    val horaInicio: String,
    val horaFin: String,
    val capacidadMaxima: Int,
    val pedidosActuales: Int = 0,
    val disponible: Boolean = true,
    val localComidaId: String
)

@Serializable
data class FranjaHorariaResponse(
    val id: String,
    val horaInicio: String,
    val horaFin: String,
    val capacidadMaxima: Int = 0,
    val pedidosActuales: Int = 0,
    val disponible: Boolean = true,
    val localComidaId: String? = null,
    val localComida: String? = null
)
