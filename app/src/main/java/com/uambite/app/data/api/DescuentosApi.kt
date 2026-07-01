package com.uambite.app.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DescuentosApi {

    @GET("descuento/all")
    suspend fun getAll(
        @Query("size") size: Int = 1000
    ): kotlinx.serialization.json.JsonElement

    @GET("descuento/{id}")
    suspend fun getById(@Path("id") id: String): DescuentoResponse

    @POST("descuento/save")
    suspend fun save(@Body request: DescuentoRequest): DescuentoResponse

    @PUT("descuento/update/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body request: DescuentoRequest
    ): DescuentoResponse

    @DELETE("descuento/delete/{id}")
    suspend fun eliminar(@Path("id") id: String)
}
