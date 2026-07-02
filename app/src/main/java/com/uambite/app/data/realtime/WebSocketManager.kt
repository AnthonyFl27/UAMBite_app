package com.uambite.app.data.realtime

import android.util.Log
import com.uambite.app.BuildConfig
import com.uambite.app.data.api.PedidoResponse
import com.uambite.app.data.auth.TokenStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow

@Singleton
class WebSocketManager @Inject constructor(
    private val stompClient: StompClient,
    private val tokenStore: TokenStore
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _events = MutableSharedFlow<OrderStatusEvent>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<OrderStatusEvent> = _events.asSharedFlow()

    private val pedidoStatusFlows = ConcurrentHashMap<String, MutableStateFlow<PedidoStatusMessage?>>()
    private val localPedidoFlows = ConcurrentHashMap<String, MutableStateFlow<PedidoResponse?>>()
    private val subscribedPedidoIds = ConcurrentHashMap.newKeySet<String>()
    private val subscribedLocalIds = ConcurrentHashMap.newKeySet<String>()

    private var frameJob: Job? = null
    private var tokenJob: Job? = null
    private var reconnectJob: Job? = null
    private var manualDisconnect = false
    private var attempt = 0

    init {
        observeFrames()
        observeState()
        observeToken()
    }

    private fun observeToken() {
        tokenJob?.cancel()
        tokenJob = scope.launch {
            tokenStore.token
                .distinctUntilChanged()
                .onStart { emit(tokenStore.token.first()) }
                .collect { token ->
                    if (token.isNullOrBlank()) {
                        disconnectInternal("no-token")
                    } else {
                        manualDisconnect = false
                        reconnectJob?.cancel()
                        stompClient.connect(
                            url = wsUrl(),
                            headers = mapOf("Authorization" to "Bearer $token")
                        )
                    }
                }
        }
    }

    private fun observeFrames() {
        frameJob?.cancel()
        frameJob = scope.launch {
            stompClient.frames.collect { frame ->
                if (frame.command != "MESSAGE") return@collect
                val destination = frame.header("destination") ?: return@collect
                val body = frame.body ?: return@collect
                routeMessage(destination, body)
            }
        }
    }

    private fun observeState() {
        scope.launch {
            stompClient.state.collect { state ->
                when (state) {
                    StompConnectionState.CONNECTED -> {
                        if (!_isConnected.value) {
                            attempt = 0
                            Log.i(TAG, "STOMP connected")
                        }
                        _isConnected.value = true
                    }
                    StompConnectionState.DISCONNECTED -> {
                        val wasConnected = _isConnected.value
                        _isConnected.value = false
                        if (wasConnected) Log.i(TAG, "STOMP dropped")
                        if (!manualDisconnect) scheduleReconnect()
                    }
                    StompConnectionState.CONNECTING -> Unit
                }
            }
        }
    }

    private suspend fun routeMessage(destination: String, body: String) {
        when {
            destination.startsWith("/topic/pedido/") -> {
                val pedidoId = destination.removePrefix("/topic/pedido/").substringBefore('?')
                runCatching { json.decodeFromString(PedidoStatusMessage.serializer(), body) }
                    .onSuccess { msg ->
                        pedidoStatusFlows[pedidoId]?.value = msg
                        _events.tryEmit(OrderStatusEvent.PedidoUpdate(pedidoId, msg))
                    }
                    .onFailure { Log.w(TAG, "Bad pedido status payload: ${it.message}") }
            }
            destination.startsWith("/topic/local/") -> {
                val localId = destination.removePrefix("/topic/local/").substringBefore('?')
                runCatching { json.decodeFromString(PedidoResponse.serializer(), body) }
                    .onSuccess { pedido ->
                        localPedidoFlows[localId]?.value = pedido
                        _events.tryEmit(OrderStatusEvent.LocalUpdate(localId, pedido))
                    }
                    .onFailure { Log.w(TAG, "Bad local pedido payload: ${it.message}") }
            }
        }
    }

    fun subscribeToOrder(pedidoId: String): StateFlow<PedidoStatusMessage?> {
        val flow = pedidoStatusFlows.getOrPut(pedidoId) { MutableStateFlow(null) }
        if (subscribedPedidoIds.add(pedidoId)) {
            stompClient.subscribe("/topic/pedido/$pedidoId")
        }
        return flow.asStateFlow()
    }

    fun unsubscribeFromOrder(pedidoId: String) {
        if (subscribedPedidoIds.remove(pedidoId)) {
            stompClient.unsubscribe("/topic/pedido/$pedidoId")
        }
        pedidoStatusFlows.remove(pedidoId)
    }

    fun subscribeToLocal(localComidaId: String): StateFlow<PedidoResponse?> {
        val flow = localPedidoFlows.getOrPut(localComidaId) { MutableStateFlow(null) }
        if (subscribedLocalIds.add(localComidaId)) {
            stompClient.subscribe("/topic/local/$localComidaId")
        }
        return flow.asStateFlow()
    }

    fun unsubscribeFromLocal(localComidaId: String) {
        if (subscribedLocalIds.remove(localComidaId)) {
            stompClient.unsubscribe("/topic/local/$localComidaId")
        }
        localPedidoFlows.remove(localComidaId)
    }

    fun connect() {
        manualDisconnect = false
        scope.launch {
            val token = tokenStore.token.first()
            if (!token.isNullOrBlank()) {
                reconnectJob?.cancel()
                stompClient.connect(
                    url = wsUrl(),
                    headers = mapOf("Authorization" to "Bearer $token")
                )
            }
        }
    }

    fun disconnect() {
        disconnectInternal("manual")
    }

    private fun disconnectInternal(reason: String) {
        manualDisconnect = true
        reconnectJob?.cancel()
        reconnectJob = null
        stompClient.disconnect()
        _isConnected.value = false
        Log.i(TAG, "disconnect: $reason")
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            val delayMs = backoffDelayMs(attempt)
            attempt += 1
            Log.i(TAG, "Reconnect attempt=$attempt in ${delayMs}ms")
            delay(delayMs)
            val token = tokenStore.token.first()
            if (manualDisconnect || token.isNullOrBlank()) return@launch
            stompClient.connect(
                url = wsUrl(),
                headers = mapOf("Authorization" to "Bearer $token")
            )
        }
    }

    private fun backoffDelayMs(attempt: Int): Long {
        val base = 1000.0 * 2.0.pow(min(attempt, 5))
        return min(base.toLong(), MAX_BACKOFF_MS)
    }

    private fun wsUrl(): String {
        val base = BuildConfig.API_BASE_URL
        val wsBase = base
            .replaceFirst("https://", "wss://")
            .replaceFirst("http://", "ws://")
            .trimEnd('/')
        return "$wsBase/ws"
    }

    private companion object {
        const val TAG = "WebSocketManager"
        const val MAX_BACKOFF_MS = 30_000L
    }
}
