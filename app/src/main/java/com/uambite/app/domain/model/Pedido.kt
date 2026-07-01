package com.uambite.app.domain.model

data class Pedido(
    val id: String,
    val estado: String,
    val total: Double,
    val subtotal: Double,
    val descuentoAplicado: Double,
    val tipoEntrega: String?,
    val usuarioId: String?,
    val usuario: String?,
    val franjaHorariaId: String?,
    val descuentoId: String?,
    val localComidaId: String?,
    val prioridad: Int,
    val detalles: List<DetallePedido>,
    val pago: Pago?,
    val entrega: Entrega?,
    val createdAt: String?
)
