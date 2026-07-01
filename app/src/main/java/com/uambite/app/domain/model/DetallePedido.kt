package com.uambite.app.domain.model

data class DetallePedido(
    val id: String?,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double,
    val producto: String?,
    val productoId: String?,
    val ingredientesExtra: List<DetalleIngredienteExtra>
)

data class DetalleIngredienteExtra(
    val id: String?,
    val ingredienteExtraId: String?,
    val nombre: String?,
    val precioExtra: Double
)
