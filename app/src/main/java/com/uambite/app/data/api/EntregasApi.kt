package com.uambite.app.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface EntregasApi {

    @GET("entrega/all")
    suspend fun getAll(
        @Query("size") size: Int = 1000
    ): PageDto<EntregaResponse>

    @POST("entrega/save")
    suspend fun save(@Body request: EntregaRequest): EntregaResponse

    @PUT("entrega/finalizar/{id}")
    suspend fun finalizar(@Path("id") id: String): EntregaResponse
}
