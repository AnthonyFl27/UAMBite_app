package com.uambite.app.domain.model

data class ProductoIngrediente(
    val id: String,
    val productoId: String,
    val producto: String?,
    val ingredienteExtraId: String,
    val ingredienteExtra: String?
)
