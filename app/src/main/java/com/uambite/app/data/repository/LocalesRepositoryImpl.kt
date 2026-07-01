package com.uambite.app.data.repository

import com.uambite.app.data.api.EncargadoCreateRequest
import com.uambite.app.data.api.LocalesApi
import com.uambite.app.data.api.LocalComidaConEncargadoRequest
import com.uambite.app.data.api.LocalComidaRequest
import com.uambite.app.data.api.LocalComidaResponse
import com.uambite.app.data.api.safeApiCall
import com.uambite.app.domain.model.Local
import com.uambite.app.domain.repository.LocalesRepository
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.decodeFromJsonElement
// ...
@Singleton
class LocalesRepositoryImpl @Inject constructor(
    private val api: LocalesApi
) : LocalesRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun getLocales(): Result<List<Local>> {
        return safeApiCall {
            val element = api.getAll()
            val list = json.decodeFromJsonElement<List<LocalComidaResponse>>(
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

    override suspend fun getById(id: String): Result<Local> {
        return safeApiCall { api.getById(id).toDomain() }
    }

    override suspend fun crear(
        nombre: String,
        ubicacion: String,
        horario: String?,
        encargadoCarnet: String,
        encargadoNombre: String,
        encargadoApellido: String,
        encargadoCorreo: String?,
        encargadoPassword: String
    ): Result<Local> {
        return safeApiCall {
            val response = api.save(
                LocalComidaConEncargadoRequest(
                    nombre = nombre,
                    ubicacion = ubicacion,
                    horario = horario,
                    encargado = EncargadoCreateRequest(
                        carnet = encargadoCarnet,
                        nombre = encargadoNombre,
                        apellido = encargadoApellido,
                        correo = encargadoCorreo,
                        password = encargadoPassword
                    )
                )
            )
            response.local.toDomain()
        }
    }

    override suspend fun actualizar(
        id: String,
        nombre: String,
        ubicacion: String,
        horario: String?
    ): Result<Local> {
        return safeApiCall {
            api.update(
                id = id,
                request = LocalComidaRequest(
                    nombre = nombre,
                    ubicacion = ubicacion,
                    horario = horario
                )
            ).toDomain()
        }
    }

    override suspend fun asignarEncargado(
        localId: String,
        carnet: String,
        nombre: String,
        apellido: String,
        correo: String?,
        password: String
    ): Result<Local> {
        return safeApiCall {
            api.asignarEncargado(
                id = localId,
                request = EncargadoCreateRequest(
                    carnet = carnet,
                    nombre = nombre,
                    apellido = apellido,
                    correo = correo,
                    password = password
                )
            ).toDomain()
        }
    }

    override suspend fun eliminar(id: String): Result<Unit> {
        return safeApiCall { api.eliminar(id) }
    }

    private fun LocalComidaResponse.toDomain(): Local = Local(
        id = id,
        nombre = nombre,
        ubicacion = ubicacion,
        horario = horario,
        duenoId = duenoId,
        tieneImagen = tieneImagen
    )
}
