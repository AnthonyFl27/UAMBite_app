package com.uambite.app.ui.pedidodetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
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
import com.uambite.app.data.realtime.PedidoStatus
import com.uambite.app.domain.model.Pedido
import com.uambite.app.ui.common.ErrorBox
import com.uambite.app.ui.common.LoadingBox
import com.uambite.app.ui.common.OrderTimeline
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

@Composable
fun PedidoDetailScreen(
    onBack: () -> Unit,
    viewModel: PedidoDetailViewModel = hiltViewModel()
) {
    val state by viewModel.viewState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.status) {
        val msg = state.status ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            PedidoStatus.mensajeLegible(msg.estado, msg.tipoEntrega, msg.mensaje)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailTopBar(
                isLive = state.status != null,
                onBack = onBack,
                onRefresh = { viewModel.load() }
            )
            when {
                state.loading && state.pedido == null -> LoadingBox()
                state.error != null && state.pedido == null -> ErrorBox(
                    message = state.error!!,
                    onRetry = { viewModel.load() }
                )
                state.pedido != null -> PedidoDetailContent(
                    pedido = state.pedido!!,
                    onCancelar = { showCancelDialog = true },
                    onConfirmarRetiro = { viewModel.confirmarRetiro() }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("¿Cancelar pedido?") },
            text = { Text("El pedido pasará a estado CANCELADO.") },
            confirmButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Cerrar") }
            }
        )
    }
}

@Composable
private fun DetailTopBar(
    isLive: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Blue600
            )
        }
        Text(
            text = "Detalle del Pedido",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )
        Box(modifier = Modifier.weight(1f))
        if (isLive) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Green100)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = Green700,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = " En vivo",
                    style = MaterialTheme.typography.labelSmall,
                    color = Green800,
                    fontWeight = FontWeight.SemiBold
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
        }
        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refrescar",
                tint = Gray500
            )
        }
    }
}

@Composable
private fun PedidoDetailContent(
    pedido: Pedido,
    onCancelar: () -> Unit,
    onConfirmarRetiro: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OrderTimeline(
                    estado = pedido.estado,
                    tipoEntrega = pedido.tipoEntrega
                )
                ResumenPedidoCard(pedido = pedido)
            }
        }
        item {
            DetallesCard(pedido = pedido)
        }
        item {
            PagoCard(pedido = pedido)
        }
        pedido.entrega?.let { e ->
            item {
                EntregaCard(ubicacion = e.ubicacion, estado = e.estado)
            }
        }
        item {
            AccionesCard(
                pedido = pedido,
                onCancelar = onCancelar,
                onConfirmarRetiro = onConfirmarRetiro
            )
        }
    }
}

@Composable
private fun ResumenPedidoCard(pedido: Pedido) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Pedido #${pedido.id.take(8)}",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
            Text(
                text = tipoEntregaTexto(pedido.tipoEntrega),
                style = MaterialTheme.typography.bodyMedium
            )
            pedido.createdAt?.let {
                Text(
                    text = it.replace("T", " ").take(16),
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500
                )
            }
        }
        Text(
            text = formatPrecio(pedido.total),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DetallesCard(pedido: Pedido) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Productos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (pedido.detalles.isEmpty()) {
            Text(
                text = "Sin productos",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
        } else {
            pedido.detalles.forEach { d ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${d.cantidad}× ${d.producto ?: "—"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (d.ingredientesExtra.isNotEmpty()) {
                            Text(
                                text = d.ingredientesExtra.joinToString { it.nombre ?: "extra" },
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500
                            )
                        }
                    }
                    Text(
                        text = formatPrecio(d.subtotal),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", style = MaterialTheme.typography.bodySmall, color = Gray500)
                Text(formatPrecio(pedido.subtotal), style = MaterialTheme.typography.bodySmall)
            }
            if (pedido.descuentoAplicado > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Descuento",
                        style = MaterialTheme.typography.bodySmall,
                        color = Green700
                    )
                    Text(
                        "-${formatPrecio(pedido.descuentoAplicado)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Green700
                    )
                }
            }
        }
    }
}

@Composable
private fun PagoCard(pedido: Pedido) {
    val pago = pedido.pago
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Pago",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (pago == null) {
            Text(
                text = "Sin pago registrado",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Método", style = MaterialTheme.typography.bodySmall, color = Gray500)
                Text(pago.metodoPago ?: "—", style = MaterialTheme.typography.bodySmall)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Monto", style = MaterialTheme.typography.bodySmall, color = Gray500)
                Text(formatPrecio(pago.monto), style = MaterialTheme.typography.bodySmall)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Estado", style = MaterialTheme.typography.bodySmall, color = Gray500)
                EstadoPagoChip(pago.estado ?: "—")
            }
        }
    }
}

@Composable
private fun EntregaCard(ubicacion: String?, estado: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Entrega",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text("Ubicación: ${ubicacion ?: "—"}", style = MaterialTheme.typography.bodySmall)
        Text("Estado: ${estado ?: "—"}", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AccionesCard(
    pedido: Pedido,
    onCancelar: () -> Unit,
    onConfirmarRetiro: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (pedido.estado == PedidoStatus.PENDIENTE) {
            Button(
                onClick = onCancelar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Red500, contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Cancelar") }
        }
        if (pedido.estado == PedidoStatus.LISTO &&
            pedido.tipoEntrega == PedidoStatus.RETIRO_LOCAL
        ) {
            Button(
                onClick = onConfirmarRetiro,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green600, contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Confirmar Retiro") }
        }
    }
}

@Composable
private fun EstadoPagoChip(estado: String) {
    val (bg, fg) = when (estado) {
        "PAGADO" -> Green100 to Green800
        else -> Red100 to Red700
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = estado,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun tipoEntregaTexto(tipo: String?): String = when (tipo) {
    "RETIRO_LOCAL" -> "Retiro en local"
    "ENTREGA_INTERNA" -> "Entrega interna"
    else -> tipo ?: "—"
}

private fun formatPrecio(value: Double): String =
    "$${String.format(java.util.Locale.US, "%.2f", value)}"
