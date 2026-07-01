package com.uambite.app.data.repository

import com.uambite.app.data.api.FranjaHorariaRequest
import com.uambite.app.data.api.FranjaHorariaResponse
import com.uambite.app.data.api.FranjasApi
import com.uambite.app.data.api.safeApiCall
import com.uambite.app.domain.model.FranjaHoraria
import com.uambite.app.domain.repository.FranjasRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FranjasRepositoryImpl @Inject constructor(
    private val api: FranjasApi
) : FranjasRepository {

    override suspend fun getDisponibles(): Result<List<FranjaHoraria>> {
        return safeApiCall { api.getDisponibles().map { it.toDomain() } }
    }

    override suspend fun getAll(): Result<List<FranjaHoraria>> {
        return safeApiCall { api.getAll().content.map { it.toDomain() } }
    }

    override suspend fun crear(
        horaInicio: String,
        horaFin: String,
        capacidadMaxima: Int,
        localComidaId: String
    ): Result<FranjaHoraria> {
        return safeApiCall {
            api.save(
                FranjaHorariaRequest(
                    horaInicio = horaInicio,
                    horaFin = horaFin,
                    capacidadMaxima = capacidadMaxima,
                    localComidaId = localComidaId
                )
            ).toDomain()
        }
    }

    override suspend fun actualizar(
        id: String,
        horaInicio: String,
        horaFin: String,
        capacidadMaxima: Int,
        disponible: Boolean,
        localComidaId: String
    ): Result<FranjaHoraria> {
        return safeApiCall {
            api.update(
                id = id,
                request = FranjaHorariaRequest(
                    horaInicio = horaInicio,
                    horaFin = horaFin,
                    capacidadMaxima = capacidadMaxima,
                    disponible = disponible,
                    localComidaId = localComidaId
                )
            ).toDomain()
        }
    }

    override suspend fun eliminar(id: String): Result<Unit> {
        return safeApiCall { api.eliminar(id) }
    }

    private fun FranjaHorariaResponse.toDomain(): FranjaHoraria = FranjaHoraria(
        id = id,
        horaInicio = horaInicio,
        horaFin = horaFin,
        capacidadMaxima = capacidadMaxima,
        pedidosActuales = pedidosActuales,
        disponible = disponible,
        localComidaId = localComidaId,
        localComida = localComida
    )
}
