package com.uambite.app.domain.repository

import com.uambite.app.data.api.UsuarioResponse

interface UsuariosRepository {
    suspend fun getAll(): Result<List<UsuarioResponse>>
    suspend fun getById(id: String): Result<UsuarioResponse>
    suspend fun save(
        carnet: String,
        nombre: String,
        apellido: String,
        correo: String?,
        rol: String,
        password: String?
    ): Result<UsuarioResponse>
    suspend fun update(
        id: String,
        carnet: String,
        rol: String,
        nombre: String,
        apellido: String,
        correo: String?,
        password: String?
    ): Result<UsuarioResponse>
}
