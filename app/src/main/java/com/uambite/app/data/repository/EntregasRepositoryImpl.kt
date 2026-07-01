package com.uambite.app.data.repository

import com.uambite.app.data.api.EntregaRequest
import com.uambite.app.data.api.EntregaResponse
import com.uambite.app.data.api.EntregasApi
import com.uambite.app.data.api.safeApiCall
import com.uambite.app.domain.model.Entrega
import com.uambite.app.domain.repository.EntregasRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntregasRepositoryImpl @Inject constructor(
    private val api: EntregasApi
) : EntregasRepository {

    override suspend fun crear(pedidoId: String, ubicacion: String): Result<Entrega> {
        return safeApiCall {
            api.save(
                EntregaRequest(
                    ubicacion = ubicacion,
                    pedidoId = pedidoId
                )
            ).toDomain()
        }
    }

    override suspend fun finalizar(entregaId: String): Result<Entrega> {
        return safeApiCall { api.finalizar(entregaId).toDomain() }
    }

    private fun EntregaResponse.toDomain(): Entrega = Entrega(
        id = id,
        estado = estado,
        ubicacion = ubicacion,
        pedidoId = pedidoId
    )
}
