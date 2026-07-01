package com.uambite.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uambite.app.domain.model.IngredienteExtra
import com.uambite.app.domain.model.Producto
import com.uambite.app.ui.cart.CartViewModel
import com.uambite.app.ui.common.EmptyBox
import com.uambite.app.ui.common.ErrorBox
import com.uambite.app.ui.common.LoadingBox
import com.uambite.app.ui.common.ModalAddResult
import com.uambite.app.ui.common.ProductoModalSheet
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLocal: (String) -> Unit,
    onToggleDarkMode: () -> Unit,
    isDarkMode: Boolean,
    viewModel: HomeViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
        val isRefreshing = uiState is HomeViewModel.HomeUiState.Loading &&
                (uiState as? HomeViewModel.HomeUiState.Success) != null
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadHome() },
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = uiState) {
                is HomeViewModel.HomeUiState.Loading -> {
                    LoadingBox()
                }
                is HomeViewModel.HomeUiState.Error -> {
                    ErrorBox(
                        message = state.message,
                        onRetry = { viewModel.loadHome() }
                    )
                }
                is HomeViewModel.HomeUiState.Success -> {
                    HomeContent(
                        state = state,
                        onRefresh = { viewModel.loadHome() },
                        onToggleDarkMode = onToggleDarkMode,
                        isDarkMode = isDarkMode,
                        onNavigateToLocal = onNavigateToLocal,
                        onProductoClick = { p ->
                            if (p.stock > 0) productoSeleccionado = p
                        }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    val modalProducto = productoSeleccionado
    if (modalProducto != null && uiState is HomeViewModel.HomeUiState.Success) {
        val success = uiState as HomeViewModel.HomeUiState.Success
        val extrasIds = success.asociaciones
            .filter { it.productoId == modalProducto.id }
            .map { it.ingredienteExtraId }
            .toSet()
        val extrasDisponibles: List<IngredienteExtra> =
            success.ingredientes.filter { it.id in extrasIds }

        ProductoModalSheet(
            producto = modalProducto,
            extrasDisponibles = extrasDisponibles,
            onDismiss = { productoSeleccionado = null },
            onConfirm = { result: ModalAddResult ->
                val local = success.locales.find { it.id == modalProducto.localComidaId }
                    ?: success.locales.find { it.nombre == modalProducto.localComida }
                cartViewModel.agregar(
                    productoId = modalProducto.id,
                    nombre = modalProducto.nombre,
                    precioBase = modalProducto.precio,
                    cantidad = result.cantidad,
                    localNombre = modalProducto.localComida,
                    localComidaId = modalProducto.localComidaId ?: local?.id,
                    extrasIds = result.extrasIds,
                    extrasPrecios = result.extrasPrecios,
                    extrasNombres = result.extrasNombres
                )
                scope.launch {
                    snackbarHostState.showSnackbar("${modalProducto.nombre} agregado al carrito")
                }
                productoSeleccionado = null
            }
        )
    }
}

@Composable
private fun HomeContent(
    state: HomeViewModel.HomeUiState.Success,
    onRefresh: () -> Unit,
    onToggleDarkMode: () -> Unit,
    isDarkMode: Boolean,
    onNavigateToLocal: (String) -> Unit,
    onProductoClick: (Producto) -> Unit
) {
    val locales = state.locales
    val productos = state.productos
    val productosConStock = productos.filter { it.stock > 0 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Locales",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Row {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onToggleDarkMode) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Cambiar tema",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        if (locales.isEmpty()) {
            item {
                EmptyBox(message = "No hay locales disponibles")
            }
        } else {
            items(locales.size) { index ->
                val local = locales[index]
                val productosDelLocal = productos.filter {
                    it.localComidaId == local.id || it.localComida == local.nombre
                }
                LocalCard(
                    local = local,
                    productos = productosDelLocal,
                    onClick = { onNavigateToLocal(local.id) },
                    onProductoClick = onProductoClick
                )
            }
        }

        if (productosConStock.isNotEmpty()) {
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Productos Destacados",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    DestacadosGrid(
                        productos = productosConStock.take(8),
                        onProductoClick = onProductoClick
                    )
                }
            }
        }
    }
}

@Composable
private fun DestacadosGrid(
    productos: List<Producto>,
    onProductoClick: (Producto) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        var index = 0
        while (index < productos.size) {
            val isFullWidth = index % 3 == 2
            if (isFullWidth) {
                ProductoCard(
                    producto = productos[index],
                    onClick = { onProductoClick(productos[index]) },
                    onAgregar = { onProductoClick(productos[index]) },
                    fullWidth = true
                )
                index++
            } else {
                val endIndex = minOf(index + 2, productos.size)
                val rowItems = productos.subList(index, endIndex)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { producto ->
                        ProductoCard(
                            producto = producto,
                            onClick = { onProductoClick(producto) },
                            onAgregar = { onProductoClick(producto) },
                            modifier = Modifier.weight(1f),
                            fullWidth = false
                        )
                    }
                    if (rowItems.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                index = endIndex
            }
        }
    }
}
