package com.uambite.app.ui.localadmin.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uambite.app.domain.model.Pedido
import com.uambite.app.ui.admin.AdminCard
import com.uambite.app.ui.admin.tabs.BtnAccion
import com.uambite.app.ui.admin.tabs.EstadoChip
import com.uambite.app.ui.common.EmptyBox
import com.uambite.app.ui.common.LoadingBox
import com.uambite.app.ui.localadmin.LocalAdminViewModel
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Gray500
import com.uambite.app.ui.theme.Green700
import com.uambite.app.ui.theme.Green800
import com.uambite.app.ui.theme.Orange700
import com.uambite.app.ui.theme.Purple700
import com.uambite.app.ui.theme.Red700
import java.util.Locale

@Composable
fun LocalAdminPedidosTab(viewModel: LocalAdminViewModel) {
    val pedidos by viewModel.pedidos.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val isLoading = "pedidos" in loading

    if (isLoading && pedidos.isEmpty()) {
        LoadingBox()
        return
    }
    if (pedidos.isEmpty()) {
        EmptyBox(message = "No hay pedidos en tus locales")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Pedidos de tus locales",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        pedidos.forEach { pedido ->
            PedidoRowAdmin(
                pedido = pedido,
                onAccion = { estado -> viewModel.cambiarEstadoPedido(pedido.id, estado) },
                onPrioridad = { delta -> viewModel.setPrioridad(pedido.id, pedido.prioridad + delta) }
            )
        }
    }
}

@Composable
private fun PedidoRowAdmin(
    pedido: Pedido,
    onAccion: (String) -> Unit,
    onPrioridad: (Int) -> Unit
) {
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
            text = "${pedido.tipoEntrega ?: "—"} · Prioridad ${pedido.prioridad}",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )
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
            if (pedido.estado in listOf("PENDIENTE", "CONFIRMADO", "EN_PREPARACION")) {
                BtnAccion("Cancelar", Red700) { onAccion("CANCELADO") }
            }
        }
    }
}

private fun formatPrecio(v: Double): String = "$${String.format(Locale.US, "%.2f", v)}"
