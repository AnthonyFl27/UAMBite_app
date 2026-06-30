package com.uambite.app.data.api

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val carnet: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val carnet: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val password: String,
    val rol: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val id: String,
    val carnet: String,
    val nombre: String,
    val apellido: String,
    val correo: String? = null,
    val rol: String,
    val requiereCambioPassword: Boolean = false
)

@Serializable
data class CambiarPasswordRequest(
    val passwordActual: String,
    val passwordNuevo: String
)
