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
        val msg = e.message ?: "Error inesperado"
        // Si el mensaje parece JSON o código técnico, lo limpiamos
        val cleanMsg = if (msg.contains("{") || msg.contains("[") || msg.contains("kotlinx")) {
            "Error al procesar la respuesta del servidor"
        } else {
            msg
        }
        Result.failure(Exception(cleanMsg))
    }
}

fun extractErrorMessage(e: HttpException): String {
    val code = e.code()
    if (code == 403) return "Acceso denegado: No tienes permisos para esta acción"
    if (code == 401) return "Sesión expirada: Por favor inicia sesión de nuevo"
    
    return try {
        val errorBody = e.response()?.errorBody()?.string()
        if (!errorBody.isNullOrBlank()) {
            val json = Json { ignoreUnknownKeys = true }
            val error = json.decodeFromString<ErrorResponse>(errorBody)
            error.message ?: "Error $code"
        } else {
            "Error $code"
        }
    } catch (_: Exception) {
        "Error $code"
    }
}

@kotlinx.serialization.Serializable
private data class ErrorResponse(
    val code: String? = null,
    val message: String? = null,
    val status: Int? = null
)
