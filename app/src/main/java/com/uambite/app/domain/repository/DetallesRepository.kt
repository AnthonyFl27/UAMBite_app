package com.uambite.app.domain.repository

import com.uambite.app.domain.model.DetallePedido

interface DetallesRepository {
    suspend fun save(
        pedidoId: String,
        productoId: String,
        cantidad: Int,
        ingredientesExtraIds: List<String>
    ): Result<DetallePedido>
}
