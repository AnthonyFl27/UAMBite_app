package com.uambite.app.data.api

import kotlinx.serialization.Serializable

@Serializable
data class DetallePedidoRequest(
    val cantidad: Int,
    val pedidoId: String,
    val productoId: String,
    val ingredientesExtraIds: List<String> = emptyList()
)

@Serializable
data class DetallePedidoResponse(
    val id: String? = null,
    val cantidad: Int = 0,
    val precioUnitario: Double = 0.0,
    val subtotal: Double = 0.0,
    val producto: String? = null,
    val productoId: String? = null,
    val ingredientesExtra: List<DetallePedidoIngredienteExtraResponse> = emptyList()
)

@Serializable
data class DetallePedidoIngredienteExtraResponse(
    val id: String? = null,
    val ingredienteExtraId: String? = null,
    val nombre: String? = null,
    val precioAdicional: Double = 0.0
)
