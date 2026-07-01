package com.uambite.app.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uambite.app.domain.model.Pedido
import com.uambite.app.domain.repository.PedidosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val pedidosRepository: PedidosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = pedidosRepository.getMisPedidos()
            _uiState.value = result.fold(
                onSuccess = { pedidos ->
                    val ordenados = pedidos.sortedByDescending { it.createdAt ?: "" }
                    UiState.Success(ordenados)
                },
                onFailure = { UiState.Error(it.message ?: "Error al cargar pedidos") }
            )
        }
    }

    fun cancelar(pedidoId: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val result = pedidosRepository.cambiarEstado(pedidoId, "CANCELADO")
            _actionState.value = result.fold(
                onSuccess = { ActionState.Success("Pedido cancelado") },
                onFailure = { ActionState.Error(it.message ?: "Error al cancelar") }
            )
            load()
        }
    }

    fun confirmarRetiro(pedidoId: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val result = pedidosRepository.cambiarEstado(pedidoId, "ENTREGADO")
            _actionState.value = result.fold(
                onSuccess = { ActionState.Success("Retiro confirmado") },
                onFailure = { ActionState.Error(it.message ?: "Error al confirmar") }
            )
            load()
        }
    }

    fun eliminar(pedidoId: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val result = pedidosRepository.eliminar(pedidoId)
            _actionState.value = result.fold(
                onSuccess = { ActionState.Success("Pedido eliminado") },
                onFailure = { ActionState.Error(it.message ?: "Error al eliminar") }
            )
            load()
        }
    }

    fun resetAction() {
        _actionState.value = ActionState.Idle
    }

    sealed class UiState {
        data object Loading : UiState()
        data class Success(val pedidos: List<Pedido>) : UiState()
        data class Error(val message: String) : UiState()
    }

    sealed class ActionState {
        data object Idle : ActionState()
        data object Loading : ActionState()
        data class Success(val message: String) : ActionState()
        data class Error(val message: String) : ActionState()
    }
}
