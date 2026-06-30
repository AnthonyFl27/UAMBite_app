package com.uambite.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uambite.app.data.api.AuthResponse
import com.uambite.app.data.auth.TokenStore
import com.uambite.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState

    private val _registerState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val registerState: StateFlow<RegisterUiState> = _registerState

    private val _cambiarPasswordState = MutableStateFlow<CambiarPasswordUiState>(CambiarPasswordUiState.Idle)
    val cambiarPasswordState: StateFlow<CambiarPasswordUiState> = _cambiarPasswordState

    fun login(carnet: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            val result = authRepository.login(carnet, password)
            _loginState.value = result.fold(
                onSuccess = { LoginUiState.Success(it) },
                onFailure = { LoginUiState.Error(it.message ?: "Error desconocido") }
            )
        }
    }

    fun register(
        carnet: String,
        nombre: String,
        apellido: String,
        correo: String,
        password: String,
        rol: String
    ) {
        viewModelScope.launch {
            _registerState.value = RegisterUiState.Loading
            val result = authRepository.register(carnet, nombre, apellido, correo, password, rol)
            _registerState.value = result.fold(
                onSuccess = { RegisterUiState.Success(it) },
                onFailure = { RegisterUiState.Error(it.message ?: "Error desconocido") }
            )
        }
    }

    fun cambiarPassword(passwordActual: String, passwordNuevo: String) {
        viewModelScope.launch {
            _cambiarPasswordState.value = CambiarPasswordUiState.Loading
            val result = authRepository.cambiarPassword(passwordActual, passwordNuevo)
            _cambiarPasswordState.value = result.fold(
                onSuccess = {
                    tokenStore.markPasswordChanged()
                    CambiarPasswordUiState.Success
                },
                onFailure = { CambiarPasswordUiState.Error(it.message ?: "Error desconocido") }
            )
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginUiState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = RegisterUiState.Idle
    }

    fun resetCambiarPasswordState() {
        _cambiarPasswordState.value = CambiarPasswordUiState.Idle
    }

    sealed class LoginUiState {
        data object Idle : LoginUiState()
        data object Loading : LoginUiState()
        data class Success(val authResponse: AuthResponse) : LoginUiState()
        data class Error(val message: String) : LoginUiState()
    }

    sealed class RegisterUiState {
        data object Idle : RegisterUiState()
        data object Loading : RegisterUiState()
        data class Success(val authResponse: AuthResponse) : RegisterUiState()
        data class Error(val message: String) : RegisterUiState()
    }

    sealed class CambiarPasswordUiState {
        data object Idle : CambiarPasswordUiState()
        data object Loading : CambiarPasswordUiState()
        data object Success : CambiarPasswordUiState()
        data class Error(val message: String) : CambiarPasswordUiState()
    }
}
