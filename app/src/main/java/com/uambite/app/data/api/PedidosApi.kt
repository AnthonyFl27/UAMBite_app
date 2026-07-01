package com.uambite.app.data.api

import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PedidosApi {

    @GET("pedido/mios")
    suspend fun getMisPedidos(): JsonElement

    @GET("pedido/all")
    suspend fun getAll(
        @Query("size") size: Int = 1000
    ): JsonElement

    @GET("pedido/{id}")
    suspend fun getById(@Path("id") id: String): PedidoResponse

    @POST("pedido/save")
    suspend fun save(@Body request: PedidoRequest): PedidoResponse

    @PUT("pedido/cancelar/{id}")
    suspend fun cancelar(@Path("id") id: String): PedidoResponse

    @PUT("pedido/confirmar/{id}")
    suspend fun confirmar(@Path("id") id: String): PedidoResponse

    @PUT("pedido/preparar/{id}")
    suspend fun preparar(@Path("id") id: String): PedidoResponse

    @PUT("pedido/listo/{id}")
    suspend fun listo(@Path("id") id: String): PedidoResponse

    @PUT("pedido/entregar/{id}")
    suspend fun entregar(@Path("id") id: String): PedidoResponse

    @PUT("pedido/{id}/prioridad")
    suspend fun setPrioridad(
        @Path("id") id: String,
        @Body request: PrioridadRequest
    ): PedidoResponse

    @DELETE("pedido/delete/{id}")
    suspend fun eliminar(@Path("id") id: String)
}
