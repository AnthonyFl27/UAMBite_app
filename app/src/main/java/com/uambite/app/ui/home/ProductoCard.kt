package com.uambite.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.uambite.app.domain.model.Producto
import com.uambite.app.ui.common.NetworkImage
import com.uambite.app.ui.common.StockBadge
import com.uambite.app.ui.theme.Blue100
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Gray500
import com.uambite.app.ui.theme.Purple500

@Composable
fun ProductoCard(
    producto: Producto,
    onClick: () -> Unit,
    onAgregar: () -> Unit,
    modifier: Modifier = Modifier,
    fullWidth: Boolean = false
) {
    if (fullWidth) {
        FullWidthProductoCard(
            producto = producto,
            onClick = onClick,
            onAgregar = onAgregar,
            modifier = modifier
        )
    } else {
        CompactProductoCard(
            producto = producto,
            onClick = onClick,
            onAgregar = onAgregar,
            modifier = modifier
        )
    }
}

@Composable
private fun CompactProductoCard(
    producto: Producto,
    onClick: () -> Unit,
    onAgregar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        if (producto.tieneImagen) {
            NetworkImage(
                url = ImageUrlBuilder.producto(producto.id),
                contentDescription = producto.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Blue100)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            StockBadge(stock = producto.stock)
        }
        Text(
            text = producto.nombre + if (producto.permitePersonalizacion) " ✎" else "",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Store,
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = producto.localComida,
                style = MaterialTheme.typography.labelSmall,
                color = Gray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$${String.format("%.2f", producto.precio)}",
                style = MaterialTheme.typography.bodyLarge,
                color = Blue600,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAgregar,
                enabled = producto.stock > 0,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(32.dp),
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Text("Agregar", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun FullWidthProductoCard(
    producto: Producto,
    onClick: () -> Unit,
    onAgregar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
    ) {
        if (producto.tieneImagen) {
            NetworkImage(
                url = ImageUrlBuilder.producto(producto.id),
                contentDescription = producto.nombre,
                modifier = Modifier
                    .fillMaxHeight()
                    .size(120.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .size(120.dp)
                    .background(Blue100)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = producto.nombre + if (producto.permitePersonalizacion) " ✎" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                StockBadge(stock = producto.stock)
            }
            Text(
                text = producto.localComida,
                style = MaterialTheme.typography.labelSmall,
                color = Gray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = producto.descripcion ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$${String.format("%.2f", producto.precio)}",
                style = MaterialTheme.typography.bodyLarge,
                color = Blue600,
                fontWeight = FontWeight.Bold
            )
        }
        Button(
            onClick = onAgregar,
            enabled = producto.stock > 0,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 12.dp)
                .height(36.dp),
            contentPadding = ButtonDefaults.ContentPadding
        ) {
            Text("Agregar", style = MaterialTheme.typography.labelSmall)
        }
    }
}
