package com.uambite.app.ui.home

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
class HomeViewModel @Inject constructor(
    private val localesRepository: LocalesRepository,
    private val productosRepository: ProductosRepository,
    private val ingredientesRepository: IngredientesRepository,
    private val productoIngredienteRepository: ProductoIngredienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            val localesResult = localesRepository.getLocales()
            val productosResult = productosRepository.getProductos()
            val ingredientesResult = ingredientesRepository.getIngredientes()
            val asociacionesResult = productoIngredienteRepository.getAsociaciones()

            val locales = localesResult.getOrNull()
            val productos = productosResult.getOrNull()
            val ingredientes = ingredientesResult.getOrNull()
            val asociaciones = asociacionesResult.getOrNull()

            val error = localesResult.exceptionOrNull()
                ?: productosResult.exceptionOrNull()
                ?: ingredientesResult.exceptionOrNull()
                ?: asociacionesResult.exceptionOrNull()

            if (error != null) {
                _uiState.value = HomeUiState.Error(error.message ?: "Error al cargar")
                return@launch
            }

            _uiState.value = HomeUiState.Success(
                locales = locales ?: emptyList(),
                productos = productos ?: emptyList(),
                ingredientes = ingredientes ?: emptyList(),
                asociaciones = asociaciones ?: emptyList()
            )
        }
    }

    sealed class HomeUiState {
        data object Loading : HomeUiState()
        data class Error(val message: String) : HomeUiState()
        data class Success(
            val locales: List<Local>,
            val productos: List<Producto>,
            val ingredientes: List<IngredienteExtra>,
            val asociaciones: List<ProductoIngrediente>
        ) : HomeUiState()
    }
}
