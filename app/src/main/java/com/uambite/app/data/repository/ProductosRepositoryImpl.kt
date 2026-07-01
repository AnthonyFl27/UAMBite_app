package com.uambite.app.data.repository

import com.uambite.app.data.api.ProductoRequest
import com.uambite.app.data.api.ProductoResponse
import com.uambite.app.data.api.ProductosApi
import com.uambite.app.data.api.safeApiCall
import com.uambite.app.domain.model.Producto
import com.uambite.app.domain.repository.ProductosRepository
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.decodeFromJsonElement
// ...
@Singleton
class ProductosRepositoryImpl @Inject constructor(
    private val api: ProductosApi
) : ProductosRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun getProductos(): Result<List<Producto>> {
        return safeApiCall {
            val element = api.getAll()
            val list = json.decodeFromJsonElement<List<ProductoResponse>>(
                extractContentIfPage(element)
            )
            list.map { it.toDomain() }
        }
    }

    private fun extractContentIfPage(element: JsonElement): JsonElement {
        return try {
            val obj = element.jsonObject
            obj["content"] ?: element
        } catch (_: Exception) {
            element
        }
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
