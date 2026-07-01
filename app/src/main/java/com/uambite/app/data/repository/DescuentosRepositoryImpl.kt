package com.uambite.app.data.repository

import com.uambite.app.data.api.DescuentoRequest
import com.uambite.app.data.api.DescuentoResponse
import com.uambite.app.data.api.DescuentosApi
import com.uambite.app.data.api.safeApiCall
import com.uambite.app.domain.model.Descuento
import com.uambite.app.domain.repository.DescuentosRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DescuentosRepositoryImpl @Inject constructor(
    private val api: DescuentosApi
) : DescuentosRepository {

    override suspend fun getAll(): Result<List<Descuento>> {
        return safeApiCall { api.getAll().content.map { it.toDomain() } }
    }

    override suspend fun buscarPorCodigo(codigo: String): Result<Descuento?> {
        return safeApiCall {
            api.getAll().content.find { it.codigo.equals(codigo, ignoreCase = true) }?.toDomain()
        }
    }

    override suspend fun crear(
        codigo: String,
        porcentaje: Double,
        fechaVencimiento: String?,
        activo: Boolean,
        localComidaId: String?
    ): Result<Descuento> {
        return safeApiCall {
            api.save(
                DescuentoRequest(
                    codigo = codigo,
                    porcentaje = porcentaje,
                    fechaVencimiento = fechaVencimiento,
                    activo = activo,
                    localComidaId = localComidaId
                )
            ).toDomain()
        }
    }

    override suspend fun actualizar(
        id: String,
        codigo: String,
        porcentaje: Double,
        fechaVencimiento: String?,
        activo: Boolean,
        localComidaId: String?
    ): Result<Descuento> {
        return safeApiCall {
            api.update(
                id = id,
                request = DescuentoRequest(
                    codigo = codigo,
                    porcentaje = porcentaje,
                    fechaVencimiento = fechaVencimiento,
                    activo = activo,
                    localComidaId = localComidaId
                )
            ).toDomain()
        }
    }

    override suspend fun eliminar(id: String): Result<Unit> {
        return safeApiCall { api.eliminar(id) }
    }

    private fun DescuentoResponse.toDomain(): Descuento = Descuento(
        id = id,
        codigo = codigo,
        porcentaje = porcentaje,
        fechaVencimiento = fechaVencimiento,
        activo = activo,
        localComida = localComida,
        localComidaId = localComidaId
    )
}
