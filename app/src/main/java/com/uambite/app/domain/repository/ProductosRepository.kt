package com.uambite.app.domain.repository

import com.uambite.app.domain.model.Producto

interface ProductosRepository {
    suspend fun getProductos(): Result<List<Producto>>
    suspend fun getById(id: String): Result<Producto>
    suspend fun crear(
        nombre: String,
        descripcion: String?,
        precio: Double,
        stock: Int,
        permitePersonalizacion: Boolean,
        localComidaId: String
    ): Result<Producto>
    suspend fun actualizar(
        id: String,
        nombre: String,
        descripcion: String?,
        precio: Double,
        stock: Int,
        permitePersonalizacion: Boolean,
        localComidaId: String
    ): Result<Producto>
    suspend fun eliminar(id: String): Result<Unit>
}
