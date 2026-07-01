package com.uambite.app.domain.model

data class Local(
    val id: String,
    val nombre: String,
    val ubicacion: String,
    val horario: String?,
    val duenoId: String?,
    val tieneImagen: Boolean
)
