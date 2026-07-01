package com.uambite.app.domain.repository

import com.uambite.app.domain.model.CartItem
import com.uambite.app.domain.model.Descuento
import kotlinx.coroutines.flow.StateFlow

interface CartRepository {
    val items: StateFlow<List<CartItem>>
    val descuento: StateFlow<Descuento?>

    fun setUserId(userId: String?)

    fun agregar(
        productoId: String,
        nombre: String,
        precioBase: Double,
        cantidad: Int,
        localNombre: String,
        localComidaId: String?,
        extrasIds: List<String>,
        extrasPrecios: Map<String, Double>,
        extrasNombres: List<String>
    )

    fun cambiarCantidad(cartKey: String, delta: Int)

    fun eliminar(cartKey: String)

    fun vaciar()

    fun aplicarDescuento(descuento: Descuento)

    fun quitarDescuento()

    fun cantidadTotal(): Int
}
