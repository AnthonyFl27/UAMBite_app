package com.uambite.app.ui.local

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uambite.app.domain.model.IngredienteExtra
import com.uambite.app.domain.model.Local
import com.uambite.app.domain.model.Producto
import com.uambite.app.domain.model.ProductoIngrediente
import com.uambite.app.domain.repository.IngredientesRepository
import com.uambite.app.domain.repository.LocalesRepository
import com.uambite.app.domain.repository.ProductoIngredienteRepository
import com.uambite.app.domain.repository.ProductosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val localesRepository: LocalesRepository,
    private val productosRepository: ProductosRepository,
    private val ingredientesRepository: IngredientesRepository,
    private val productoIngredienteRepository: ProductoIngredienteRepository
) : ViewModel() {

    val localId: String = savedStateHandle.get<String>("localId") ?: ""

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val localesResult = localesRepository.getLocales()
            val productosResult = productosRepository.getProductos()
            val ingredientesResult = ingredientesRepository.getIngredientes()
            val asociacionesResult = productoIngredienteRepository.getAsociaciones()

            val error = localesResult.exceptionOrNull()
                ?: productosResult.exceptionOrNull()
                ?: ingredientesResult.exceptionOrNull()
                ?: asociacionesResult.exceptionOrNull()

            if (error != null) {
                _uiState.value = UiState.Error(error.message ?: "Error al cargar")
                return@launch
            }

            val local = localesResult.getOrNull()?.find { it.id == localId }
            if (local == null) {
                _uiState.value = UiState.Error("Local no encontrado")
                return@launch
            }

            val todosProductos = productosResult.getOrNull().orEmpty()
            val productosLocal = todosProductos.filter { it.localComidaId == localId || it.localComida == local.nombre }
            val ingredientes = ingredientesResult.getOrNull().orEmpty()
            val asociaciones = asociacionesResult.getOrNull().orEmpty()

            _uiState.value = UiState.Success(
                local = local,
                productos = productosLocal,
                ingredientes = ingredientes,
                asociaciones = asociaciones
            )
        }
    }

    fun extrasParaProducto(productoId: String): List<IngredienteExtra> {
        val state = _uiState.value
        if (state !is UiState.Success) return emptyList()
        val idsExtras = state.asociaciones
            .filter { it.productoId == productoId }
            .map { it.ingredienteExtraId }
            .toSet()
        return state.ingredientes.filter { it.id in idsExtras }
    }

    sealed class UiState {
        data object Loading : UiState()
        data class Error(val message: String) : UiState()
        data class Success(
            val local: Local,
            val productos: List<Producto>,
            val ingredientes: List<IngredienteExtra>,
            val asociaciones: List<ProductoIngrediente>
        ) : UiState()
    }
}
