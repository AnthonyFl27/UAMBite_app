package com.uambite.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uambite.app.data.api.UsuarioResponse
import com.uambite.app.domain.repository.AuthRepository
import com.uambite.app.domain.repository.UsuariosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val usuariosRepository: UsuariosRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private val _logoutState = MutableStateFlow<Boolean>(false)
    val logoutState: StateFlow<Boolean> = _logoutState

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    fun load(userId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = usuariosRepository.getById(userId)
            _uiState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Error al cargar perfil") }
            )
        }
    }

    fun guardar(
        id: String,
        carnet: String,
        rol: String,
        nombre: String,
        apellido: String,
        correo: String?,
        nuevaPassword: String?
    ) {
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            val result = usuariosRepository.update(
                id = id,
                carnet = carnet,
                rol = rol,
                nombre = nombre,
                apellido = apellido,
                correo = correo,
                password = nuevaPassword
            )
            _saveState.value = result.fold(
                onSuccess = { SaveState.Success("Perfil actualizado") },
                onFailure = { SaveState.Error(it.message ?: "Error al guardar") }
            )
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _logoutState.value = true
        }
    }

    sealed class UiState {
        data object Loading : UiState()
        data class Success(val usuario: UsuarioResponse) : UiState()
        data class Error(val message: String) : UiState()
    }

    sealed class SaveState {
        data object Idle : SaveState()
        data object Loading : SaveState()
        data class Success(val message: String) : SaveState()
        data class Error(val message: String) : SaveState()
    }
}
