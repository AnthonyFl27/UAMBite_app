package com.uambite.app.data.api

import kotlinx.serialization.Serializable

@Serializable
data class PedidoRequest(
    val tipoEntrega: String,
    val usuarioId: String,
    val franjaHorariaId: String? = null,
    val descuentoId: String? = null
)

@Serializable
data class PedidoResponse(
    val id: String,
    val estado: String,
    val total: Double = 0.0,
    val subtotal: Double = 0.0,
    val descuentoAplicado: Double = 0.0,
    val tipoEntrega: String? = null,
    val usuarioId: String? = null,
    val usuario: String? = null,
    val franjaHorariaId: String? = null,
    val descuentoId: String? = null,
    val localComidaId: String? = null,
    val prioridad: Int = 0,
    val detalles: List<DetallePedidoResponse> = emptyList(),
    val pago: PagoResponse? = null,
    val entrega: EntregaResponse? = null,
    val createdAt: String? = null
)

@Serializable
data class PedidoEstadoRequest(
    val estado: String
)

@Serializable
data class PrioridadRequest(
    val prioridad: Int
)

@Serializable
data class EntregaRequest(
    val ubicacion: String? = null,
    val pedidoId: String? = null
)

@Serializable
data class EntregaResponse(
    val id: String? = null,
    val estado: String? = null,
    val ubicacion: String? = null,
    val pedidoId: String? = null,
    val fechaEntrega: String? = null
)
