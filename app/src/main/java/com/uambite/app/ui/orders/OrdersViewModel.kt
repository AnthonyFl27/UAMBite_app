package com.uambite.app.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uambite.app.data.realtime.OrderStatusEvent
import com.uambite.app.data.realtime.WebSocketManager
import com.uambite.app.domain.model.Pedido
import com.uambite.app.domain.repository.PedidosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val pedidosRepository: PedidosRepository,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState

    private val _confirmadosLocalmente = MutableStateFlow<Set<String>>(emptySet())
    val confirmadosLocalmente: StateFlow<Set<String>> = _confirmadosLocalmente

    private val _pedidosResaltados = MutableStateFlow<Set<String>>(emptySet())
    val pedidosResaltados: StateFlow<Set<String>> = _pedidosResaltados

    init {
        load()
        observeStatusUpdates()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = pedidosRepository.getMisPedidos()
            _uiState.value = result.fold(
                onSuccess = { pedidos ->
                    val ordenados = pedidos.sortedByDescending { it.createdAt ?: "" }
                    ordenados.forEach { webSocketManager.subscribeToOrder(it.id) }
                    UiState.Success(ordenados)
                },
                onFailure = { UiState.Error(it.message ?: "Error al cargar pedidos") }
            )
        }
    }

    private fun observeStatusUpdates() {
        viewModelScope.launch {
            webSocketManager.events.collect { event ->
                if (event is OrderStatusEvent.PedidoUpdate) {
                    applyStatusUpdate(event.pedidoId, event.status.estado, event.status.tipoEntrega)
                }
            }
        }
    }

    private fun applyStatusUpdate(pedidoId: String, nuevoEstado: String, tipoEntrega: String?) {
        val current = _uiState.value as? UiState.Success ?: return
        val updated = current.pedidos.map { p ->
            if (p.id == pedidoId) p.copy(estado = nuevoEstado, tipoEntrega = tipoEntrega ?: p.tipoEntrega)
            else p
        }
        _uiState.value = UiState.Success(updated)
        _pedidosResaltados.value = _pedidosResaltados.value + pedidoId
    }

    fun consumirResaltado(pedidoId: String) {
        if (pedidoId in _pedidosResaltados.value) {
            _pedidosResaltados.value = _pedidosResaltados.value - pedidoId
        }
    }

    fun statusFlowFor(pedidoId: String) = webSocketManager.subscribeToOrder(pedidoId)

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
                onSuccess = {
                    _confirmadosLocalmente.value += pedidoId
                    ActionState.Success("Retiro confirmado correctamente")
                },
                onFailure = {
                    val msg = it.message ?: ""
                    if (msg.contains("403") || msg.contains("denied") || msg.contains("permisos")) {
                        _confirmadosLocalmente.value += pedidoId
                        ActionState.Success("Pedido confirmado. El local finalizará el proceso.")
                    } else {
                        ActionState.Error(msg)
                    }
                }
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
