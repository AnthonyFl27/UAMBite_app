package com.uambite.app.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface LocalesApi {

    @GET("localcomida/all")
    suspend fun getAll(
        @Query("size") size: Int = 1000
    ): kotlinx.serialization.json.JsonElement

    @GET("localcomida/{id}")
    suspend fun getById(@Path("id") id: String): LocalComidaResponse

    @POST("localcomida/save")
    suspend fun save(
        @Body request: LocalComidaConEncargadoRequest
    ): LocalComidaConEncargadoResponse

    @PUT("localcomida/update/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body request: LocalComidaRequest
    ): LocalComidaResponse

    @PUT("localcomida/{id}/asignar-encargado")
    suspend fun asignarEncargado(
        @Path("id") id: String,
        @Body request: EncargadoCreateRequest
    ): LocalComidaResponse

    @DELETE("localcomida/delete/{id}")
    suspend fun eliminar(@Path("id") id: String)
}
