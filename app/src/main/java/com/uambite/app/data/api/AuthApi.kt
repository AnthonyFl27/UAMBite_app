package com.uambite.app.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/cambiar-password")
    suspend fun cambiarPassword(@Body request: CambiarPasswordRequest): Response<Unit>
}
