package com.uambite.app.data.repository

import com.uambite.app.data.api.PagoApi
import com.uambite.app.data.api.PagoRequest
import com.uambite.app.data.api.PagoResponse
import com.uambite.app.data.api.safeApiCall
import com.uambite.app.domain.model.Pago
import com.uambite.app.domain.repository.PagosRepository
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.decodeFromJsonElement
// ...
@Singleton
class PagosRepositoryImpl @Inject constructor(
    private val api: PagoApi
) : PagosRepository {

    override suspend fun save(pedidoId: String, metodoPago: String): Result<Pago> {
        return safeApiCall {
            api.save(
                PagoRequest(
                    metodoPago = metodoPago,
                    pedidoId = pedidoId
                )
            ).toDomain()
        }
    }

    private fun PagoResponse.toDomain(): Pago = Pago(
        id = id,
        metodoPago = metodoPago,
        monto = monto,
        fecha = fecha,
        estado = estado,
        pedidoId = pedidoId
    )
}
