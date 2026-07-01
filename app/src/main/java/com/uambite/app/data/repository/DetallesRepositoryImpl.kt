package com.uambite.app.data.repository

import com.uambite.app.data.api.DetallePedidoRequest
import com.uambite.app.data.api.DetallePedidoResponse
import com.uambite.app.data.api.DetallesApi
import com.uambite.app.data.api.safeApiCall
import com.uambite.app.domain.model.DetalleIngredienteExtra
import com.uambite.app.domain.model.DetallePedido
import com.uambite.app.domain.repository.DetallesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetallesRepositoryImpl @Inject constructor(
    private val api: DetallesApi
) : DetallesRepository {

    override suspend fun save(
        pedidoId: String,
        productoId: String,
        cantidad: Int,
        ingredientesExtraIds: List<String>
    ): Result<DetallePedido> {
        return safeApiCall {
            api.save(
                DetallePedidoRequest(
                    cantidad = cantidad,
                    pedidoId = pedidoId,
                    productoId = productoId,
                    ingredientesExtraIds = ingredientesExtraIds
                )
            ).toDomain()
        }
    }

    private fun DetallePedidoResponse.toDomain(): DetallePedido = DetallePedido(
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
}
