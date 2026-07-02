package com.uambite.app.ui.localadmin

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
import androidx.compose.material.icons.filled.Store
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
import com.uambite.app.ui.imagenes.ImagenUploadViewModel
import com.uambite.app.ui.localadmin.tabs.LocalAdminDescuentosTab
import com.uambite.app.ui.localadmin.tabs.LocalAdminFranjasTab
import com.uambite.app.ui.localadmin.tabs.LocalAdminIngredientesTab
import com.uambite.app.ui.localadmin.tabs.LocalAdminPedidosTab
import com.uambite.app.ui.localadmin.tabs.LocalAdminProductosTab
import com.uambite.app.ui.localadmin.tabs.LocalAdminTab
import com.uambite.app.ui.nav.SessionViewModel
import com.uambite.app.ui.theme.Blue100
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Blue700

@Composable
fun LocalAdminScreen(
    tab: String,
    onBack: () -> Unit,
    onTabChange: (String) -> Unit,
    viewModel: LocalAdminViewModel = hiltViewModel(),
    sessionViewModel: SessionViewModel = hiltViewModel(),
    imagenVm: ImagenUploadViewModel = hiltViewModel()
) {
    val snackbar by viewModel.snackbar.collectAsState()
    val snackbarLocal by viewModel.snackbarLocal.collectAsState()
    val uploadState by imagenVm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val user by sessionViewModel.user.collectAsState()

    LaunchedEffect(tab, user?.id) {
        viewModel.loadTab(tab, user?.id)
    }

    LaunchedEffect(snackbar) {
        snackbar?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.resetSnackbar()
        }
    }

    LaunchedEffect(snackbarLocal) {
        snackbarLocal?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumirSnackbarLocal()
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
            viewModel.loadTab(tab, user?.id)
            imagenVm.consumirActualizacion()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(onBack = onBack)
            Tabs(currentTab = tab, onTabChange = onTabChange)
            Box(modifier = Modifier.weight(1f)) {
                when (tab) {
                    "pedidos" -> LocalAdminPedidosTab(viewModel)
                    "local" -> LocalAdminTab(viewModel, imagenVm)
                    "productos" -> LocalAdminProductosTab(viewModel, imagenVm)
                    "franjas" -> LocalAdminFranjasTab(viewModel)
                    "descuentos" -> LocalAdminDescuentosTab(viewModel)
                    "ingredientes" -> LocalAdminIngredientesTab(viewModel, imagenVm)
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
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Store,
            contentDescription = null,
            tint = Blue600
        )
        Text(
            text = "Mi Local",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private data class LocalAdminTabInfo(val id: String, val titulo: String)

private val localAdminTabs = listOf(
    LocalAdminTabInfo("pedidos", "Pedidos"),
    LocalAdminTabInfo("local", "Local"),
    LocalAdminTabInfo("productos", "Productos"),
    LocalAdminTabInfo("franjas", "Franjas"),
    LocalAdminTabInfo("descuentos", "Descuentos"),
    LocalAdminTabInfo("ingredientes", "Ingredientes")
)

@Composable
private fun Tabs(currentTab: String, onTabChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        localAdminTabs.forEach { tab ->
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
