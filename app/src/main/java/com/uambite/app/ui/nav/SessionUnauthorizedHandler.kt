package com.uambite.app.ui.nav

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.uambite.app.data.auth.SessionManager

@Composable
fun SessionUnauthorizedHandler(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    sessionManager: SessionManager = hiltViewModel()
) {
    val mensaje by sessionManager.mensaje.collectAsState()
    val event by sessionManager.unauthorizedEvent.collectAsState(initial = null)

    LaunchedEffect(event) {
        if (event == Unit) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            sessionManager.consumirMensaje()
        }
    }
}
