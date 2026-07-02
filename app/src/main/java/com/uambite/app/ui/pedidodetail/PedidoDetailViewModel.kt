package com.uambite.app.ui.pedidodetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uambite.app.data.realtime.PedidoStatusMessage
import com.uambite.app.data.realtime.WebSocketManager
import com.uambite.app.domain.model.Pedido
import com.uambite.app.domain.repository.PedidosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PedidoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pedidosRepository: PedidosRepository,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    val pedidoId: String = checkNotNull(savedStateHandle["pedidoId"]) {
        "pedidoId arg required"
    }

    private val _pedido = MutableStateFlow<Pedido?>(null)
    val pedido: StateFlow<Pedido?> = _pedido

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val statusLive: StateFlow<PedidoStatusMessage?> =
        webSocketManager.subscribeToOrder(pedidoId)
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val viewState: StateFlow<ViewState> = combine(
        _pedido, _loading, _error, statusLive
    ) { pedido, loading, error, status ->
        ViewState(pedido, loading, error, status)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ViewState(null, true, null, null))

    init {
        load()
        viewModelScope.launch {
            statusLive.collect { msg ->
                if (msg != null) {
                    _pedido.value?.let { current ->
                        _pedido.value = current.copy(
                            estado = msg.estado,
                            tipoEntrega = msg.tipoEntrega ?: current.tipoEntrega
                        )
                    }
                }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val result = pedidosRepository.getById(pedidoId)
            _pedido.value = result.getOrNull()
            _error.value = result.exceptionOrNull()?.message
            _loading.value = false
        }
    }

    fun confirmarRetiro() {
        viewModelScope.launch {
            pedidosRepository.cambiarEstado(pedidoId, "ENTREGADO")
            load()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // The shared StateFlow for this pedidoId is kept alive on the manager
        // because OrdersViewModel may also be subscribed. If you want strict
        // cleanup, uncomment the next line.
        // webSocketManager.unsubscribeFromOrder(pedidoId)
    }

    data class ViewState(
        val pedido: Pedido?,
        val loading: Boolean,
        val error: String?,
        val status: PedidoStatusMessage?
    )
}
