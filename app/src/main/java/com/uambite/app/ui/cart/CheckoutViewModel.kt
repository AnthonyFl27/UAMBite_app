package com.uambite.app.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uambite.app.domain.model.CartItem
import com.uambite.app.domain.repository.CartRepository
import com.uambite.app.domain.repository.DetallesRepository
import com.uambite.app.domain.repository.PagosRepository
import com.uambite.app.domain.repository.PedidosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val pedidosRepository: PedidosRepository,
    private val detallesRepository: DetallesRepository,
    private val pagosRepository: PagosRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _state = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val state: StateFlow<CheckoutState> = _state

    fun confirmar(
        usuarioId: String,
        tipoEntrega: String,
        metodoPago: String,
        franjaHorariaId: String?
    ) {
        val items = cartRepository.items.value
        if (items.isEmpty()) {
            _state.value = CheckoutState.Error("El carrito está vacío")
            return
        }
        viewModelScope.launch {
            _state.value = CheckoutState.Loading
            val descuentoId = cartRepository.descuento.value?.id

            val pedidoResult = pedidosRepository.save(
                tipoEntrega = tipoEntrega,
                usuarioId = usuarioId,
                franjaHorariaId = franjaHorariaId,
                descuentoId = descuentoId
            )

            val pedido = pedidoResult.getOrElse {
                _state.value = CheckoutState.Error(it.message ?: "Error al crear el pedido")
                return@launch
            }

            val detallesError = guardarDetalles(pedido.id, items)
            if (detallesError != null) {
                _state.value = CheckoutState.Error(detallesError)
                return@launch
            }

            val pagoResult = pagosRepository.save(pedido.id, metodoPago)
            if (pagoResult.isFailure) {
                _state.value = CheckoutState.Error(
                    pagoResult.exceptionOrNull()?.message ?: "Error al registrar el pago"
                )
                return@launch
            }

            cartRepository.vaciar()
            _state.value = CheckoutState.Success(pedidoId = pedido.id)
        }
    }

    private suspend fun guardarDetalles(pedidoId: String, items: List<CartItem>): String? {
        for (item in items) {
            val result = detallesRepository.save(
                pedidoId = pedidoId,
                productoId = item.productoId,
                cantidad = item.cantidad,
                ingredientesExtraIds = item.extrasIds
            )
            if (result.isFailure) {
                return result.exceptionOrNull()?.message ?: "Error al guardar detalle del producto ${item.nombre}"
            }
        }
        return null
    }

    fun reset() {
        _state.value = CheckoutState.Idle
    }

    sealed class CheckoutState {
        data object Idle : CheckoutState()
        data object Loading : CheckoutState()
        data class Success(val pedidoId: String) : CheckoutState()
        data class Error(val message: String) : CheckoutState()
    }
}
