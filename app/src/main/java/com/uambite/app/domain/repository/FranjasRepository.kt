package com.uambite.app.domain.repository

import com.uambite.app.domain.model.FranjaHoraria

interface FranjasRepository {
    suspend fun getDisponibles(): Result<List<FranjaHoraria>>
    suspend fun getAll(): Result<List<FranjaHoraria>>
    suspend fun crear(
        horaInicio: String,
        horaFin: String,
        capacidadMaxima: Int,
        localComidaId: String
    ): Result<FranjaHoraria>
    suspend fun actualizar(
        id: String,
        horaInicio: String,
        horaFin: String,
        capacidadMaxima: Int,
        disponible: Boolean,
        localComidaId: String
    ): Result<FranjaHoraria>
    suspend fun eliminar(id: String): Result<Unit>
}
