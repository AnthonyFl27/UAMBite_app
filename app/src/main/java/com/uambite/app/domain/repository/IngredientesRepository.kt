package com.uambite.app.domain.repository

import com.uambite.app.domain.model.IngredienteExtra

interface IngredientesRepository {
    suspend fun getIngredientes(): Result<List<IngredienteExtra>>
    suspend fun getById(id: String): Result<IngredienteExtra>
    suspend fun crear(nombre: String, precioExtra: Double): Result<IngredienteExtra>
    suspend fun actualizar(id: String, nombre: String, precioExtra: Double): Result<IngredienteExtra>
    suspend fun eliminar(id: String): Result<Unit>
}
