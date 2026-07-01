package com.uambite.app.ui.franjas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uambite.app.domain.model.Descuento
import com.uambite.app.domain.model.FranjaHoraria
import com.uambite.app.domain.repository.CartRepository
import com.uambite.app.domain.repository.DescuentosRepository
import com.uambite.app.domain.repository.FranjasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FranjasViewModel @Inject constructor(
    private val franjasRepository: FranjasRepository,
    private val descuentosRepository: DescuentosRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = franjasRepository.getDisponibles()
            _uiState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Error al cargar franjas") }
            )
        }
    }

    fun buscarYAplicarDescuento(
        codigo: String,
        onAplicado: (Descuento) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = descuentosRepository.buscarPorCodigo(codigo)
            result.fold(
                onSuccess = { d ->
                    if (d == null) {
                        onError("Código no encontrado")
                        return@fold
                    }
                    if (!d.activo) {
                        onError("Descuento inactivo")
                        return@fold
                    }
                    d.fechaVencimiento?.let { fecha ->
                        if (esVencido(fecha)) {
                            onError("Descuento vencido")
                            return@fold
                        }
                    }
                    cartRepository.aplicarDescuento(d)
                    onAplicado(d)
                },
                onFailure = { onError(it.message ?: "Error al aplicar descuento") }
            )
        }
    }

    private fun esVencido(fecha: String): Boolean {
        return try {
            val parts = fecha.split("-")
            if (parts.size != 3) return false
            val y = parts[0].toInt()
            val m = parts[1].toInt()
            val d = parts[2].toInt()
            val cal = java.util.Calendar.getInstance()
            cal.clear()
            cal.set(y, m - 1, d, 23, 59, 59)
            cal.timeInMillis < System.currentTimeMillis()
        } catch (_: Exception) {
            false
        }
    }

    sealed class UiState {
        data object Loading : UiState()
        data class Success(val franjas: List<FranjaHoraria>) : UiState()
        data class Error(val message: String) : UiState()
    }
}
