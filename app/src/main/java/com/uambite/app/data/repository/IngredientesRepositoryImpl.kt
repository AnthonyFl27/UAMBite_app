package com.uambite.app.data.repository

import com.uambite.app.data.api.IngredienteExtraRequest
import com.uambite.app.data.api.IngredienteExtraResponse
import com.uambite.app.data.api.IngredientesApi
import com.uambite.app.data.api.safeApiCall
import com.uambite.app.domain.model.IngredienteExtra
import com.uambite.app.domain.repository.IngredientesRepository
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.decodeFromJsonElement
// ...
@Singleton
class IngredientesRepositoryImpl @Inject constructor(
    private val api: IngredientesApi
) : IngredientesRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun getIngredientes(): Result<List<IngredienteExtra>> {
        return safeApiCall {
            val element = api.getAll()
            val list = json.decodeFromJsonElement<List<IngredienteExtraResponse>>(
                extractContentIfPage(element)
            )
            list.map { it.toDomain() }
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
