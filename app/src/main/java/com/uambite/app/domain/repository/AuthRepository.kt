package com.uambite.app.domain.repository

import com.uambite.app.data.api.AuthResponse

interface AuthRepository {
    suspend fun login(carnet: String, password: String): Result<AuthResponse>
    suspend fun register(
        carnet: String,
        nombre: String,
        apellido: String,
        correo: String,
        password: String,
        rol: String
    ): Result<AuthResponse>
    suspend fun cambiarPassword(passwordActual: String, passwordNuevo: String): Result<Unit>
    suspend fun logout()
}
