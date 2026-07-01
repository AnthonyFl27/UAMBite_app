package com.uambite.app.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductoIngredienteApi {

    @GET("productoingredienteextra/all")
    suspend fun getAll(
        @Query("size") size: Int = 1000
    ): PageDto<ProductoIngredienteExtraResponse>

    @POST("productoingredienteextra/save")
    suspend fun save(@Body request: ProductoIngredienteExtraRequest): ProductoIngredienteExtraResponse

    @DELETE("productoingredienteextra/delete/{id}")
    suspend fun eliminar(@Path("id") id: String)
}
