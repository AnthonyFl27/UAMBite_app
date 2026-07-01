package com.uambite.app.ui.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uambite.app.data.auth.TokenStore
import com.uambite.app.domain.model.User
import com.uambite.app.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    tokenStore: TokenStore,
    cartRepository: CartRepository
) : ViewModel() {

    val user: StateFlow<User?> = tokenStore.user
        .onEach { u -> cartRepository.setUserId(u?.id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
