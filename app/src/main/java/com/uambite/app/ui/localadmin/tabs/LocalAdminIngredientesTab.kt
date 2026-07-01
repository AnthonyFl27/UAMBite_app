package com.uambite.app.ui.localadmin.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.uambite.app.data.api.ImageUrlBuilder
import com.uambite.app.domain.model.IngredienteExtra
import com.uambite.app.ui.admin.AdminCard
import com.uambite.app.ui.admin.AdminFormDialog
import com.uambite.app.ui.admin.AdminFormField
import com.uambite.app.ui.admin.AdminTabScaffold
import com.uambite.app.ui.common.NetworkImage
import com.uambite.app.ui.imagenes.ImagenUploadViewModel
import com.uambite.app.ui.imagenes.rememberImagenPicker
import com.uambite.app.ui.localadmin.LocalAdminViewModel
import com.uambite.app.ui.theme.Blue100
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Blue700
import java.util.Locale

@Composable
fun LocalAdminIngredientesTab(
    viewModel: LocalAdminViewModel,
    imagenVm: ImagenUploadViewModel
) {
    val ingredientes by viewModel.ingredientes.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val isLoading = "ingredientes" in loading

    var modo by remember { mutableStateOf<ModoFormIngrediente?>(null) }

    AdminTabScaffold(
        items = ingredientes,
        loading = isLoading,
        textoVacio = "No hay ingredientes",
        textoNuevo = "Nuevo Ingrediente",
        onNuevo = { modo = ModoFormIngrediente(null) },
        key = { it.id }
    ) { ing ->
        AdminCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (ing.tieneImagen) {
                    NetworkImage(
                        url = ImageUrlBuilder.ingrediente(ing.id),
                        contentDescription = ing.nombre,
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
                            .background(Blue100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Blue600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = ing.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "+$${String.format(java.util.Locale.US, "%.2f", ing.precioExtra)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Blue700,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row {
                    IconButton(onClick = { modo = ModoFormIngrediente(ing) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Blue600)
                    }
                    IngredienteImagenRowActions(
                        ingrediente = ing,
                        imagenVm = imagenVm
                    )
                    IconButton(onClick = { viewModel.eliminarIngrediente(ing.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    val m = modo
    if (m != null) {
        AdminFormDialog(
            titulo = if (m.existente == null) "Nuevo Ingrediente" else "Editar Ingrediente",
            campos = listOf(
                AdminFormField.Text("nombre", "Nombre", m.existente?.nombre ?: ""),
                AdminFormField.Text("precioExtra", "Precio extra", m.existente?.precioExtra?.toString() ?: "", isNumber = true)
            ),
            textoConfirmar = if (m.existente == null) "Crear" else "Guardar",
            onCancelar = { modo = null },
            onConfirmar = { valores, _ ->
                val precio = valores["precioExtra"]?.toDoubleOrNull() ?: 0.0
                if (m.existente == null) {
                    viewModel.crearIngrediente(
                        nombre = valores["nombre"].orEmpty(),
                        precioExtra = precio
                    )
                } else {
                    viewModel.actualizarIngrediente(
                        id = m.existente.id,
                        nombre = valores["nombre"].orEmpty(),
                        precioExtra = precio
                    )
                }
                modo = null
            }
        )
    }
}

private data class ModoFormIngrediente(val existente: IngredienteExtra?)

@Composable
private fun IngredienteImagenRowActions(
    ingrediente: IngredienteExtra,
    imagenVm: ImagenUploadViewModel
) {
    val pickImagen = rememberImagenPicker(imagenVm, "ingrediente", ingrediente.id)
    Row {
        IconButton(onClick = pickImagen) {
            Icon(Icons.Default.CameraAlt, contentDescription = "Imagen", tint = Blue600)
        }
        if (ingrediente.tieneImagen) {
            IconButton(onClick = { imagenVm.eliminar("ingrediente", ingrediente.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Quitar imagen",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
