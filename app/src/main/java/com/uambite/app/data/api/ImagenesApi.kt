package com.uambite.app.data.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ImagenesApi {

    @Multipart
    @POST("localcomida/{id}/imagen")
    suspend fun uploadLocal(
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>

    @Multipart
    @POST("producto/{id}/imagen")
    suspend fun uploadProducto(
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>

    @Multipart
    @POST("ingredienteextra/{id}/imagen")
    suspend fun uploadIngrediente(
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>

    @DELETE("localcomida/{id}/imagen")
    suspend fun deleteLocal(@Path("id") id: String): Response<ResponseBody>

    @DELETE("producto/{id}/imagen")
    suspend fun deleteProducto(@Path("id") id: String): Response<ResponseBody>

    @DELETE("ingredienteextra/{id}/imagen")
    suspend fun deleteIngrediente(@Path("id") id: String): Response<ResponseBody>
}
