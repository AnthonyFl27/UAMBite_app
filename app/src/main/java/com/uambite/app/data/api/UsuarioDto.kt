package com.uambite.app.data.api

import kotlinx.serialization.Serializable

@Serializable
data class UsuarioRequest(
    val carnet: String,
    val nombre: String,
    val apellido: String,
    val correo: String? = null,
    val rol: String,
    val password: String? = null
)

@Serializable
data class UsuarioResponse(
    val id: String,
    val carnet: String,
    val nombre: String,
    val apellido: String,
    val correo: String? = null,
    val rol: String,
    val requiereCambioPassword: Boolean = false
)
