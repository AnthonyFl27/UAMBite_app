package com.uambite.app.ui.admin.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uambite.app.data.api.ImageUrlBuilder
import com.uambite.app.domain.model.IngredienteExtra
import com.uambite.app.domain.model.Local
import com.uambite.app.domain.model.Producto
import com.uambite.app.ui.admin.AdminCard
import com.uambite.app.ui.admin.AdminFormDialog
import com.uambite.app.ui.admin.AdminFormField
import com.uambite.app.ui.admin.AdminTabScaffold
import com.uambite.app.ui.admin.AdminViewModel
import com.uambite.app.ui.common.NetworkImage
import com.uambite.app.ui.imagenes.ImagenUploadViewModel
import com.uambite.app.ui.imagenes.rememberImagenPicker
import com.uambite.app.ui.theme.Blue100
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Gray500
import com.uambite.app.ui.theme.Purple500

@Composable
fun AdminProductosTab(
    viewModel: AdminViewModel,
    imagenVm: ImagenUploadViewModel
) {
    val productos by viewModel.productos.collectAsState()
    val locales by viewModel.locales.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val isLoading = "productos" in loading

    var modo by remember { mutableStateOf<ModoFormProducto?>(null) }

    val localesOptions = locales.map { it.id to it.nombre }

    AdminTabScaffold(
        items = productos,
        loading = isLoading,
        textoVacio = "No hay productos",
        textoNuevo = "Nuevo Producto",
        onNuevo = { modo = ModoFormProducto(null) },
        key = { it.id }
    ) { producto ->
        AdminCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (producto.tieneImagen) {
                    NetworkImage(
                        url = ImageUrlBuilder.producto(producto.id),
                        contentDescription = producto.nombre,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Blue100)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = producto.nombre,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        if (producto.permitePersonalizacion) {
                            Text("✎", color = Purple500)
                        }
                    }
                    Text(
                        text = "${formatPrecio(producto.precio)} · Stock: ${producto.stock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Blue600,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = producto.localComida,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500,
                        maxLines = 1
                    )
                }
                Row {
                    IconButton(onClick = { modo = ModoFormProducto(producto) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Blue600)
                    }
                    ProductoImagenActions(
                        producto = producto,
                        imagenVm = imagenVm
                    )
                    IconButton(onClick = { viewModel.eliminarProducto(producto.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    val m = modo
    if (m != null) {
        AdminFormDialog(
            titulo = if (m.existente == null) "Nuevo Producto" else "Editar Producto",
            campos = listOf(
                if (localesOptions.isEmpty()) {
                    AdminFormField.Text("aviso", "Sin locales. Crea uno primero", required = false)
                } else {
                    AdminFormField.Select(
                        "localComidaId", "Local",
                        localesOptions,
                        initial = m.existente?.localComidaId ?: localesOptions.first().first
                    )
                },
                AdminFormField.Text(
                    "nombre", "Nombre",
                    m.existente?.nombre ?: ""
                ),
                AdminFormField.Text(
                    "descripcion", "Descripción",
                    m.existente?.descripcion ?: "",
                    required = false
                ),
                AdminFormField.Text(
                    "precio", "Precio",
                    m.existente?.precio?.toString() ?: "",
                    isNumber = true
                ),
                AdminFormField.Text(
                    "stock", "Stock",
                    m.existente?.stock?.toString() ?: "",
                    isNumber = true
                ),
                AdminFormField.Bool(
                    "permitePersonalizacion", "Permite personalización",
                    m.existente?.permitePersonalizacion ?: false
                )
            ),
            textoConfirmar = if (m.existente == null) "Crear" else "Guardar",
            onCancelar = { modo = null },
            onConfirmar = { valores, checks ->
                val localId = valores["localComidaId"].orEmpty()
                if (localId.isNotEmpty()) {
                    val precio = valores["precio"]?.toDoubleOrNull() ?: 0.0
                    val stock = valores["stock"]?.toIntOrNull() ?: 0
                    val existente = m.existente
                    if (existente == null) {
                        viewModel.crearProducto(
                            nombre = valores["nombre"].orEmpty(),
                            descripcion = valores["descripcion"]?.takeIf { it.isNotBlank() },
                            precio = precio,
                            stock = stock,
                            permitePersonalizacion = checks["permitePersonalizacion"] == true,
                            localComidaId = localId
                        )
                    } else {
                        viewModel.actualizarProducto(
                            id = existente.id,
                            nombre = valores["nombre"].orEmpty(),
                            descripcion = valores["descripcion"]?.takeIf { it.isNotBlank() },
                            precio = precio,
                            stock = stock,
                            permitePersonalizacion = checks["permitePersonalizacion"] == true,
                            localComidaId = localId
                        )
                    }
                }
                modo = null
            }
        )
    }
}

private data class ModoFormProducto(val existente: Producto?)

@Composable
private fun ProductoImagenActions(
    producto: Producto,
    imagenVm: ImagenUploadViewModel
) {
    val pickImagen = rememberImagenPicker(imagenVm, "producto", producto.id)
    Row {
        IconButton(onClick = pickImagen) {
            Icon(Icons.Default.CameraAlt, contentDescription = "Imagen", tint = Blue600)
        }
        if (producto.tieneImagen) {
            IconButton(onClick = { imagenVm.eliminar("producto", producto.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Quitar imagen",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatPrecio(v: Double): String = "$${String.format(java.util.Locale.US, "%.2f", v)}"
