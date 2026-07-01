package com.uambite.app.data.repository

import com.uambite.app.data.api.UsuarioRequest
import com.uambite.app.data.api.UsuarioResponse
import com.uambite.app.data.api.UsuariosApi
import com.uambite.app.data.api.safeApiCall
import com.uambite.app.domain.repository.UsuariosRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsuariosRepositoryImpl @Inject constructor(
    private val api: UsuariosApi
) : UsuariosRepository {

    override suspend fun getAll(): Result<List<UsuarioResponse>> {
        return safeApiCall { api.getAll().content }
    }

    override suspend fun getById(id: String): Result<UsuarioResponse> {
        return safeApiCall { api.getById(id) }
    }

    override suspend fun save(
        carnet: String,
        nombre: String,
        apellido: String,
        correo: String?,
        rol: String,
        password: String?
    ): Result<UsuarioResponse> {
        return safeApiCall {
            api.save(
                UsuarioRequest(
                    carnet = carnet,
                    nombre = nombre,
                    apellido = apellido,
                    correo = correo,
                    rol = rol,
                    password = password
                )
            )
        }
    }

    override suspend fun update(
        id: String,
        carnet: String,
        rol: String,
        nombre: String,
        apellido: String,
        correo: String?,
        password: String?
    ): Result<UsuarioResponse> {
        return safeApiCall {
            api.update(
                id = id,
                request = UsuarioRequest(
                    carnet = carnet,
                    nombre = nombre,
                    apellido = apellido,
                    correo = correo,
                    rol = rol,
                    password = password
                )
            )
        }
    }
}
