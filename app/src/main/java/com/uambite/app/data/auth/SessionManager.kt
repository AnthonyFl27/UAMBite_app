package com.uambite.app.data.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uambite.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionManager @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _unauthorizedEvent = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val unauthorizedEvent: SharedFlow<Unit> = _unauthorizedEvent.asSharedFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    init {
        viewModelScope.launch {
            AuthInterceptor.unauthorizedEvents.collect {
                Log.w("SessionManager", "401 detectado, limpiando sesión")
                _mensaje.value = "Tu sesión expiró. Vuelve a iniciar sesión."
                authRepository.logout()
                _unauthorizedEvent.tryEmit(Unit)
            }
        }
    }

    fun consumirMensaje() { _mensaje.value = null }
}
