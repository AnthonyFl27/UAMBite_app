package com.uambite.app.data.api

import kotlinx.serialization.Serializable

@Serializable
data class PagoRequest(
    val metodoPago: String,
    val pedidoId: String
)

@Serializable
data class PagoResponse(
    val id: String? = null,
    val metodoPago: String? = null,
    val monto: Double = 0.0,
    val fecha: String? = null,
    val estado: String? = null,
    val pedidoId: String? = null
)
