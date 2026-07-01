package com.uambite.app.data.repository

import com.uambite.app.data.api.ProductoIngredienteApi
import com.uambite.app.data.api.ProductoIngredienteExtraRequest
import com.uambite.app.data.api.ProductoIngredienteExtraResponse
import com.uambite.app.data.api.safeApiCall
import com.uambite.app.domain.model.ProductoIngrediente
import com.uambite.app.domain.repository.ProductoIngredienteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductoIngredienteRepositoryImpl @Inject constructor(
    private val api: ProductoIngredienteApi
) : ProductoIngredienteRepository {

    override suspend fun getAsociaciones(): Result<List<ProductoIngrediente>> {
        return safeApiCall { api.getAll().content.map { it.toDomain() } }
    }

    override suspend fun vincular(
        productoId: String,
        ingredienteExtraId: String
    ): Result<ProductoIngrediente> {
        return safeApiCall {
            api.save(
                ProductoIngredienteExtraRequest(
                    productoId = productoId,
                    ingredienteExtraId = ingredienteExtraId
                )
            ).toDomain()
        }
    }

    override suspend fun eliminar(id: String): Result<Unit> {
        return safeApiCall { api.eliminar(id) }
    }

    private fun ProductoIngredienteExtraResponse.toDomain(): ProductoIngrediente = ProductoIngrediente(
        id = id,
        productoId = productoId,
        producto = producto,
        ingredienteExtraId = ingredienteExtraId,
        ingredienteExtra = ingredienteExtra
    )
}
