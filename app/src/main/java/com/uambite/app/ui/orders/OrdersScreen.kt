package com.uambite.app.ui.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uambite.app.domain.model.DetallePedido
import com.uambite.app.domain.model.Pedido
import com.uambite.app.ui.common.EmptyBox
import com.uambite.app.ui.common.ErrorBox
import com.uambite.app.ui.common.LoadingBox
import com.uambite.app.ui.common.MiniOrderTimeline
import com.uambite.app.ui.theme.Blue100
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Gray500
import com.uambite.app.ui.theme.Green100
import com.uambite.app.ui.theme.Green600
import com.uambite.app.ui.theme.Green700
import com.uambite.app.ui.theme.Green800
import com.uambite.app.ui.theme.Orange100
import com.uambite.app.ui.theme.Orange700
import com.uambite.app.ui.theme.Purple100
import com.uambite.app.ui.theme.Purple700
import com.uambite.app.ui.theme.Red100
import com.uambite.app.ui.theme.Red500
import com.uambite.app.ui.theme.Red700
import com.uambite.app.ui.theme.Yellow100
import com.uambite.app.ui.theme.Yellow800

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onBack: () -> Unit,
    onPedidoClick: (String) -> Unit = {},
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val confirmadosLocalmente by viewModel.confirmadosLocalmente.collectAsState()
    val pedidosResaltados by viewModel.pedidosResaltados.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var pedidoAEliminar by remember { mutableStateOf<Pedido?>(null) }
    var pedidoACancelar by remember { mutableStateOf<Pedido?>(null) }
    var pedidoAConfirmarRetiro by remember { mutableStateOf<Pedido?>(null) }

    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is OrdersViewModel.ActionState.Success -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetAction()
            }
            is OrdersViewModel.ActionState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetAction()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OrdersTopBar(onBack = onBack)

            val isRefreshing = uiState is OrdersViewModel.UiState.Loading &&
                    (uiState as? OrdersViewModel.UiState.Success) != null
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.load() },
                modifier = Modifier.weight(1f)
            ) {
                when (val state = uiState) {
                    is OrdersViewModel.UiState.Loading -> {
                        LoadingBox()
                    }
                    is OrdersViewModel.UiState.Error -> {
                        ErrorBox(message = state.message, onRetry = { viewModel.load() })
                    }
                    is OrdersViewModel.UiState.Success -> {
                        if (state.pedidos.isEmpty()) {
                            EmptyBox(message = "No tienes pedidos aún")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.pedidos, key = { it.id }) { pedido ->
                                    PedidoCard(
                                        pedido = pedido,
                                        procesando = actionState is OrdersViewModel.ActionState.Loading,
                                        confirmadoLocalmente = pedido.id in confirmadosLocalmente,
                                        resaltado = pedido.id in pedidosResaltados,
                                        onClick = {
                                            viewModel.consumirResaltado(pedido.id)
                                            onPedidoClick(pedido.id)
                                        },
                                        onCancelar = { pedidoACancelar = pedido },
                                        onConfirmarRetiro = { pedidoAConfirmarRetiro = pedido },
                                        onEliminar = { pedidoAEliminar = pedido }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    pedidoAEliminar?.let { pedido ->
        ConfirmDialog(
            titulo = "¿Eliminar pedido?",
            mensaje = "Esta acción no se puede deshacer.",
            textoConfirmar = "Sí, eliminar",
            onConfirmar = {
                viewModel.eliminar(pedido.id)
                pedidoAEliminar = null
            },
            onCancelar = { pedidoAEliminar = null }
        )
    }
    pedidoACancelar?.let { pedido ->
        ConfirmDialog(
            titulo = "¿Cancelar pedido?",
            mensaje = "El pedido pasará a estado CANCELADO.",
            textoConfirmar = "Sí, cancelar",
            onConfirmar = {
                viewModel.cancelar(pedido.id)
                pedidoACancelar = null
            },
            onCancelar = { pedidoACancelar = null }
        )
    }
    pedidoAConfirmarRetiro?.let { pedido ->
        ConfirmDialog(
            titulo = "¿Confirmar retiro?",
            mensaje = "Confirmas que ya retiraste tu pedido del local.",
            textoConfirmar = "Sí, ya lo retiré",
            onConfirmar = {
                viewModel.confirmarRetiro(pedido.id)
                pedidoAConfirmarRetiro = null
            },
            onCancelar = { pedidoAConfirmarRetiro = null }
        )
    }
}

@Composable
private fun OrdersTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = Blue600
            )
        }
        Text(
            text = "Mis Pedidos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun PedidoCard(
    pedido: Pedido,
    procesando: Boolean,
    confirmadoLocalmente: Boolean = false,
    resaltado: Boolean = false,
    onClick: () -> Unit,
    onCancelar: () -> Unit,
    onConfirmarRetiro: () -> Unit,
    onEliminar: () -> Unit
) {
    val borderModifier = if (resaltado) {
        Modifier.border(width = 2.dp, color = Green600, shape = RoundedCornerShape(16.dp))
    } else Modifier

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(borderModifier)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(visible = resaltado, enter = fadeIn(), exit = fadeOut()) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Green100)
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "● Actualizado en tiempo real",
                    style = MaterialTheme.typography.labelSmall,
                    color = Green800,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pedido.createdAt?.take(16)?.replace("T", " ") ?: pedido.id.take(8),
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
            EstadoChip(estado = pedido.estado)
        }
        Text(
            text = formatPrecio(pedido.total),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = tipoEntregaTexto(pedido.tipoEntrega),
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
            if (pedido.estado != "CANCELADO" && pedido.estado != "PENDIENTE") {
                Spacer(modifier = Modifier.size(10.dp))
                MiniOrderTimeline(
                    estado = pedido.estado,
                    tipoEntrega = pedido.tipoEntrega,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (pedido.descuentoAplicado > 0) {
            Text(
                text = "Descuento aplicado: -${formatPrecio(pedido.descuentoAplicado)}",
                style = MaterialTheme.typography.bodySmall,
                color = Green700
            )
        }
        pedido.pago?.let { pago ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (pago.estado == "PAGADO") Green100 else Red100)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = pago.estado ?: "—",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (pago.estado == "PAGADO") Green800 else Red700,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = pago.metodoPago ?: "—",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }
        pedido.entrega?.let { entrega ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (entrega.estado == "ENTREGADA") Green100 else Orange100)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = entrega.estado ?: "—",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (entrega.estado == "ENTREGADA") Green800 else Orange700,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = entrega.ubicacion ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }
        if (pedido.detalles.isNotEmpty()) {
            Column {
                pedido.detalles.forEach { d ->
                    DetalleLinea(d)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (pedido.estado == "PENDIENTE") {
                Button(
                    onClick = onCancelar,
                    enabled = !procesando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red500,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Cancelar", style = MaterialTheme.typography.labelMedium)
                }
            }
            if (pedido.estado == "LISTO" && pedido.tipoEntrega == "RETIRO_LOCAL" && !confirmadoLocalmente) {
                Button(
                    onClick = onConfirmarRetiro,
                    enabled = !procesando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green600,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Confirmar Retiro", style = MaterialTheme.typography.labelMedium)
                }
            }
            if (confirmadoLocalmente) {
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓ Confirmado",
                        style = MaterialTheme.typography.labelMedium,
                        color = Green700,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (pedido.estado in listOf("PENDIENTE", "CANCELADO", "ENTREGADO")) {
                IconButton(
                    onClick = onEliminar,
                    enabled = !procesando,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Gray500
                    )
                }
            }
        }
    }
}

@Composable
private fun DetalleLinea(detalle: DetallePedido) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${detalle.cantidad}× ${detalle.producto ?: "—"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = formatPrecio(detalle.subtotal),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun EstadoChip(estado: String) {
    val (bg, fg) = when (estado) {
        "PENDIENTE" -> Yellow100 to Yellow800
        "CONFIRMADO" -> Blue100 to Blue600
        "EN_PREPARACION" -> Purple100 to Purple700
        "LISTO" -> Green100 to Green800
        "EN_CAMINO" -> Orange100 to Orange700
        "ENTREGADO" -> Green100 to Green800
        "CANCELADO" -> Red100 to Red700
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = estado,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ConfirmDialog(
    titulo: String,
    mensaje: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = {
            TextButton(onClick = onConfirmar) { Text(textoConfirmar) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

private fun tipoEntregaTexto(tipo: String?): String = when (tipo) {
    "RETIRO_LOCAL" -> "Retiro en local"
    "ENTREGA_INTERNA" -> "Entrega interna"
    else -> tipo ?: "—"
}

private fun formatPrecio(value: Double): String =
    "$${String.format(java.util.Locale.US, "%.2f", value)}"
