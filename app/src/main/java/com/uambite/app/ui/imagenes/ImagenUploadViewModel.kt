package com.uambite.app.ui.imagenes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uambite.app.domain.repository.ImagenesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ImagenUploadViewModel @Inject constructor(
    private val imagenesRepository: ImagenesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UploadState())
    val state: StateFlow<UploadState> = _state.asStateFlow()

    fun subir(tipo: String, id: String, file: File) {
        val validation = UploadHelper.validate(file)
        if (validation.isFailure) {
            _state.value = _state.value.copy(subiendo = false, snackbar = validation.exceptionOrNull()?.message)
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(subiendo = true, snackbar = null)
            val result = imagenesRepository.subir(tipo, id, file)
            result.onSuccess {
                _state.value = _state.value.copy(subiendo = false, snackbar = "Imagen subida", lastUploadedTipo = tipo, lastUploadedId = id, lastUploadTimestamp = System.currentTimeMillis())
                try { file.delete() } catch (_: Exception) {}
            }.onFailure { err ->
                _state.value = _state.value.copy(subiendo = false, snackbar = err.message ?: "Error al subir")
            }
        }
    }

    fun eliminar(tipo: String, id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(subiendo = true, snackbar = null)
            val result = imagenesRepository.eliminar(tipo, id)
            result.onSuccess {
                _state.value = _state.value.copy(subiendo = false, snackbar = "Imagen eliminada", lastUploadedTipo = tipo, lastUploadedId = id, lastUploadTimestamp = System.currentTimeMillis())
            }.onFailure { err ->
                _state.value = _state.value.copy(subiendo = false, snackbar = err.message ?: "Error al eliminar")
            }
        }
    }

    fun consumirSnackbar() {
        _state.value = _state.value.copy(snackbar = null)
    }

    fun consumirActualizacion() {
        _state.value = _state.value.copy(lastUploadedTipo = null, lastUploadedId = null, lastUploadTimestamp = 0L)
    }
}

data class UploadState(
    val subiendo: Boolean = false,
    val snackbar: String? = null,
    val lastUploadedTipo: String? = null,
    val lastUploadedId: String? = null,
    val lastUploadTimestamp: Long = 0L
)
