package com.uambite.app.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uambite.app.data.realtime.PedidoStatus
import com.uambite.app.ui.theme.Blue100
import com.uambite.app.ui.theme.Blue500
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Blue700
import com.uambite.app.ui.theme.Gray200
import com.uambite.app.ui.theme.Gray300
import com.uambite.app.ui.theme.Gray500
import com.uambite.app.ui.theme.Gray700
import com.uambite.app.ui.theme.Green100
import com.uambite.app.ui.theme.Green600
import com.uambite.app.ui.theme.Green700
import com.uambite.app.ui.theme.Red100
import com.uambite.app.ui.theme.Red500
import com.uambite.app.ui.theme.White
import com.uambite.app.ui.theme.Yellow100
import com.uambite.app.ui.theme.Yellow800

internal data class TimelineStep(
    val key: String,
    val label: String,
    val icon: ImageVector
)

internal object OrderTimelineSteps {
    fun forTipoEntrega(tipoEntrega: String?): List<TimelineStep> = when (tipoEntrega) {
        PedidoStatus.RETIRO_LOCAL -> listOf(
            TimelineStep(PedidoStatus.CONFIRMADO, "Confirmado", Icons.Default.CheckCircle),
            TimelineStep(PedidoStatus.EN_PREPARACION, "Preparando", Icons.Default.Restaurant),
            TimelineStep(PedidoStatus.LISTO, "Listo", Icons.Default.Done),
            TimelineStep(PedidoStatus.ENTREGADO, "Entregado", Icons.Default.DeliveryDining)
        )
        PedidoStatus.ENTREGA_INTERNA -> listOf(
            TimelineStep(PedidoStatus.CONFIRMADO, "Confirmado", Icons.Default.CheckCircle),
            TimelineStep(PedidoStatus.EN_PREPARACION, "Preparando", Icons.Default.Restaurant),
            TimelineStep(PedidoStatus.LISTO, "Listo", Icons.Default.Done),
            TimelineStep(PedidoStatus.EN_CAMINO, "Enviado", Icons.Default.LocalShipping),
            TimelineStep(PedidoStatus.ENTREGADO, "Entregado", Icons.Default.DeliveryDining)
        )
        else -> emptyList()
    }
}

@Composable
fun OrderTimeline(
    estado: String,
    tipoEntrega: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LiveDot()
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Estado de tu pedido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            EstadoBadge(estado = estado)
        }
        Spacer(Modifier.height(18.dp))

        when (estado) {
            PedidoStatus.CANCELADO -> CancelledBanner()
            PedidoStatus.PENDIENTE -> PendingBanner()
            else -> ActiveTimeline(estado = estado, tipoEntrega = tipoEntrega)
        }
    }
}

