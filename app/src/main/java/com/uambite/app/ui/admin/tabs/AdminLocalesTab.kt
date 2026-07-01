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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Store
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
import com.uambite.app.domain.model.Local
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

@Composable
fun AdminLocalesTab(
    viewModel: AdminViewModel,
    imagenVm: ImagenUploadViewModel
) {
    val locales by viewModel.locales.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val isLoading = "locales" in loading

    var modo by remember { mutableStateOf<ModoLocalForm>(ModoLocalForm.Ninguno) }

    AdminTabScaffold(
        items = locales,
        loading = isLoading,
        textoVacio = "No hay locales",
        textoNuevo = "Nuevo Local",
        onNuevo = { modo = ModoLocalForm.Crear(null) },
        key = { it.id }
    ) { local ->
        AdminCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (local.tieneImagen) {
                    NetworkImage(
                        url = ImageUrlBuilder.local(local.id),
                        contentDescription = local.nombre,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Blue100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = Blue600)
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = local.nombre,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = "${local.ubicacion} · ${local.horario ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500,
                        maxLines = 1
                    )
                    Text(
                        text = "Dueño: ${local.duenoId?.take(8) ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500,
                        maxLines = 1
                    )
                }
                LocalRowActions(
                    local = local,
                    imagenVm = imagenVm,
                    onEditar = { modo = ModoLocalForm.Editar(local) },
                    onAsignar = { modo = ModoLocalForm.AsignarEncargado(local) },
                    onEliminar = { viewModel.eliminarLocal(local.id) }
                )
            }
        }
    }

    when (val m = modo) {
        is ModoLocalForm.Crear -> { /* ver abajo */ }
        is ModoLocalForm.Editar -> { /* ver abajo */ }
        is ModoLocalForm.AsignarEncargado -> { /* ver abajo */ }
        ModoLocalForm.Ninguno -> Unit
    }
    when (val m = modo) {
        is ModoLocalForm.Crear -> {
            AdminFormDialog(
                titulo = if (m.existente == null) "Nuevo Local + Encargado" else "Editar Local",
                campos = listOf(
                    AdminFormField.Text("nombre", "Nombre", m.existente?.nombre ?: ""),
                    AdminFormField.Text("ubicacion", "Ubicación", m.existente?.ubicacion ?: ""),
                    AdminFormField.Text("horario", "Horario", m.existente?.horario ?: "", required = false)
                ) + if (m.existente == null) listOf(
                    AdminFormField.Text("encCarnet", "Carnet encargado"),
                    AdminFormField.Text("encNombre", "Nombre encargado"),
                    AdminFormField.Text("encApellido", "Apellido encargado"),
                    AdminFormField.Text("encCorreo", "Correo encargado", required = false),
                    AdminFormField.Text("encPassword", "Contraseña encargado", isPassword = true)
                ) else emptyList(),
                textoConfirmar = if (m.existente == null) "Crear" else "Guardar",
                onCancelar = { modo = ModoLocalForm.Ninguno },
                onConfirmar = { valores, _ ->
                    if (m.existente == null) {
                        viewModel.crearLocal(
                            nombre = valores["nombre"].orEmpty(),
                            ubicacion = valores["ubicacion"].orEmpty(),
                            horario = valores["horario"]?.takeIf { it.isNotBlank() },
                            encCarnet = valores["encCarnet"].orEmpty(),
                            encNombre = valores["encNombre"].orEmpty(),
                            encApellido = valores["encApellido"].orEmpty(),
                            encCorreo = valores["encCorreo"]?.takeIf { it.isNotBlank() },
                            encPassword = valores["encPassword"].orEmpty()
                        )
                    } else {
                        viewModel.actualizarLocal(
                            id = m.existente.id,
                            nombre = valores["nombre"].orEmpty(),
                            ubicacion = valores["ubicacion"].orEmpty(),
                            horario = valores["horario"]?.takeIf { it.isNotBlank() }
                        )
                    }
                    modo = ModoLocalForm.Ninguno
                }
            )
        }
        is ModoLocalForm.Editar -> {
            AdminFormDialog(
                titulo = "Editar Local",
                campos = listOf(
                    AdminFormField.Text("nombre", "Nombre", m.local.nombre),
                    AdminFormField.Text("ubicacion", "Ubicación", m.local.ubicacion),
                    AdminFormField.Text("horario", "Horario", m.local.horario ?: "", required = false)
                ),
                textoConfirmar = "Guardar",
                onCancelar = { modo = ModoLocalForm.Ninguno },
                onConfirmar = { valores, _ ->
                    viewModel.actualizarLocal(
                        id = m.local.id,
                        nombre = valores["nombre"].orEmpty(),
                        ubicacion = valores["ubicacion"].orEmpty(),
                        horario = valores["horario"]?.takeIf { it.isNotBlank() }
                    )
                    modo = ModoLocalForm.Ninguno
                }
            )
        }
        is ModoLocalForm.AsignarEncargado -> {
            AdminFormDialog(
                titulo = "Asignar Encargado",
                campos = listOf(
                    AdminFormField.Text("carnet", "Carnet"),
                    AdminFormField.Text("nombre", "Nombre"),
                    AdminFormField.Text("apellido", "Apellido"),
                    AdminFormField.Text("correo", "Correo", required = false),
                    AdminFormField.Text("password", "Contraseña", isPassword = true)
                ),
                textoConfirmar = "Asignar",
                onCancelar = { modo = ModoLocalForm.Ninguno },
                onConfirmar = { valores, _ ->
                    viewModel.asignarEncargado(
                        localId = m.local.id,
                        carnet = valores["carnet"].orEmpty(),
                        nombre = valores["nombre"].orEmpty(),
                        apellido = valores["apellido"].orEmpty(),
                        correo = valores["correo"]?.takeIf { it.isNotBlank() },
                        password = valores["password"].orEmpty()
                    )
                    modo = ModoLocalForm.Ninguno
                }
            )
        }
        ModoLocalForm.Ninguno -> Unit
    }
}

private sealed class ModoLocalForm {
    data object Ninguno : ModoLocalForm()
    data class Crear(val existente: Local?) : ModoLocalForm()
    data class Editar(val local: Local) : ModoLocalForm()
    data class AsignarEncargado(val local: Local) : ModoLocalForm()
}

@Composable
private fun LocalRowActions(
    local: Local,
    imagenVm: ImagenUploadViewModel,
    onEditar: () -> Unit,
    onAsignar: () -> Unit,
    onEliminar: () -> Unit
) {
    val pickImagen = rememberImagenPicker(imagenVm, "local", local.id)
    Row {
        IconButton(onClick = onEditar) {
            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Blue600)
        }
        IconButton(onClick = onAsignar) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Asignar", tint = Blue600)
        }
        IconButton(onClick = pickImagen) {
            Icon(Icons.Default.CameraAlt, contentDescription = "Imagen", tint = Blue600)
        }
        if (local.tieneImagen) {
            IconButton(onClick = { imagenVm.eliminar("local", local.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Quitar imagen",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        IconButton(onClick = onEliminar) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar local",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
