package com.uambite.app.data.api

import com.uambite.app.domain.model.DetalleIngredienteExtra
import com.uambite.app.domain.model.DetallePedido
import com.uambite.app.domain.model.Entrega
import com.uambite.app.domain.model.Pago
import com.uambite.app.domain.model.Pedido

fun PedidoResponse.toDomain(): Pedido = Pedido(
    id = id,
    estado = estado,
    total = total,
    subtotal = subtotal,
    descuentoAplicado = descuentoAplicado,
    tipoEntrega = tipoEntrega,
    usuarioId = usuarioId,
    usuario = usuario,
    franjaHorariaId = franjaHorariaId,
    descuentoId = descuentoId,
    localComidaId = localComidaId,
    detalles = detalles.map { it.toDomain() },
    pago = pago?.toDomain(),
    entrega = entrega?.toDomain(),
    createdAt = createdAt
)

fun DetallePedidoResponse.toDomain(): DetallePedido = DetallePedido(
    id = id,
    cantidad = cantidad,
    precioUnitario = precioUnitario,
    subtotal = subtotal,
    producto = producto,
    productoId = productoId,
    ingredientesExtra = ingredientesExtra.map { it.toDomain() }
)

fun DetallePedidoIngredienteExtraResponse.toDomain(): DetalleIngredienteExtra =
    DetalleIngredienteExtra(
        id = id,
        ingredienteExtraId = ingredienteExtraId,
        nombre = nombre,
        precioExtra = precioAdicional
    )

fun PagoResponse.toDomain(): Pago = Pago(
    id = id,
    metodoPago = metodoPago,
    monto = monto,
    fecha = fecha,
    estado = estado,
    pedidoId = pedidoId
)

fun EntregaResponse.toDomain(): Entrega = Entrega(
    id = id,
    estado = estado,
    ubicacion = ubicacion,
    pedidoId = pedidoId
)
