package com.uambite.app.ui.admin.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uambite.app.ui.theme.Blue100
import com.uambite.app.ui.theme.Blue700
import com.uambite.app.ui.theme.Green100
import com.uambite.app.ui.theme.Green800
import com.uambite.app.ui.theme.Orange100
import com.uambite.app.ui.theme.Orange700
import com.uambite.app.ui.theme.Purple100
import com.uambite.app.ui.theme.Purple700
import com.uambite.app.ui.theme.Red100
import com.uambite.app.ui.theme.Red700
import com.uambite.app.ui.theme.Yellow100
import com.uambite.app.ui.theme.Yellow800

@Composable
fun BtnAccion(texto: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.size(width = 110.dp, height = 32.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        )
    ) {
        Text(texto, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun EstadoChip(estado: String) {
    val (bg, fg) = when (estado) {
        "PENDIENTE" -> Yellow100 to Yellow800
        "CONFIRMADO" -> Blue100 to Blue700
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
