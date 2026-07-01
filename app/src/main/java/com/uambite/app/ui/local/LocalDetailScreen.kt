package com.uambite.app.ui.local

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uambite.app.data.api.ImageUrlBuilder
import com.uambite.app.domain.model.IngredienteExtra
import com.uambite.app.domain.model.Producto
import com.uambite.app.ui.cart.CartViewModel
import com.uambite.app.ui.common.EmptyBox
import com.uambite.app.ui.common.ErrorBox
import com.uambite.app.ui.common.ModalAddResult
import com.uambite.app.ui.common.NetworkImage
import com.uambite.app.ui.common.ProductoModalSheet
import com.uambite.app.ui.common.StockBadge
import com.uambite.app.ui.theme.Blue100
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Gray500
import com.uambite.app.ui.theme.Purple500
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun LocalDetailScreen(
    localId: String,
    onBack: () -> Unit,
    onProductoAgregado: () -> Unit,
    viewModel: LocalDetailViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LocalTopBar(
                state = state,
                onBack = onBack
            )

            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f)) {
                when (val s = state) {
                    is LocalDetailViewModel.UiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is LocalDetailViewModel.UiState.Error -> {
                        ErrorBox(message = s.message, onRetry = { viewModel.load() })
                    }
                    is LocalDetailViewModel.UiState.Success -> {
                        LocalProductosContent(
                            productos = s.productos,
                            onProductoClick = { p -> productoSeleccionado = p }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    val modalProducto = productoSeleccionado
    if (modalProducto != null && state is LocalDetailViewModel.UiState.Success) {
        val successState = state as LocalDetailViewModel.UiState.Success
        val extras: List<IngredienteExtra> = viewModel.extrasParaProducto(modalProducto.id)
        ProductoModalSheet(
            producto = modalProducto,
            extrasDisponibles = extras,
            onDismiss = { productoSeleccionado = null },
            onConfirm = { result: ModalAddResult ->
                val currentState = state
                if (currentState is LocalDetailViewModel.UiState.Success) {
                    val local = currentState.local
                    cartViewModel.agregar(
                        productoId = modalProducto.id,
                        nombre = modalProducto.nombre,
                        precioBase = modalProducto.precio,
                        cantidad = result.cantidad,
                        localNombre = local.nombre,
                        localComidaId = local.id,
                        extrasIds = result.extrasIds,
                        extrasPrecios = result.extrasPrecios,
                        extrasNombres = result.extrasNombres
                    )
                    scope.launch {
                        snackbarHostState.showSnackbar("${modalProducto.nombre} agregado al carrito")
                    }
                    productoSeleccionado = null
                    onProductoAgregado()
                }
            }
        )
    }
}

@Composable
private fun LocalTopBar(
    state: LocalDetailViewModel.UiState,
    onBack: () -> Unit
) {
    val titulo = (state as? LocalDetailViewModel.UiState.Success)?.local?.nombre ?: "Local"
    val info = (state as? LocalDetailViewModel.UiState.Success)?.let { s ->
        "${s.local.ubicacion} · ${s.local.horario ?: "Sin horario"}"
    } ?: ""
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (info.isNotEmpty()) {
                Text(
                    text = info,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LocalProductosContent(
    productos: List<Producto>,
    onProductoClick: (Producto) -> Unit
) {
    if (productos.isEmpty()) {
        EmptyBox(message = "Este local aún no tiene productos")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp, vertical = 12.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(productos, key = { it.id }) { producto ->
            ProductoRow(
                producto = producto,
                onClick = { if (producto.stock > 0) onProductoClick(producto) }
            )
        }
    }
}

@Composable
private fun ProductoRow(
    producto: Producto,
    onClick: () -> Unit
) {
    val agotado = producto.stock == 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = !agotado, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (producto.tieneImagen) {
            NetworkImage(
                url = ImageUrlBuilder.producto(producto.id),
                contentDescription = producto.nombre,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Blue100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = Blue600.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (producto.permitePersonalizacion) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Personalizable",
                        tint = Purple500,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            if (!producto.descripcion.isNullOrBlank()) {
                Text(
                    text = producto.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.size(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatPrecio(producto.precio),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Blue600,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(8.dp))
                StockBadge(stock = producto.stock)
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        Button(
            onClick = onClick,
            enabled = !agotado,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.height(36.dp),
            contentPadding = ButtonDefaults.ContentPadding
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = "Agregar",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

private fun formatPrecio(value: Double): String =
    "$${String.format(Locale.US, "%.2f", value)}"
