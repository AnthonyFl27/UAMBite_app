package com.uambite.app.data.repository

import com.uambite.app.data.api.PedidoEstadoRequest
import com.uambite.app.data.api.PedidoRequest
import com.uambite.app.data.api.PedidoResponse
import com.uambite.app.data.api.PedidosApi
import com.uambite.app.data.api.PrioridadRequest
import com.uambite.app.data.api.safeApiCall
import com.uambite.app.domain.model.DetalleIngredienteExtra
import com.uambite.app.domain.model.DetallePedido
import com.uambite.app.domain.model.Entrega
import com.uambite.app.domain.model.Pago
import com.uambite.app.domain.model.Pedido
import com.uambite.app.domain.repository.PedidosRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PedidosRepositoryImpl @Inject constructor(
    private val api: PedidosApi
) : PedidosRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun getMisPedidos(): Result<List<Pedido>> {
        return safeApiCall {
            val element = api.getMisPedidos()
            val pedidos = json.decodeFromJsonElement<List<PedidoResponse>>(
                extractContentIfPage(element)
            )
            pedidos.map { it.toDomain() }
        }
    }

    override suspend fun getAllPedidos(): Result<List<Pedido>> {
        return safeApiCall {
            val element = api.getAll()
            val list = json.decodeFromJsonElement<List<PedidoResponse>>(
                extractContentIfPage(element)
            )
            list.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: String): Result<Pedido> {
        return safeApiCall {
            api.getById(id).toDomain()
        }
    }

    override suspend fun save(
        tipoEntrega: String,
        usuarioId: String,
        franjaHorariaId: String?,
        descuentoId: String?
    ): Result<Pedido> {
        return safeApiCall {
            api.save(
                PedidoRequest(
                    tipoEntrega = tipoEntrega,
                    usuarioId = usuarioId,
                    franjaHorariaId = franjaHorariaId,
                    descuentoId = descuentoId
                )
            ).toDomain()
        }
    }

    override suspend fun cambiarEstado(id: String, estado: String): Result<Pedido> {
        return safeApiCall {
            when (estado) {
                "CONFIRMADO" -> api.confirmar(id).toDomain()
                "EN_PREPARACION" -> api.preparar(id).toDomain()
                "LISTO" -> api.listo(id).toDomain()
                "ENTREGADO" -> api.entregar(id).toDomain()
                "CANCELADO" -> api.cancelar(id).toDomain()
                else -> throw Exception("Estado no soportado: $estado")
            }
        }
    }

    override suspend fun setPrioridad(id: String, prioridad: Int): Result<Pedido> {
        return safeApiCall {
            api.setPrioridad(id, PrioridadRequest(prioridad.coerceAtLeast(0))).toDomain()
        }
    }

    override suspend fun eliminar(id: String): Result<Unit> {
        return safeApiCall {
            api.eliminar(id)
        }
    }

    private fun extractContentIfPage(element: JsonElement): JsonElement {
        return try {
            val obj = element.jsonObject
            obj["content"] ?: element
        } catch (_: Exception) {
            element
        }
    }

    private fun PedidoResponse.toDomain(): Pedido = Pedido(
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
        prioridad = prioridad,
        detalles = detalles.map { it.toDomain() },
        pago = pago?.toDomain(),
        entrega = entrega?.toDomain(),
        createdAt = createdAt
    )

    private fun com.uambite.app.data.api.DetallePedidoResponse.toDomain(): DetallePedido =
        DetallePedido(
            id = id,
            cantidad = cantidad,
            precioUnitario = precioUnitario,
            subtotal = subtotal,
            producto = producto,
            productoId = productoId,
            ingredientesExtra = ingredientesExtra.map { ie ->
                DetalleIngredienteExtra(
                    id = ie.id,
                    ingredienteExtraId = ie.ingredienteExtraId,
                    nombre = ie.nombre,
                    precioExtra = ie.precioAdicional
                )
            }
        )

    private fun com.uambite.app.data.api.PagoResponse.toDomain(): Pago = Pago(
        id = id,
        metodoPago = metodoPago,
        monto = monto,
        fecha = fecha,
        estado = estado,
        pedidoId = pedidoId
    )

    private fun com.uambite.app.data.api.EntregaResponse.toDomain(): Entrega = Entrega(
        id = id,
        estado = estado,
        ubicacion = ubicacion,
        pedidoId = pedidoId
    )
}
