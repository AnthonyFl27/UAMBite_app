package com.uambite.app.domain.repository

import com.uambite.app.domain.model.Pago

interface PagosRepository {
    suspend fun save(pedidoId: String, metodoPago: String): Result<Pago>
}
