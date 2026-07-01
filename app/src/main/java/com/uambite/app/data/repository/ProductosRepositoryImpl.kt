package com.uambite.app.data.repository

import com.uambite.app.data.api.ProductoRequest
import com.uambite.app.data.api.ProductoResponse
import com.uambite.app.data.api.ProductosApi
import com.uambite.app.data.api.safeApiCall
import com.uambite.app.domain.model.Producto
import com.uambite.app.domain.repository.ProductosRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductosRepositoryImpl @Inject constructor(
    private val api: ProductosApi
) : ProductosRepository {

    override suspend fun getProductos(): Result<List<Producto>> {
        return safeApiCall { api.getAll().content.map { it.toDomain() } }
    }

    override suspend fun getById(id: String): Result<Producto> {
        return safeApiCall { api.getById(id).toDomain() }
    }

    override suspend fun crear(
        nombre: String,
        descripcion: String?,
        precio: Double,
        stock: Int,
        permitePersonalizacion: Boolean,
        localComidaId: String
    ): Result<Producto> {
        return safeApiCall {
            api.save(
                ProductoRequest(
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio,
                    stock = stock,
                    permitePersonalizacion = permitePersonalizacion,
                    localComidaId = localComidaId
                )
            ).toDomain()
        }
    }

    override suspend fun actualizar(
        id: String,
        nombre: String,
        descripcion: String?,
        precio: Double,
        stock: Int,
        permitePersonalizacion: Boolean,
        localComidaId: String
    ): Result<Producto> {
        return safeApiCall {
            api.update(
                id = id,
                request = ProductoRequest(
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio,
                    stock = stock,
                    permitePersonalizacion = permitePersonalizacion,
                    localComidaId = localComidaId
                )
            ).toDomain()
        }
    }

    override suspend fun eliminar(id: String): Result<Unit> {
        return safeApiCall { api.eliminar(id) }
    }

    private fun ProductoResponse.toDomain(): Producto = Producto(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        precio = precio,
        stock = stock,
        permitePersonalizacion = permitePersonalizacion,
        localComida = localComida,
        localComidaId = localComidaId,
        ingredientesExtraIds = ingredientesExtraIds,
        tieneImagen = tieneImagen
    )
}
