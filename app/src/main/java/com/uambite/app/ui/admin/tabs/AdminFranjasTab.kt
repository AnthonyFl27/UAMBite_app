package com.uambite.app.ui.admin.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uambite.app.domain.model.FranjaHoraria
import com.uambite.app.domain.model.Local
import com.uambite.app.ui.admin.AdminCard
import com.uambite.app.ui.admin.AdminFormDialog
import com.uambite.app.ui.admin.AdminFormField
import com.uambite.app.ui.admin.AdminTabScaffold
import com.uambite.app.ui.admin.AdminViewModel
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Gray500
import com.uambite.app.ui.theme.Green700
import com.uambite.app.ui.theme.Red700

@Composable
fun AdminFranjasTab(viewModel: AdminViewModel) {
    val franjas by viewModel.franjas.collectAsState()
    val locales by viewModel.locales.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val isLoading = "franjas" in loading

    var modo by remember { mutableStateOf<ModoFormFranja?>(null) }
    val localesOptions = locales.map { it.id to it.nombre }

    AdminTabScaffold(
        items = franjas,
        loading = isLoading,
        textoVacio = "No hay franjas",
        textoNuevo = "Nueva Franja",
        onNuevo = { modo = ModoFormFranja(null) },
        key = { it.id }
    ) { franja ->
        AdminCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${franja.horaInicio} - ${franja.horaFin}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${franja.localComida ?: "Global"} · ${franja.pedidosActuales}/${franja.capacidadMaxima}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (franja.disponible) Green700 else Red700
                    )
                }
                Row {
                    IconButton(onClick = { modo = ModoFormFranja(franja) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Blue600)
                    }
                    IconButton(onClick = { viewModel.eliminarFranja(franja.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    val m = modo
    if (m != null) {
        AdminFormDialog(
            titulo = if (m.existente == null) "Nueva Franja" else "Editar Franja",
            campos = listOf(
                if (localesOptions.isEmpty()) {
                    AdminFormField.Text("aviso", "Sin locales", required = false)
                } else {
                    AdminFormField.Select(
                        "localComidaId", "Local",
                        localesOptions,
                        initial = m.existente?.localComidaId ?: localesOptions.first().first
                    )
                },
                AdminFormField.Text("horaInicio", "Hora inicio (HH:mm)", m.existente?.horaInicio ?: ""),
                AdminFormField.Text("horaFin", "Hora fin (HH:mm)", m.existente?.horaFin ?: ""),
                AdminFormField.Text(
                    "capacidadMaxima", "Capacidad máxima",
                    m.existente?.capacidadMaxima?.toString() ?: "",
                    isNumber = true
                ),
                AdminFormField.Bool(
                    "disponible", "Disponible",
                    m.existente?.disponible ?: true
                )
            ),
            textoConfirmar = if (m.existente == null) "Crear" else "Guardar",
            onCancelar = { modo = null },
            onConfirmar = { valores, checks ->
                val localId = valores["localComidaId"].orEmpty()
                if (localId.isNotEmpty()) {
                    val cap = valores["capacidadMaxima"]?.toIntOrNull() ?: 0
                    if (m.existente == null) {
                        viewModel.crearFranja(
                            horaInicio = valores["horaInicio"].orEmpty(),
                            horaFin = valores["horaFin"].orEmpty(),
                            capacidadMaxima = cap,
                            localComidaId = localId
                        )
                    } else {
                        viewModel.actualizarFranja(
                            id = m.existente.id,
                            horaInicio = valores["horaInicio"].orEmpty(),
                            horaFin = valores["horaFin"].orEmpty(),
                            capacidadMaxima = cap,
                            disponible = checks["disponible"] == true,
                            localComidaId = localId
                        )
                    }
                }
                modo = null
            }
        )
    }
}

private data class ModoFormFranja(val existente: FranjaHoraria?)
