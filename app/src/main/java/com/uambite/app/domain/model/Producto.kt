package com.uambite.app.domain.model

data class Producto(
    val id: String,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val stock: Int,
    val permitePersonalizacion: Boolean,
    val localComida: String,
    val localComidaId: String?,
    val ingredientesExtraIds: List<String>,
    val tieneImagen: Boolean
)
