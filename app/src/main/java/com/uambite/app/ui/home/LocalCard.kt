package com.uambite.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uambite.app.data.api.ImageUrlBuilder
import com.uambite.app.domain.model.Local
import com.uambite.app.domain.model.Producto
import com.uambite.app.ui.common.NetworkImage
import com.uambite.app.ui.common.StockBadge
import com.uambite.app.ui.theme.Blue100
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Blue700
import com.uambite.app.ui.theme.Gray500

@Composable
fun LocalCard(
    local: Local,
    productos: List<Producto>,
    onClick: () -> Unit,
    onProductoClick: (Producto) -> Unit,
    modifier: Modifier = Modifier
) {
    val sugeridos = productos.take(4)
    val count = productos.size

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            if (local.tieneImagen) {
                NetworkImage(
                    url = ImageUrlBuilder.local(local.id),
                    contentDescription = local.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = Blue600,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = local.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Gray500,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${local.ubicacion} · ${local.horario ?: "Sin horario"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Productos Sugeridos",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(Blue100)
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$count en total",
                            style = MaterialTheme.typography.labelSmall,
                            color = Blue700
                        )
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sugeridos) { producto ->
                    SugeridoCard(
                        producto = producto,
                        onClick = { onProductoClick(producto) }
                    )
                }
                if (count > sugeridos.size) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(width = 80.dp, height = 120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Blue100),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${count - sugeridos.size} más",
                                style = MaterialTheme.typography.labelSmall,
                                color = Blue600,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Ver local completo →",
                    style = MaterialTheme.typography.bodySmall,
                    color = Blue600,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SugeridoCard(
    producto: Producto,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(width = 144.dp, height = 140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = producto.stock > 0) { onClick() }
            .padding(8.dp)
    ) {
        if (producto.tieneImagen) {
            NetworkImage(
                url = ImageUrlBuilder.producto(producto.id),
                contentDescription = producto.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = producto.nombre,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = producto.descripcion ?: " ",
            style = MaterialTheme.typography.labelSmall,
            color = Gray500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$${String.format("%.2f", producto.precio)}",
                style = MaterialTheme.typography.labelMedium,
                color = Blue600,
                fontWeight = FontWeight.Bold
            )
            StockBadge(stock = producto.stock)
        }
    }
}
