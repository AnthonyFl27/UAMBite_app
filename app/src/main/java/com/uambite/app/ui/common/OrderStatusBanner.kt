package com.uambite.app.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uambite.app.data.realtime.OrderStatusEvent
import com.uambite.app.data.realtime.PedidoStatus
import com.uambite.app.data.realtime.WebSocketManager
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Gray500
import com.uambite.app.ui.theme.Green100
import com.uambite.app.ui.theme.Green700
import com.uambite.app.ui.theme.Green800
import com.uambite.app.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun OrderStatusBannerHost(
    webSocketManager: WebSocketManager,
    onVerPedido: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var visibleEvent by remember { mutableStateOf<OrderStatusEvent.PedidoUpdate?>(null) }
    var dismissToken by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        webSocketManager.events.collect { event ->
            if (event is OrderStatusEvent.PedidoUpdate) {
                visibleEvent = event
                dismissToken += 1
            }
        }
    }

    LaunchedEffect(visibleEvent, dismissToken) {
        if (visibleEvent != null) {
            val captured = dismissToken
            delay(5_000)
            if (captured == dismissToken) {
                visibleEvent = null
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visibleEvent != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            val ev = visibleEvent
            if (ev != null) {
                StatusBannerCard(
                    title = "Actualización de tu pedido",
                    body = PedidoStatus.mensajeLegible(
                        ev.status.estado,
                        ev.status.tipoEntrega,
                        ev.status.mensaje
                    ),
                    onVer = {
                        onVerPedido(ev.pedidoId)
                        visibleEvent = null
                    },
                    onCerrar = { visibleEvent = null }
                )
            }
        }
    }
}

@Composable
private fun StatusBannerCard(
    title: String,
    body: String,
    onVer: () -> Unit,
    onCerrar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Green100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = Green700
            )
        }
        Spacer(Modifier.size(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Gray500,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        TextButton(
            onClick = onVer,
            colors = ButtonDefaults.textButtonColors(contentColor = Blue600)
        ) { Text("Ver") }
        IconButton(onClick = onCerrar) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = Gray500
            )
        }
    }
}
