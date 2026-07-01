package com.uambite.app.domain.model

data class CartItem(
    val cartKey: String,
    val productoId: String,
    val nombre: String,
    val precioUnitario: Double,
    val cantidad: Int,
    val localNombre: String,
    val localComidaId: String?,
    val extrasIds: List<String>,
    val extrasNombres: List<String>
) {
    val subtotal: Double get() = precioUnitario * cantidad
}
