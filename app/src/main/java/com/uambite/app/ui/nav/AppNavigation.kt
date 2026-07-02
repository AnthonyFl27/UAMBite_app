package com.uambite.app.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.uambite.app.data.auth.SessionManager
import com.uambite.app.data.realtime.WebSocketManager
import com.uambite.app.domain.repository.AuthRepository
import com.uambite.app.ui.auth.CambiarPasswordScreen
import com.uambite.app.ui.auth.LoginScreen
import com.uambite.app.ui.auth.RegisterScreen
import com.uambite.app.ui.cart.CartScreen
import com.uambite.app.ui.cart.CartViewModel
import com.uambite.app.ui.common.BottomNavBar
import com.uambite.app.ui.common.OrderStatusBannerHost
import com.uambite.app.ui.home.HomeScreen
import com.uambite.app.ui.local.LocalDetailScreen
import com.uambite.app.ui.orders.OrdersScreen
import com.uambite.app.ui.pedidodetail.PedidoDetailScreen
import com.uambite.app.ui.profile.ProfileScreen
import com.uambite.app.ui.admin.AdminScreen
import com.uambite.app.ui.localadmin.LocalAdminScreen
import com.uambite.app.ui.theme.ThemeViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : androidx.lifecycle.ViewModel() {
    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WebSocketEntryPoint {
    fun webSocketManager(): WebSocketManager
}

@Composable
fun AppNavigation(
    viewModel: StartViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    sessionViewModel: SessionViewModel = hiltViewModel(),
    logoutViewModel: LogoutViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val startDestination by viewModel.startDestination.collectAsState()
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDarkMode by themeViewModel.isDarkTheme.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val webSocketManager: WebSocketManager = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            WebSocketEntryPoint::class.java
        ).webSocketManager()
    }

    if (startDestination.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: startDestination
    val user by sessionViewModel.user.collectAsState()
    val cartItems by cartViewModel.items.collectAsState()
    val cartCount = cartItems.sumOf { it.cantidad }

    val showBottomBar = currentRoute == "home" ||
            currentRoute.startsWith("local/") ||
            currentRoute.startsWith("localadmin") ||
            currentRoute.startsWith("admin") ||
            currentRoute == "cart" ||
            currentRoute == "orders" ||
            currentRoute == "profile"

    val showAdmin = user?.rol == "ADMIN"
    val showLocal = user?.rol == "LOCAL"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    cartCount = cartCount,
                    showAdmin = showAdmin,
                    showLocal = showLocal,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("home")
                            launchSingleTop = true
                        }
                    },
                    onLogout = {
                        logoutViewModel.logout {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        val sessionManager: SessionManager = hiltViewModel()
        val sessionSnackbarHostState = remember { SnackbarHostState() }
        SessionUnauthorizedHandler(
            navController = navController,
            snackbarHostState = sessionSnackbarHostState
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { requiereCambioPassword ->
                            if (requiereCambioPassword) {
                                navController.navigate("cambiar-password") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate("register")
                        }
                    )
                }

                composable("register") {
                    RegisterScreen(
                        onRegisterSuccess = {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable("cambiar-password") {
                    CambiarPasswordScreen(
                        onPasswordChanged = {
                            navController.navigate("home") {
                                popUpTo("cambiar-password") { inclusive = true }
                            }
                        }
                    )
                }

                composable("home") {
                    HomeScreen(
                        onNavigateToLocal = { localId ->
                            navController.navigate("local/$localId")
                        },
                        onToggleDarkMode = { themeViewModel.toggleDarkMode() },
                        isDarkMode = isDarkMode ?: systemDark
                    )
                }

                composable("cart") {
                    CartScreen(
                        onBack = { navController.popBackStack() },
                        onPedidoCreado = {
                            navController.navigate("orders") {
                                popUpTo("home")
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("orders") {
                    OrdersScreen(
                        onBack = { navController.popBackStack() },
                        onPedidoClick = { pedidoId ->
                            navController.navigate("pedido/$pedidoId")
                        }
                    )
                }

                composable(
                    route = "pedido/{pedidoId}",
                    arguments = listOf(navArgument("pedidoId") { type = NavType.StringType })
                ) {
                    PedidoDetailScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("profile") {
                    ProfileScreen(
                        onBack = { navController.popBackStack() },
                        onLoggedOut = {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = "local/{localId}",
                    arguments = listOf(navArgument("localId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val localId = backStackEntry.arguments?.getString("localId") ?: ""
                    LocalDetailScreen(
                        localId = localId,
                        onBack = { navController.popBackStack() },
                        onProductoAgregado = { /* sin acción por ahora */ }
                    )
                }

                composable(
                    route = "admin/{tab}",
                    arguments = listOf(navArgument("tab") { type = NavType.StringType })
                ) { backStackEntry ->
                    val tab = backStackEntry.arguments?.getString("tab") ?: "pedidos"
                    AdminScreen(
                        tab = tab,
                        onBack = { navController.popBackStack() },
                        onTabChange = { nuevaTab ->
                            navController.navigate("admin/$nuevaTab") {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(
                    route = "localadmin/{tab}",
                    arguments = listOf(navArgument("tab") { type = NavType.StringType })
                ) { backStackEntry ->
                    val tab = backStackEntry.arguments?.getString("tab") ?: "pedidos"
                    LocalAdminScreen(
                        tab = tab,
                        onBack = { navController.popBackStack() },
                        onTabChange = { nuevaTab ->
                            navController.navigate("localadmin/$nuevaTab") {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
            SnackbarHost(
                hostState = sessionSnackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            if (currentRoute != "login" && currentRoute != "register" &&
                currentRoute != "cambiar-password"
            ) {
                OrderStatusBannerHost(
                    webSocketManager = webSocketManager,
                    onVerPedido = { pedidoId ->
                        navController.navigate("pedido/$pedidoId")
                    }
                )
            }
        }
    }
}
