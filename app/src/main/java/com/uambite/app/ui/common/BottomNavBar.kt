package com.uambite.app.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavBar(
    currentRoute: String,
    cartCount: Int,
    showAdmin: Boolean,
    showLocal: Boolean,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Inicio") },
            selected = currentRoute == "home",
            onClick = { onNavigate("home") }
        )
        NavigationBarItem(
            icon = {
                if (cartCount > 0) {
                    BadgedBox(badge = { Text(cartCount.toString()) }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    }
                } else {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                }
            },
            label = { Text("Carrito") },
            selected = currentRoute == "cart",
            onClick = { onNavigate("cart") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) },
            label = { Text("Pedidos") },
            selected = currentRoute == "orders",
            onClick = { onNavigate("orders") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Perfil") },
            selected = currentRoute == "profile",
            onClick = { onNavigate("profile") }
        )
        if (showLocal) {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Store, contentDescription = null) },
                label = { Text("Mi Local") },
                selected = currentRoute.startsWith("localadmin"),
                onClick = { onNavigate("localadmin/pedidos") }
            )
        }
        if (showAdmin) {
            NavigationBarItem(
                icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                label = { Text("Admin") },
                selected = currentRoute.startsWith("admin"),
                onClick = { onNavigate("admin/pedidos") }
            )
        }
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Salir",
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            },
            label = { Text("Salir") },
            selected = false,
            onClick = onLogout
        )
    }
}
