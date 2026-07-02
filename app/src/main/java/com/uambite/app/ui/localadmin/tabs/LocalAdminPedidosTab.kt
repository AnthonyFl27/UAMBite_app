package com.uambite.app.ui.localadmin.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uambite.app.domain.model.Pedido
import com.uambite.app.ui.admin.tabs.BtnAccion
import com.uambite.app.ui.admin.tabs.EstadoChip
import com.uambite.app.ui.common.EmptyBox
import com.uambite.app.ui.common.LoadingBox
import com.uambite.app.ui.common.MiniOrderTimeline
import com.uambite.app.ui.localadmin.LocalAdminViewModel
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Gray500
import com.uambite.app.ui.theme.Green700
import com.uambite.app.ui.theme.Orange700
import com.uambite.app.ui.theme.Purple700
import com.uambite.app.ui.theme.Red700
import java.util.Locale

@Composable
fun LocalAdminPedidosTab(viewModel: LocalAdminViewModel) {
    val pedidos by viewModel.pedidos.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val resaltados by viewModel.pedidosResaltados.collectAsState()
    val isLoading = "pedidos" in loading

    var pedidoParaEntrega by remember { mutableStateOf<Pedido?>(null) }
    var ubicacion by remember { mutableStateOf("") }

    if (isLoading && pedidos.isEmpty()) {
        LoadingBox()
        return
    }
    if (pedidos.isEmpty()) {
        EmptyBox(message = "No hay pedidos en tus locales")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Pedidos de tus locales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        items(pedidos, key = { it.id }) { pedido ->
            PedidoRowAdmin(
                pedido = pedido,
                resaltado = pedido.id in resaltados,
                onAccion = { estado -> viewModel.cambiarEstadoPedido(pedido.id, estado) },
                onCrearEntrega = { pedidoParaEntrega = pedido; ubicacion = "" },
                onClick = { viewModel.consumirResaltado(pedido.id) }
            )
        }
    }

    pedidoParaEntrega?.let { pedido ->
        AlertDialog(
            onDismissRequest = { pedidoParaEntrega = null },
            title = { Text("Crear Entrega") },
            text = {
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación") },
                    placeholder = { Text("ej: Recepción Edificio A") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (ubicacion.isNotBlank()) {
                        viewModel.crearEntrega(pedido.id, ubicacion.trim())
                        pedidoParaEntrega = null
                    }
                }) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { pedidoParaEntrega = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun PedidoRowAdmin(
    pedido: Pedido,
    resaltado: Boolean = false,
    onAccion: (String) -> Unit,
    onCrearEntrega: () -> Unit,
    onClick: () -> Unit = {}
) {
    val borderMod = if (resaltado) {
        Modifier.border(width = 2.dp, color = Green700, shape = RoundedCornerShape(14.dp))
    } else Modifier

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(borderMod)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = pedido.id.take(8) + "…",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
                modifier = Modifier.weight(1f)
            )
            EstadoChip(pedido.estado)
        }
        Text(
            text = "Cliente: ${pedido.usuario ?: pedido.usuarioId?.take(8) ?: "—"}",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )
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
        Spacer(Modifier.size(4.dp))
        Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
            if (pedido.estado == "PENDIENTE") {
                BtnAccion("Confirmar", Blue600) { onAccion("CONFIRMADO") }
            }
            if (pedido.estado == "CONFIRMADO") {
                BtnAccion("Preparar", Purple700) { onAccion("EN_PREPARACION") }
            }
            if (pedido.estado == "EN_PREPARACION") {
                BtnAccion("Listo", Green700) { onAccion("LISTO") }
            }
            if (pedido.estado == "LISTO" && pedido.tipoEntrega == "ENTREGA_INTERNA") {
                BtnAccion("Enviar", Orange700) { onCrearEntrega() }
            }
            if (pedido.estado == "LISTO" && pedido.tipoEntrega == "RETIRO_LOCAL") {
                BtnAccion("Entregar", com.uambite.app.ui.theme.Green800) { onAccion("ENTREGADO") }
            }
            if (pedido.estado in listOf("PENDIENTE", "CONFIRMADO", "EN_PREPARACION")) {
                BtnAccion("Cancelar", Red700) { onAccion("CANCELADO") }
            }
        }
    }
}

private fun tipoEntregaTexto(tipo: String?): String = when (tipo) {
    "RETIRO_LOCAL" -> "Retiro en local"
    "ENTREGA_INTERNA" -> "Entrega interna"
    else -> tipo ?: "—"
}

private fun formatPrecio(v: Double): String = "$${String.format(Locale.US, "%.2f", v)}"
