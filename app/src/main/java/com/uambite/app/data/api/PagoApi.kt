package com.uambite.app.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PagoApi {

    @GET("pago/all")
    suspend fun getAll(
        @Query("size") size: Int = 1000
    ): PageDto<PagoResponse>

    @GET("pago/{id}")
    suspend fun getById(@Path("id") id: String): PagoResponse

    @POST("pago/save")
    suspend fun save(@Body request: PagoRequest): PagoResponse
}
