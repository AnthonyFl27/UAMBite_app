package com.uambite.app.domain.repository

import com.uambite.app.domain.model.ProductoIngrediente

interface ProductoIngredienteRepository {
    suspend fun getAsociaciones(): Result<List<ProductoIngrediente>>
    suspend fun vincular(productoId: String, ingredienteExtraId: String): Result<ProductoIngrediente>
    suspend fun eliminar(id: String): Result<Unit>
}
