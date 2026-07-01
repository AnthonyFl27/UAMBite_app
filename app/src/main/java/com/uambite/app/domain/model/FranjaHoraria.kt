package com.uambite.app.domain.model

data class FranjaHoraria(
    val id: String,
    val horaInicio: String,
    val horaFin: String,
    val capacidadMaxima: Int,
    val pedidosActuales: Int,
    val disponible: Boolean,
    val localComidaId: String?,
    val localComida: String?
) {
    val descripcionCompleta: String
        get() = "$horaInicio - $horaFin ($pedidosActuales/$capacidadMaxima)" +
                (localComida?.let { " — $it" } ?: "")
}
