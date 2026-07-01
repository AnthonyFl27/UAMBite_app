package com.uambite.app.domain.repository

import java.io.File

interface ImagenesRepository {
    suspend fun subir(tipo: String, id: String, file: File): Result<String>
    suspend fun eliminar(tipo: String, id: String): Result<Unit>
}
