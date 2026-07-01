package com.uambite.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uambite.app.data.api.ImageUrlBuilder
import com.uambite.app.domain.model.IngredienteExtra
import com.uambite.app.domain.model.Producto
import com.uambite.app.ui.theme.Blue100
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Blue700
import com.uambite.app.ui.theme.Gray500
import com.uambite.app.ui.theme.Purple500
import java.util.Locale

data class ModalAddResult(
    val cantidad: Int,
    val extrasIds: List<String>,
    val extrasPrecios: Map<String, Double>,
    val extrasNombres: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoModalSheet(
    producto: Producto,
    extrasDisponibles: List<IngredienteExtra>,
    onDismiss: () -> Unit,
    onConfirm: (ModalAddResult) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        ModalContent(
            producto = producto,
            extrasDisponibles = extrasDisponibles,
            sheetState = sheetState,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModalContent(
    producto: Producto,
    extrasDisponibles: List<IngredienteExtra>,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirm: (ModalAddResult) -> Unit
) {
    val extrasSeleccionados = remember { mutableStateMapOf<String, IngredienteExtra>() }
    var cantidad by remember { mutableStateOf(1) }

    val extrasSubtotal = extrasSeleccionados.values.sumOf { it.precioExtra }
    val subtotalUnitario = producto.precio + extrasSubtotal
    val subtotal = subtotalUnitario * cantidad

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        if (producto.tieneImagen) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Blue100)
            ) {
                NetworkImage(
                    url = ImageUrlBuilder.producto(producto.id),
                    contentDescription = producto.nombre,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(50)
                        )
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Blue100)
                    .padding(8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(50)
                        )
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (producto.permitePersonalizacion) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Personalizable",
                        tint = Purple500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (!producto.descripcion.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = producto.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatPrecio(producto.precio),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Blue600,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(12.dp))
                StockBadge(stock = producto.stock)
            }

            if (producto.permitePersonalizacion && extrasDisponibles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ingredientes Extra",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    extrasDisponibles.forEach { extra ->
                        val checked = extrasSeleccionados[extra.id] != null
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        extrasSeleccionados[extra.id] = extra
                                    } else {
                                        extrasSeleccionados.remove(extra.id)
                                    }
                                }
                            )
                            Text(
                                text = extra.nombre,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "+${formatPrecio(extra.precioExtra)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Blue600,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Cantidad:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.size(12.dp))
                CantidadStepper(
                    cantidad = cantidad,
                    onCambiar = { delta ->
                        val nueva = (cantidad + delta).coerceIn(1, producto.stock.coerceAtLeast(1))
                        cantidad = nueva
                    },
                    enabled = producto.stock > 0
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subtotal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                    Text(
                        text = formatPrecio(subtotal),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Blue700
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val ids = extrasSeleccionados.keys.toList()
                    val precios = extrasSeleccionados.values.associate { it.id to it.precioExtra }
                    val nombres = extrasSeleccionados.values.map { it.nombre }
                    onConfirm(
                        ModalAddResult(
                            cantidad = cantidad,
                            extrasIds = ids,
                            extrasPrecios = precios,
                            extrasNombres = nombres
                        )
                    )
                },
                enabled = producto.stock > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Agregar al Carrito",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CantidadStepper(
    cantidad: Int,
    onCambiar: (Int) -> Unit,
    enabled: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        IconButton(
            onClick = { onCambiar(-1) },
            enabled = enabled && cantidad > 1,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Disminuir", tint = Blue600)
        }
        Text(
            text = cantidad.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .heightIn(min = 32.dp)
                .padding(horizontal = 12.dp)
        )
        IconButton(
            onClick = { onCambiar(1) },
            enabled = enabled,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Aumentar", tint = Blue600)
        }
    }
}

private fun formatPrecio(value: Double): String =
    "$${String.format(Locale.US, "%.2f", value)}"
