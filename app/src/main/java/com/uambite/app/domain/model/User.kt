package com.uambite.app.domain.model

data class User(
    val id: String,
    val carnet: String,
    val nombre: String,
    val apellido: String,
    val correo: String?,
    val rol: String,
    val requiereCambioPassword: Boolean
)
