package com.uambite.app.domain.repository

import com.uambite.app.domain.model.Descuento

interface DescuentosRepository {
    suspend fun getAll(): Result<List<Descuento>>
    suspend fun buscarPorCodigo(codigo: String): Result<Descuento?>
    suspend fun crear(
        codigo: String,
        porcentaje: Double,
        fechaVencimiento: String?,
        activo: Boolean,
        localComidaId: String?
    ): Result<Descuento>
    suspend fun actualizar(
        id: String,
        codigo: String,
        porcentaje: Double,
        fechaVencimiento: String?,
        activo: Boolean,
        localComidaId: String?
    ): Result<Descuento>
    suspend fun eliminar(id: String): Result<Unit>
}
