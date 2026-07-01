package com.uambite.app.ui.admin.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uambite.app.domain.model.Pedido
import com.uambite.app.ui.admin.AdminCard
import com.uambite.app.ui.admin.AdminTabScaffold
import com.uambite.app.ui.admin.AdminViewModel
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Gray500
import com.uambite.app.ui.theme.Green700
import com.uambite.app.ui.theme.Green800
import com.uambite.app.ui.theme.Orange700
import com.uambite.app.ui.theme.Purple700
import com.uambite.app.ui.theme.Red700
import java.util.Locale

@Composable
fun AdminPedidosTab(viewModel: AdminViewModel) {
    val pedidos by viewModel.pedidos.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val isLoading = "pedidos" in loading

    var pedidoParaEntrega by remember { mutableStateOf<Pedido?>(null) }
    var ubicacion by remember { mutableStateOf("") }

    AdminTabScaffold(
        items = pedidos,
        loading = isLoading,
        textoVacio = "No hay pedidos",
        textoNuevo = "Actualizar",
        onNuevo = { viewModel.loadTab("pedidos") },
        key = { it.id }
    ) { pedido ->
        AdminCard {
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
            Text(
                text = "${tipoEntregaTexto(pedido.tipoEntrega)}",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
            if (pedido.detalles.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    pedido.detalles.forEach { d ->
                        Text(
                            text = "${d.cantidad}× ${d.producto ?: "—"}  ${formatPrecio(d.subtotal)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            pedido.pago?.let { pago ->
                Text(
                    text = "Pago: ${pago.metodoPago} · ${pago.estado}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (pago.estado == "PAGADO") Green700 else Red700
                )
            }
            pedido.entrega?.let { entrega ->
                Text(
                    text = "Entrega: ${entrega.estado} · ${entrega.ubicacion ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
            Spacer(Modifier.size(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (pedido.estado == "PENDIENTE") {
                    BtnAccion("Confirmar", Blue600) {
                        viewModel.cambiarEstadoPedido(pedido.id, "CONFIRMADO")
                    }
                }
                if (pedido.estado == "CONFIRMADO") {
                    BtnAccion("Preparar", Purple700) {
                        viewModel.cambiarEstadoPedido(pedido.id, "EN_PREPARACION")
                    }
                }
                if (pedido.estado == "EN_PREPARACION") {
                    BtnAccion("Listo", Green700) {
                        viewModel.cambiarEstadoPedido(pedido.id, "LISTO")
                    }
                }
                if (pedido.estado in listOf("PENDIENTE", "CONFIRMADO", "EN_PREPARACION")) {
                    BtnAccion("Cancelar", Red700) {
                        viewModel.cambiarEstadoPedido(pedido.id, "CANCELADO")
                    }
                }
                if (pedido.estado == "LISTO" &&
                    pedido.pago?.estado == "PAGADO" &&
                    pedido.tipoEntrega == "ENTREGA_INTERNA"
                ) {
                    BtnAccion("Crear Entrega", Orange700) {
                        pedidoParaEntrega = pedido
                        ubicacion = ""
                    }
                }
                if (pedido.estado == "EN_CAMINO" && pedido.entrega != null) {
                    BtnAccion("Finalizar Entrega", Green800) {
                        pedido.entrega.id?.let { viewModel.finalizarEntrega(it) }
                    }
                }
            }
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
private fun tipoEntregaTexto(tipo: String?): String = when (tipo) {
    "RETIRO_LOCAL" -> "Retiro en local"
    "ENTREGA_INTERNA" -> "Entrega interna"
    else -> tipo ?: "—"
}

private fun formatPrecio(value: Double): String =
    "$${String.format(Locale.US, "%.2f", value)}"