@Composable
private fun ActiveTimeline(estado: String, tipoEntrega: String?) {
    val steps = remember(tipoEntrega) { OrderTimelineSteps.forTipoEntrega(tipoEntrega) }
    if (steps.isEmpty()) {
        PendingBanner()
        return
    }
    val currentIndex = steps.indexOfFirst { it.key == estado }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        steps.forEachIndexed { index, step ->
            val isCompleted = index < currentIndex
            val isCurrent = index == currentIndex
            val leftActive = index > 0 && index <= currentIndex
            val rightActive = index < currentIndex
            TimelineNode(
                step = step,
                isCompleted = isCompleted,
                isCurrent = isCurrent,
                showLeftLine = index > 0,
                showRightLine = index < steps.lastIndex,
                leftLineActive = leftActive,
                rightLineActive = rightActive,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TimelineNode(
    step: TimelineStep,
    isCompleted: Boolean,
    isCurrent: Boolean,
    showLeftLine: Boolean,
    showRightLine: Boolean,
    leftLineActive: Boolean,
    rightLineActive: Boolean,
    modifier: Modifier = Modifier
) {
    val targetColor = when {
        isCompleted -> Green600
        isCurrent -> Blue500
        else -> Gray200
    }
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "stepColor"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConnectorLine(visible = showLeftLine, active = leftLineActive)
            StepCircle(
                step = step,
                isCompleted = isCompleted,
                isCurrent = isCurrent
            )
            ConnectorLine(visible = showRightLine, active = rightLineActive)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = step.label,
            style = MaterialTheme.typography.labelSmall,
            color = when {
                isCurrent -> Blue700
                isCompleted -> Green700
                else -> Gray500
            },
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun RowScope.ConnectorLine(visible: Boolean, active: Boolean) {
    if (!visible) {
        Spacer(Modifier.weight(1f))
        return
    }
    val target = if (active) Green600 else Gray200
    val animatedColor by animateColorAsState(target, tween(500), label = "lineColor")
    Box(
        modifier = Modifier
            .weight(1f)
            .height(3.dp)
            .padding(horizontal = 1.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(animatedColor)
    )
}

@Composable
private fun StepCircle(
    step: TimelineStep,
    isCompleted: Boolean,
    isCurrent: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable<Float>(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val targetScale = if (isCurrent) pulse else 1f
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "circleScale"
    )

    val bgColor = when {
        isCompleted -> Green600
        isCurrent -> Blue500
        else -> Color.Transparent
    }
    val borderColor = when {
        isCompleted -> Green600
        isCurrent -> Blue500
        else -> Gray300
    }
    val borderWidth = if (isCurrent) 3.dp else 2.dp
    val size = if (isCurrent) 40.dp else 36.dp

    Box(
        modifier = Modifier
            .size(size)
            .scale(animatedScale)
            .clip(CircleShape)
            .background(bgColor)
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(18.dp)
            )
        } else if (isCurrent) {
            Icon(
                imageVector = step.icon,
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun LiveDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "liveDot")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable<Float>(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveAlpha"
    )
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(Green600.copy(alpha = alpha))
    )
}

@Composable
private fun EstadoBadge(estado: String) {
    val (bg, fg) = when (estado) {
        PedidoStatus.PENDIENTE -> Yellow100 to Yellow800
        PedidoStatus.CONFIRMADO -> Blue100 to Blue700
        PedidoStatus.EN_PREPARACION -> Blue100 to Blue700
        PedidoStatus.LISTO -> Green100 to Green700
        PedidoStatus.EN_CAMINO -> Blue100 to Blue700
        PedidoStatus.ENTREGADO -> Green100 to Green700
        PedidoStatus.CANCELADO -> Red100 to Red500
        else -> MaterialTheme.colorScheme.surfaceVariant to Gray700
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
private fun CancelledBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Red100)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Cancel,
            contentDescription = null,
            tint = Red500
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = "Tu pedido fue cancelado",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Red500
            )
            Text(
                text = "Si tienes dudas, contacta al local.",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
        }
    }
}

@Composable
private fun PendingBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Yellow100)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.HourglassEmpty,
            contentDescription = null,
            tint = Yellow800
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = "Esperando confirmación del local",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Yellow800
            )
            Text(
                text = "Te avisaremos en cuanto acepten tu pedido.",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
        }
    }
}

@Composable
fun MiniOrderTimeline(
    estado: String,
    tipoEntrega: String?,
    modifier: Modifier = Modifier
) {
    if (estado == PedidoStatus.CANCELADO || estado == PedidoStatus.PENDIENTE) {
        return
    }
    val steps = remember(tipoEntrega) { OrderTimelineSteps.forTipoEntrega(tipoEntrega) }
    if (steps.isEmpty()) return
    val currentIndex = steps.indexOfFirst { it.key == estado }
    if (currentIndex < 0) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, _ ->
            val isCompleted = index < currentIndex
            val isCurrent = index == currentIndex
            val color = when {
                isCompleted -> Green600
                isCurrent -> Blue500
                else -> Gray200
            }
            val dotSize = if (isCurrent) 12.dp else 8.dp
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(color)
            )
            if (index < steps.lastIndex) {
                val lineColor = if (index < currentIndex) Green600 else Gray200
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(lineColor)
                )
            }
        }
    }
}
