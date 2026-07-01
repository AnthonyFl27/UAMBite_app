package com.uambite.app.ui.localadmin.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.uambite.app.ui.admin.AdminFormDialog
import com.uambite.app.ui.admin.AdminFormField
import com.uambite.app.ui.common.EmptyBox
import com.uambite.app.ui.common.NetworkImage
import com.uambite.app.ui.imagenes.ImagenUploadViewModel
import com.uambite.app.ui.imagenes.rememberImagenPicker
import com.uambite.app.ui.localadmin.LocalAdminViewModel
import com.uambite.app.ui.theme.Blue100
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Gray500

@Composable
fun LocalAdminTab(
    viewModel: LocalAdminViewModel,
    imagenVm: ImagenUploadViewModel
) {
    val locales by viewModel.misLocales.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val isLoading = "local" in loading

    var localEditando by remember { mutableStateOf<Local?>(null) }
    var localParaImagen by remember { mutableStateOf<Local?>(null) }

    if (locales.isEmpty()) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else {
            EmptyBox(message = "No tienes locales asignados")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(locales, key = { it.id }) { local ->
            LocalCard(
                local = local,
                imagenVm = imagenVm,
                onEditar = { localEditando = it }
            )
        }
    }

    localEditando?.let { local ->
        AdminFormDialog(
            titulo = "Editar Local",
            campos = listOf(
                AdminFormField.Text("nombre", "Nombre", local.nombre),
                AdminFormField.Text("ubicacion", "Ubicación", local.ubicacion),
                AdminFormField.Text("horario", "Horario", local.horario ?: "", required = false)
            ),
            textoConfirmar = "Guardar",
            onCancelar = { localEditando = null },
            onConfirmar = { valores, _ ->
                viewModel.actualizarLocal(
                    id = local.id,
                    nombre = valores["nombre"].orEmpty(),
                    ubicacion = valores["ubicacion"].orEmpty(),
                    horario = valores["horario"]?.takeIf { it.isNotBlank() }
                )
                localEditando = null
            }
        )
    }
}

@Composable
private fun LocalCard(
    local: Local,
    imagenVm: ImagenUploadViewModel,
    onEditar: (Local) -> Unit
) {
    val uploadState by imagenVm.state.collectAsState()
    val pickImagen = rememberImagenPicker(imagenVm, "local", local.id)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(192.dp)) {
            if (local.tieneImagen) {
                NetworkImage(
                    url = ImageUrlBuilder.local(local.id),
                    contentDescription = local.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Blue100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = Blue600.copy(alpha = 0.5f),
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            if (uploadState.subiendo) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Button(
                    onClick = pickImagen,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = if (local.tieneImagen) Icons.Default.CameraAlt else Icons.Default.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))
                    Text(
                        text = if (local.tieneImagen) "Cambiar" else "Subir",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                if (local.tieneImagen) {
                    androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))
                    Button(
                        onClick = { imagenVm.eliminar("local", local.id) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))
                        Text("Quitar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = local.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Gray500,
                        modifier = Modifier.size(14.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.size(2.dp))
                    Text(
                        text = local.ubicacion.ifBlank { "Sin ubicación" },
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
                androidx.compose.foundation.layout.Spacer(Modifier.size(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Gray500,
                        modifier = Modifier.size(14.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.size(2.dp))
                    Text(
                        text = local.horario?.ifBlank { "Sin horario" } ?: "Sin horario",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
            }
            IconButton(onClick = { onEditar(local) }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = Blue600
                )
            }
        }
    }
}
