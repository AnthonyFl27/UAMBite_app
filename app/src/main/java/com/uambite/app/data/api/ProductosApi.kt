package com.uambite.app.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductosApi {

    @GET("producto/all")
    suspend fun getAll(
        @Query("size") size: Int = 1000
    ): PageDto<ProductoResponse>

    @GET("producto/{id}")
    suspend fun getById(@Path("id") id: String): ProductoResponse

    @POST("producto/save")
    suspend fun save(@Body request: ProductoRequest): ProductoResponse

    @PUT("producto/update/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body request: ProductoRequest
    ): ProductoResponse

    @DELETE("producto/delete/{id}")
    suspend fun eliminar(@Path("id") id: String)
}
