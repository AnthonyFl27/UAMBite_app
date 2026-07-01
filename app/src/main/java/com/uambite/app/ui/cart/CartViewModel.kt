package com.uambite.app.ui.cart

import androidx.lifecycle.ViewModel
import com.uambite.app.domain.model.CartItem
import com.uambite.app.domain.model.Descuento
import com.uambite.app.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    val items: StateFlow<List<CartItem>> = cartRepository.items
    val descuento: StateFlow<Descuento?> = cartRepository.descuento

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
    ) {
        cartRepository.agregar(
            productoId = productoId,
            nombre = nombre,
            precioBase = precioBase,
            cantidad = cantidad,
            localNombre = localNombre,
            localComidaId = localComidaId,
            extrasIds = extrasIds,
            extrasPrecios = extrasPrecios,
            extrasNombres = extrasNombres
        )
    }

    fun cambiarCantidad(cartKey: String, delta: Int) {
        cartRepository.cambiarCantidad(cartKey, delta)
    }

    fun eliminar(cartKey: String) {
        cartRepository.eliminar(cartKey)
    }

    fun vaciar() {
        cartRepository.vaciar()
    }

    fun aplicarDescuento(descuento: Descuento) {
        cartRepository.aplicarDescuento(descuento)
    }

    fun quitarDescuento() {
        cartRepository.quitarDescuento()
    }

    fun subtotal(): Double = items.value.sumOf { it.subtotal }
    fun montoDescuento(): Double {
        val d = descuento.value ?: return 0.0
        return subtotal() * d.porcentaje / 100.0
    }
    fun total(): Double = subtotal() - montoDescuento()
    fun cantidadTotal(): Int = items.value.sumOf { it.cantidad }
}
