package com.uambite.app.domain.model

data class Pago(
    val id: String?,
    val metodoPago: String?,
    val monto: Double,
    val fecha: String?,
    val estado: String?,
    val pedidoId: String?
)

data class Entrega(
    val id: String?,
    val estado: String?,
    val ubicacion: String?,
    val pedidoId: String?
)
