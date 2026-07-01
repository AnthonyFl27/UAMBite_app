package com.uambite.app.domain.repository

import com.uambite.app.domain.model.Pedido

interface PedidosRepository {
    suspend fun getMisPedidos(): Result<List<Pedido>>
    suspend fun getAllPedidos(): Result<List<Pedido>>
    suspend fun getById(id: String): Result<Pedido>
    suspend fun save(
        tipoEntrega: String,
        usuarioId: String,
        franjaHorariaId: String?,
        descuentoId: String?
    ): Result<Pedido>
    suspend fun cambiarEstado(id: String, estado: String): Result<Pedido>
    suspend fun setPrioridad(id: String, prioridad: Int): Result<Pedido>
    suspend fun eliminar(id: String): Result<Unit>
}
