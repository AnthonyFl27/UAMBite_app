package com.uambite.app.data.repository

import com.uambite.app.data.api.AuthApi
import com.uambite.app.data.api.AuthResponse
import com.uambite.app.data.api.CambiarPasswordRequest
import com.uambite.app.data.api.LoginRequest
import com.uambite.app.data.api.RegisterRequest
import com.uambite.app.data.auth.TokenStore
import com.uambite.app.domain.repository.AuthRepository
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore
) : AuthRepository {

    override suspend fun login(carnet: String, password: String): Result<AuthResponse> {
        return safeApiCall {
            authApi.login(LoginRequest(carnet, password)).also {
                tokenStore.saveSession(it)
            }
        }
    }

    override suspend fun register(
        carnet: String,
        nombre: String,
        apellido: String,
        correo: String,
        password: String,
        rol: String
    ): Result<AuthResponse> {
        return safeApiCall {
            authApi.register(
                RegisterRequest(
                    carnet = carnet,
                    nombre = nombre,
                    apellido = apellido,
                    correo = correo,
                    password = password,
                    rol = rol
                )
            ).also {
                tokenStore.saveSession(it)
            }
        }
    }

    override suspend fun cambiarPassword(passwordActual: String, passwordNuevo: String): Result<Unit> {
        return safeApiCall {
            val response = authApi.cambiarPassword(
                CambiarPasswordRequest(passwordActual, passwordNuevo)
            )
            if (response.isSuccessful) {
                Unit
            } else {
                throw HttpException(response)
            }
        }
    }

    private inline fun <T> safeApiCall(call: () -> T): Result<T> {
        return try {
            Result.success(call())
        } catch (e: IOException) {
            Result.failure(Exception("Error de red. Verifica tu conexión."))
        } catch (e: HttpException) {
            Result.failure(Exception(extractErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Error inesperado"))
        }
    }

    private fun extractErrorMessage(e: HttpException): String {
        return try {
            val errorBody = e.response()?.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                val json = Json { ignoreUnknownKeys = true }
                val error = json.decodeFromString<ErrorResponse>(errorBody)
                error.message ?: "Error ${e.code()}"
            } else {
                "Error ${e.code()}"
            }
        } catch (_: Exception) {
            "Error ${e.code()}"
        }
    }

    @kotlinx.serialization.Serializable
    private data class ErrorResponse(
        val code: String? = null,
        val message: String? = null,
        val status: Int? = null
    )
}
