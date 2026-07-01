package com.uambite.app.ui.imagenes

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object UploadHelper {

    const val MAX_SIZE_BYTES = 5L * 1024L * 1024L
    val ALLOWED_MIME = setOf("image/jpeg", "image/png", "image/webp")
    val ALLOWED_EXT = setOf("jpg", "jpeg", "png", "webp")

    data class ValidatedFile(
        val file: File,
        val mimeType: String,
        val originalName: String
    )

    fun uriToFile(context: Context, uri: Uri): File {
        val name = nameOrFallback(context, uri)
        val cacheDir = File(context.cacheDir, "uploads").apply { mkdirs() }
        val ext = extensionFromMimeOrName(context, uri, name)
        val timestamp = System.currentTimeMillis()
        val safeName = if (name.contains('.')) name else "upload_$timestamp.$ext"
        val out = File(cacheDir, "${timestamp}_$safeName")
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "No se pudo abrir el archivo seleccionado" }
            FileOutputStream(out).use { output -> input.copyTo(output) }
        }
        return out
    }

    fun validate(file: File): Result<ValidatedFile> {
        if (!file.exists() || file.length() == 0L) {
            return Result.failure(IllegalArgumentException("El archivo está vacío"))
        }
        if (file.length() > MAX_SIZE_BYTES) {
            return Result.failure(IllegalArgumentException("La imagen supera 5MB (${formatSize(file.length())})"))
        }
        val ext = file.extension.lowercase()
        val mime = when (ext) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: ""
        }
        if (mime !in ALLOWED_MIME) {
            return Result.failure(IllegalArgumentException("Tipo no permitido (jpg/png/webp)"))
        }
        return Result.success(
            ValidatedFile(
                file = file,
                mimeType = mime,
                originalName = file.name
            )
        )
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun nameOrFallback(context: Context, uri: Uri): String {
        val name = queryDisplayName(context, uri)
        if (!name.isNullOrBlank()) return name
        val timestamp = System.currentTimeMillis()
        val ext = extensionFromMimeOrName(context, uri, "")
        return "upload_$timestamp.$ext"
    }

    private fun extensionFromMimeOrName(context: Context, uri: Uri, name: String): String {
        val fromName = name.substringAfterLast('.', "").lowercase()
        if (fromName in ALLOWED_EXT) return fromName
        val mime = context.contentResolver.getType(uri) ?: return "jpg"
        return when (mime) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
    }

    private fun formatSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1) String.format(Locale.US, "%.2f MB", mb)
        else String.format(Locale.US, "%.0f KB", kb)
    }
}
