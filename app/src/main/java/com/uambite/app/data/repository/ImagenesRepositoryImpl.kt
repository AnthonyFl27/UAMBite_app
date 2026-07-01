package com.uambite.app.data.repository

import com.uambite.app.data.api.ImagenesApi
import com.uambite.app.data.api.safeApiCall
import com.uambite.app.domain.repository.ImagenesRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImagenesRepositoryImpl @Inject constructor(
    private val api: ImagenesApi
) : ImagenesRepository {

    override suspend fun subir(tipo: String, id: String, file: File): Result<String> {
        return safeApiCall {
            val mediaType = detectarMediaType(file).toMediaTypeOrNull()
            val part = MultipartBody.Part.createFormData(
                name = "file",
                filename = file.name,
                body = file.asRequestBody(mediaType)
            )
            val response = when (tipo) {
                "local" -> api.uploadLocal(id, part)
                "producto" -> api.uploadProducto(id, part)
                "ingrediente" -> api.uploadIngrediente(id, part)
                else -> throw IllegalArgumentException("Tipo de imagen no soportado: $tipo")
            }
            if (!response.isSuccessful) {
                throw retrofit2.HttpException(response)
            }
            "ok"
        }
    }

    override suspend fun eliminar(tipo: String, id: String): Result<Unit> {
        return safeApiCall {
            val response = when (tipo) {
                "local" -> api.deleteLocal(id)
                "producto" -> api.deleteProducto(id)
                "ingrediente" -> api.deleteIngrediente(id)
                else -> throw IllegalArgumentException("Tipo de imagen no soportado: $tipo")
            }
            if (!response.isSuccessful) {
                throw retrofit2.HttpException(response)
            }
        }
    }

    private fun detectarMediaType(file: File): String {
        val ext = file.extension.lowercase()
        return when (ext) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }
    }
}
