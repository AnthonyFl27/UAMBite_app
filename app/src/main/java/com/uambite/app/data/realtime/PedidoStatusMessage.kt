package com.uambite.app.data.realtime

import com.uambite.app.data.api.PedidoResponse
import kotlinx.serialization.Serializable

@Serializable
data class PedidoStatusMessage(
    val pedidoId: String,
    val estado: String,
    val mensaje: String? = null,
    val tipoEntrega: String? = null,
    val localComidaId: String? = null,
    val timestamp: String? = null
)

object PedidoStatus {
    const val PENDIENTE = "PENDIENTE"
    const val CONFIRMADO = "CONFIRMADO"
    const val EN_PREPARACION = "EN_PREPARACION"
    const val LISTO = "LISTO"
    const val EN_CAMINO = "EN_CAMINO"
    const val ENTREGADO = "ENTREGADO"
    const val CANCELADO = "CANCELADO"

    const val RETIRO_LOCAL = "RETIRO_LOCAL"
    const val ENTREGA_INTERNA = "ENTREGA_INTERNA"

    fun mensajeLegible(estado: String, tipoEntrega: String?, mensajeOriginal: String? = null): String {
        if (!mensajeOriginal.isNullOrBlank()) return mensajeOriginal
        return when (estado) {
            CONFIRMADO -> "El local aceptó tu pedido"
            EN_PREPARACION -> "Se está preparando tu pedido"
            LISTO -> when (tipoEntrega) {
                RETIRO_LOCAL -> "Tu pedido está listo. Puedes retirarlo en el local."
                ENTREGA_INTERNA -> "Tu pedido está listo. Pronto será enviado."
                else -> "Tu pedido está listo"
            }
            EN_CAMINO -> "Se envió tu pedido"
            ENTREGADO -> "Tu pedido fue entregado"
            CANCELADO -> "Tu pedido fue cancelado"
            else -> estado
        }
    }
}

sealed class OrderStatusEvent {
    data class PedidoUpdate(
        val pedidoId: String,
        val status: PedidoStatusMessage
    ) : OrderStatusEvent()

    data class LocalUpdate(
        val localComidaId: String,
        val pedido: PedidoResponse
    ) : OrderStatusEvent()
}
