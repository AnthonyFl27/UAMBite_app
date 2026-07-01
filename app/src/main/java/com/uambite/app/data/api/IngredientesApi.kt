package com.uambite.app.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface IngredientesApi {

    @GET("ingredienteextra/all")
    suspend fun getAll(
        @Query("size") size: Int = 1000
    ): kotlinx.serialization.json.JsonElement

    @GET("ingredienteextra/{id}")
    suspend fun getById(@Path("id") id: String): IngredienteExtraResponse

    @POST("ingredienteextra/save")
    suspend fun save(@Body request: IngredienteExtraRequest): IngredienteExtraResponse

    @PUT("ingredienteextra/update/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body request: IngredienteExtraRequest
    ): IngredienteExtraResponse

    @DELETE("ingredienteextra/delete/{id}")
    suspend fun eliminar(@Path("id") id: String)
}
