package com.uambite.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uambite.app.ui.admin.tabs.AdminDescuentosTab
import com.uambite.app.ui.admin.tabs.AdminFranjasTab
import com.uambite.app.ui.admin.tabs.AdminIngredientesTab
import com.uambite.app.ui.admin.tabs.AdminLocalesTab
import com.uambite.app.ui.admin.tabs.AdminPedidosTab
import com.uambite.app.ui.admin.tabs.AdminProductosTab
import com.uambite.app.ui.admin.tabs.AdminUsuariosTab
import com.uambite.app.ui.imagenes.ImagenUploadViewModel
import com.uambite.app.ui.theme.Blue100
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Blue700

@Composable
fun AdminScreen(
    tab: String,
    onBack: () -> Unit,
    onTabChange: (String) -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
    imagenVm: ImagenUploadViewModel = hiltViewModel()
) {
    val snackbar by viewModel.snackbar.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val uploadState by imagenVm.state.collectAsState()

    LaunchedEffect(tab) { viewModel.loadTab(tab) }

    LaunchedEffect(snackbar) {
        snackbar?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.resetSnackbar()
        }
    }

    LaunchedEffect(uploadState.snackbar) {
        uploadState.snackbar?.let {
            snackbarHostState.showSnackbar(it)
            imagenVm.consumirSnackbar()
        }
    }

    LaunchedEffect(uploadState.lastUploadTimestamp) {
        if (uploadState.lastUploadTimestamp > 0L) {
            viewModel.loadTab(tab)
            imagenVm.consumirActualizacion()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AdminTopBar(onBack = onBack)
            AdminTabs(
                currentTab = tab,
                onTabChange = onTabChange
            )
            Box(modifier = Modifier.weight(1f)) {
                when (tab) {
                    "pedidos" -> AdminPedidosTab(viewModel)
                    "usuarios" -> AdminUsuariosTab(viewModel)
                    "locales" -> AdminLocalesTab(viewModel, imagenVm)
                    "productos" -> AdminProductosTab(viewModel, imagenVm)
                    "franjas" -> AdminFranjasTab(viewModel)
                    "descuentos" -> AdminDescuentosTab(viewModel)
                    "ingredientes" -> AdminIngredientesTab(viewModel, imagenVm)
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun AdminTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AdminPanelSettings,
            contentDescription = null,
            tint = Blue600
        )
        Text(
            text = "Panel Admin",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private data class AdminTabItem(val id: String, val titulo: String)

private val adminTabs = listOf(
    AdminTabItem("pedidos", "Pedidos"),
    AdminTabItem("usuarios", "Usuarios"),
    AdminTabItem("locales", "Locales"),
    AdminTabItem("productos", "Productos"),
    AdminTabItem("franjas", "Franjas"),
    AdminTabItem("descuentos", "Descuentos"),
    AdminTabItem("ingredientes", "Ingredientes")
)

@Composable
private fun AdminTabs(
    currentTab: String,
    onTabChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        adminTabs.forEach { tab ->
            val selected = tab.id == currentTab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) Blue600 else Blue100)
                    .clickable { onTabChange(tab.id) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = tab.titulo,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else Blue700,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
