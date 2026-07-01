package com.uambite.app.data.repository

import com.uambite.app.data.api.IngredienteExtraRequest
import com.uambite.app.data.api.IngredienteExtraResponse
import com.uambite.app.data.api.IngredientesApi
import com.uambite.app.data.api.safeApiCall
import com.uambite.app.domain.model.IngredienteExtra
import com.uambite.app.domain.repository.IngredientesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IngredientesRepositoryImpl @Inject constructor(
    private val api: IngredientesApi
) : IngredientesRepository {

    override suspend fun getIngredientes(): Result<List<IngredienteExtra>> {
        return safeApiCall { api.getAll().content.map { it.toDomain() } }
    }

    override suspend fun getById(id: String): Result<IngredienteExtra> {
        return safeApiCall { api.getById(id).toDomain() }
    }

    override suspend fun crear(nombre: String, precioExtra: Double): Result<IngredienteExtra> {
        return safeApiCall {
            api.save(IngredienteExtraRequest(nombre, precioExtra)).toDomain()
        }
    }

    override suspend fun actualizar(
        id: String,
        nombre: String,
        precioExtra: Double
    ): Result<IngredienteExtra> {
        return safeApiCall {
            api.update(
                id = id,
                request = IngredienteExtraRequest(nombre, precioExtra)
            ).toDomain()
        }
    }

    override suspend fun eliminar(id: String): Result<Unit> {
        return safeApiCall { api.eliminar(id) }
    }

    private fun IngredienteExtraResponse.toDomain(): IngredienteExtra = IngredienteExtra(
        id = id,
        nombre = nombre,
        precioExtra = precioExtra,
        tieneImagen = tieneImagen
    )
}
