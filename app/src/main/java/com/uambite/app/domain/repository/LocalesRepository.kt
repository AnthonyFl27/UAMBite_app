package com.uambite.app.domain.repository

import com.uambite.app.domain.model.Local

interface LocalesRepository {
    suspend fun getLocales(): Result<List<Local>>
    suspend fun getById(id: String): Result<Local>
    suspend fun crear(
        nombre: String,
        ubicacion: String,
        horario: String?,
        encargadoCarnet: String,
        encargadoNombre: String,
        encargadoApellido: String,
        encargadoCorreo: String?,
        encargadoPassword: String
    ): Result<Local>
    suspend fun actualizar(
        id: String,
        nombre: String,
        ubicacion: String,
        horario: String?
    ): Result<Local>
    suspend fun asignarEncargado(
        localId: String,
        carnet: String,
        nombre: String,
        apellido: String,
        correo: String?,
        password: String
    ): Result<Local>
    suspend fun eliminar(id: String): Result<Unit>
}
