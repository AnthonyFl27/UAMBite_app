package com.uambite.app.data.repository

import com.uambite.app.data.api.DescuentoRequest
import com.uambite.app.data.api.DescuentoResponse
import com.uambite.app.data.api.DescuentosApi
import com.uambite.app.data.api.safeApiCall
import com.uambite.app.domain.model.Descuento
import com.uambite.app.domain.repository.DescuentosRepository
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.decodeFromJsonElement
// ...
@Singleton
class DescuentosRepositoryImpl @Inject constructor(
    private val api: DescuentosApi
) : DescuentosRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun getAll(): Result<List<Descuento>> {
        return safeApiCall {
            val element = api.getAll()
            val list = json.decodeFromJsonElement<List<DescuentoResponse>>(
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

    override suspend fun buscarPorCodigo(codigo: String): Result<Descuento?> {
        return safeApiCall {
            val element = api.getAll()
            val list = json.decodeFromJsonElement<List<DescuentoResponse>>(
                extractContentIfPage(element)
            )
            list.find { it.codigo.equals(codigo, ignoreCase = true) }?.toDomain()
        }
    }

    override suspend fun crear(
        codigo: String,
        porcentaje: Double,
        fechaVencimiento: String?,
        activo: Boolean,
        localComidaId: String?
    ): Result<Descuento> {
        return safeApiCall {
            api.save(
                DescuentoRequest(
                    codigo = codigo,
                    porcentaje = porcentaje,
                    fechaVencimiento = fechaVencimiento,
                    activo = activo,
                    localComidaId = localComidaId
                )
            ).toDomain()
        }
    }

    override suspend fun actualizar(
        id: String,
        codigo: String,
        porcentaje: Double,
        fechaVencimiento: String?,
        activo: Boolean,
        localComidaId: String?
    ): Result<Descuento> {
        return safeApiCall {
            api.update(
                id = id,
                request = DescuentoRequest(
                    codigo = codigo,
                    porcentaje = porcentaje,
                    fechaVencimiento = fechaVencimiento,
                    activo = activo,
                    localComidaId = localComidaId
                )
            ).toDomain()
        }
    }

    override suspend fun eliminar(id: String): Result<Unit> {
        return safeApiCall { api.eliminar(id) }
    }

    private fun DescuentoResponse.toDomain(): Descuento = Descuento(
        id = id,
        codigo = codigo,
        porcentaje = porcentaje,
        fechaVencimiento = fechaVencimiento,
        activo = activo,
        localComida = localComida,
        localComidaId = localComidaId
    )
}
