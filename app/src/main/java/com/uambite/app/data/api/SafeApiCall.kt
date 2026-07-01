package com.uambite.app.data.api

import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

inline fun <T> safeApiCall(call: () -> T): Result<T> {
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

fun extractErrorMessage(e: HttpException): String {
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
