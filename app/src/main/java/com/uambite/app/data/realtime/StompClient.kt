package com.uambite.app.data.realtime

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

enum class StompConnectionState { DISCONNECTED, CONNECTING, CONNECTED }

private data class StompSubscription(val id: String, val destination: String)

@Singleton
class StompClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val subIdCounter = AtomicInteger(0)

    private val _state = MutableStateFlow(StompConnectionState.DISCONNECTED)
    val state: StateFlow<StompConnectionState> = _state.asStateFlow()

    private val _frames = MutableSharedFlow<StompFrame>(
        replay = 0,
        extraBufferCapacity = 128,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val frames: SharedFlow<StompFrame> = _frames.asSharedFlow()

    private var socket: WebSocket? = null
    private val subscriptions = LinkedHashMap<String, StompSubscription>()

    fun connect(url: String, headers: Map<String, String>) {
        if (_state.value != StompConnectionState.DISCONNECTED) {
            Log.w(TAG, "connect() ignored, current state=${_state.value}")
            return
        }
        _state.value = StompConnectionState.CONNECTING
        Log.i(TAG, "Connecting to $url")
        val request = Request.Builder().url(url).build()
        socket = okHttpClient.newWebSocket(request, listener)
        connectHeaders = headers
    }

    private var connectHeaders: Map<String, String> = emptyMap()

    fun disconnect() {
        val s = socket ?: return
        if (_state.value == StompConnectionState.CONNECTED) {
            sendRaw(StompFrames.build("DISCONNECT", emptyMap(), null))
        }
        s.close(1000, "client-disconnect")
        socket = null
        subscriptions.clear()
        _state.value = StompConnectionState.DISCONNECTED
        Log.i(TAG, "Disconnected")
    }

    fun subscribe(destination: String): String {
        val id = "sub-${subIdCounter.incrementAndGet()}"
        val sub = StompSubscription(id, destination)
        subscriptions[destination] = sub
        if (_state.value == StompConnectionState.CONNECTED) {
            sendSubscribe(sub)
        }
        return id
    }

    fun unsubscribe(destination: String) {
        val sub = subscriptions.remove(destination) ?: return
        if (_state.value == StompConnectionState.CONNECTED) {
            sendRaw(
                StompFrames.build("UNSUBSCRIBE", mapOf("id" to sub.id), null)
            )
        }
    }

    val activeDestinations: Set<String>
        get() = subscriptions.keys.toSet()

    private fun sendSubscribe(sub: StompSubscription) {
        sendRaw(
            StompFrames.build(
                "SUBSCRIBE",
                mapOf("id" to sub.id, "destination" to sub.destination),
                null
            )
        )
    }

    private fun sendConnect() {
        val headers = buildMap {
            put("accept-version", "1.1")
            put("host", "uambite")
            put("heart-beat", "10000,10000")
            putAll(connectHeaders)
        }
        sendRaw(StompFrames.build("CONNECT", headers, null))
    }

    private fun sendRaw(text: String) {
        val s = socket ?: return
        val ok = s.send(text)
        if (!ok) Log.w(TAG, "send() returned false")
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.i(TAG, "WebSocket opened")
            sendConnect()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val frame = StompFrames.parse(text) ?: return
            when (frame.command) {
                "CONNECTED" -> {
                    Log.i(TAG, "STOMP CONNECTED")
                    _state.value = StompConnectionState.CONNECTED
                    subscriptions.values.forEach { sendSubscribe(it) }
                }
                "MESSAGE" -> {
                    _frames.tryEmit(frame)
                }
                "ERROR" -> {
                    Log.w(TAG, "STOMP ERROR: ${frame.body}")
                    webSocket.close(1011, "stomp-error")
                }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.w(TAG, "WebSocket failure: ${t.message}")
            socket = null
            _state.value = StompConnectionState.DISCONNECTED
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.i(TAG, "WebSocket closed code=$code reason=$reason")
            socket = null
            _state.value = StompConnectionState.DISCONNECTED
        }
    }

    private companion object {
        const val TAG = "StompClient"
    }
}
