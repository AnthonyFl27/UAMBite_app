package com.uambite.app.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DetallesApi {

    @GET("detallepedido/all")
    suspend fun getAll(
        @Query("size") size: Int = 1000
    ): kotlinx.serialization.json.JsonElement

    @POST("detallepedido/save")
    suspend fun save(@Body request: DetallePedidoRequest): DetallePedidoResponse

    @DELETE("detallepedido/delete/{id}")
    suspend fun eliminar(@Path("id") id: String)
}
