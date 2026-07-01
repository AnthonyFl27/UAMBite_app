package com.uambite.app.domain.repository

import com.uambite.app.domain.model.Entrega

interface EntregasRepository {
    suspend fun crear(pedidoId: String, ubicacion: String): Result<Entrega>
    suspend fun finalizar(entregaId: String): Result<Entrega>
}
