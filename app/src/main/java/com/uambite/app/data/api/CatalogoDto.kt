package com.uambite.app.data.api

import kotlinx.serialization.Serializable

@Serializable
data class LocalComidaResponse(
    val id: String,
    val nombre: String,
    val ubicacion: String,
    val horario: String? = null,
    val duenoId: String? = null,
    val tieneImagen: Boolean = false
)

@Serializable
data class ProductoResponse(
    val id: String,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val stock: Int,
    val permitePersonalizacion: Boolean,
    val localComida: String,
    val localComidaId: String? = null,
    val ingredientesExtraIds: List<String> = emptyList(),
    val tieneImagen: Boolean = false
)

@Serializable
data class IngredienteExtraResponse(
    val id: String,
    val nombre: String,
    val precioExtra: Double,
    val tieneImagen: Boolean = false
)

@Serializable
data class ProductoIngredienteExtraResponse(
    val id: String,
    val productoId: String,
    val producto: String? = null,
    val ingredienteExtraId: String,
    val ingredienteExtra: String? = null
)

@Serializable
data class LocalComidaRequest(
    val nombre: String,
    val ubicacion: String,
    val horario: String? = null
)

@Serializable
data class EncargadoCreateRequest(
    val carnet: String,
    val nombre: String,
    val apellido: String,
    val correo: String? = null,
    val password: String
)

@Serializable
data class LocalComidaConEncargadoRequest(
    val nombre: String,
    val ubicacion: String,
    val horario: String? = null,
    val encargado: EncargadoCreateRequest
)

@Serializable
data class LocalComidaConEncargadoResponse(
    val local: LocalComidaResponse,
    val encargado: UsuarioResponse
)

@Serializable
data class ProductoRequest(
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val stock: Int,
    val permitePersonalizacion: Boolean,
    val localComidaId: String
)

@Serializable
data class IngredienteExtraRequest(
    val nombre: String,
    val precioExtra: Double
)

@Serializable
data class ProductoIngredienteExtraRequest(
    val productoId: String,
    val ingredienteExtraId: String
)
