package com.uambite.app.ui.localadmin.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.uambite.app.domain.model.Descuento
import com.uambite.app.ui.admin.AdminCard
import com.uambite.app.ui.admin.AdminFormDialog
import com.uambite.app.ui.admin.AdminFormField
import com.uambite.app.ui.admin.AdminTabScaffold
import com.uambite.app.ui.localadmin.LocalAdminViewModel
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Gray500
import com.uambite.app.ui.theme.Green700
import com.uambite.app.ui.theme.Red700

@Composable
fun LocalAdminDescuentosTab(viewModel: LocalAdminViewModel) {
    val descuentos by viewModel.descuentos.collectAsState()
    val locales by viewModel.misLocales.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val isLoading = "descuentos" in loading

    var modo by remember { mutableStateOf<ModoFormDescuento?>(null) }
    val localesOptions = locales.map { it.id to it.nombre }

    AdminTabScaffold(
        items = descuentos,
        loading = isLoading,
        textoVacio = "No hay descuentos",
        textoNuevo = "Nuevo Descuento",
        onNuevo = { modo = ModoFormDescuento(null) },
        key = { it.id }
    ) { d ->
        AdminCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = d.codigo,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "  ${d.porcentaje.toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Green700,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "Vence: ${d.fechaVencimiento ?: "—"} · ${if (d.activo) "Activo" else "Inactivo"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (d.activo) Green700 else Red700
                    )
                    Text(
                        text = d.localComida ?: "—",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
                Row {
                    IconButton(onClick = { modo = ModoFormDescuento(d) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Blue600)
                    }
                    IconButton(onClick = { viewModel.eliminarDescuento(d.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    val m = modo
    if (m != null) {
        AdminFormDialog(
            titulo = if (m.existente == null) "Nuevo Descuento" else "Editar Descuento",
            campos = buildList {
                add(AdminFormField.Text("codigo", "Código", m.existente?.codigo ?: ""))
                add(AdminFormField.Text("porcentaje", "Porcentaje", m.existente?.porcentaje?.toString() ?: "", isNumber = true))
                add(AdminFormField.Text("fechaVencimiento", "Fecha vencimiento (YYYY-MM-DD)", m.existente?.fechaVencimiento ?: "", required = false))

                if (localesOptions.size > 1) {
                    add(
                        AdminFormField.Select(
                            "localComidaId", "Local",
                            localesOptions,
                            initial = m.existente?.localComidaId ?: localesOptions.first().first
                        )
                    )
                } else if (localesOptions.isEmpty()) {
                    add(AdminFormField.Text("aviso", "Sin locales asignados", required = false))
                }

                add(AdminFormField.Bool("activo", "Activo", m.existente?.activo ?: true))
            },
            textoConfirmar = if (m.existente == null) "Crear" else "Guardar",
            onCancelar = { modo = null },
            onConfirmar = { valores, checks ->
                val localId = valores["localComidaId"]?.takeIf { it.isNotBlank() }
                    ?: m.existente?.localComidaId
                    ?: localesOptions.firstOrNull()?.first
                val porcentaje = valores["porcentaje"]?.toDoubleOrNull() ?: 0.0
                if (m.existente == null) {
                    viewModel.crearDescuento(
                        codigo = valores["codigo"].orEmpty(),
                        porcentaje = porcentaje,
                        fechaVencimiento = valores["fechaVencimiento"]?.takeIf { it.isNotBlank() },
                        activo = checks["activo"] == true,
                        localComidaId = localId
                    )
                } else {
                    viewModel.actualizarDescuento(
                        id = m.existente.id,
                        codigo = valores["codigo"].orEmpty(),
                        porcentaje = porcentaje,
                        fechaVencimiento = valores["fechaVencimiento"]?.takeIf { it.isNotBlank() },
                        activo = checks["activo"] == true,
                        localComidaId = localId
                    )
                }
                modo = null
            }
        )
    }
}

private data class ModoFormDescuento(val existente: Descuento?)
