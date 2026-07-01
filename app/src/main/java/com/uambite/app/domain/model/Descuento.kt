package com.uambite.app.domain.model

data class Descuento(
    val id: String,
    val codigo: String,
    val porcentaje: Double,
    val fechaVencimiento: String?,
    val activo: Boolean,
    val localComida: String?,
    val localComidaId: String?
)
