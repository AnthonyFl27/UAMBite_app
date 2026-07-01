package com.uambite.app.data.api

import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FranjasApi {

    @GET("franja/all")
    suspend fun getAll(
        @Query("size") size: Int = 1000
    ): JsonElement

    @GET("franja/disponibles")
    suspend fun getDisponibles(): JsonElement

    @GET("franja/{id}")
    suspend fun getById(@Path("id") id: String): FranjaHorariaResponse

    @POST("franja/save")
    suspend fun save(@Body request: FranjaHorariaRequest): FranjaHorariaResponse

    @PUT("franja/update/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body request: FranjaHorariaRequest
    ): FranjaHorariaResponse

    @DELETE("franja/delete/{id}")
    suspend fun eliminar(@Path("id") id: String)
}
