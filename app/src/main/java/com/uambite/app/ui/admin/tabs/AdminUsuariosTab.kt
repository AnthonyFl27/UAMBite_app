package com.uambite.app.ui.admin.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uambite.app.data.api.UsuarioResponse
import com.uambite.app.ui.admin.AdminCard
import com.uambite.app.ui.admin.AdminFormDialog
import com.uambite.app.ui.admin.AdminFormField
import com.uambite.app.ui.admin.AdminTabScaffold
import com.uambite.app.ui.admin.AdminViewModel
import com.uambite.app.ui.theme.Blue100
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Blue700
import com.uambite.app.ui.theme.Gray500
import com.uambite.app.ui.theme.Green100
import com.uambite.app.ui.theme.Green700
import com.uambite.app.ui.theme.Purple100
import com.uambite.app.ui.theme.Purple700
import com.uambite.app.ui.theme.Red100
import com.uambite.app.ui.theme.Red700

@Composable
fun AdminUsuariosTab(viewModel: AdminViewModel) {
    val usuarios by viewModel.usuarios.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val isLoading = "usuarios" in loading
    var mostrarForm by remember { mutableStateOf(false) }

    AdminTabScaffold(
        items = usuarios,
        loading = isLoading,
        textoVacio = "No hay usuarios",
        textoNuevo = "Nuevo Usuario",
        onNuevo = { mostrarForm = true },
        key = { it.id }
    ) { u ->
        AdminCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Blue100)
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Blue600,
                        modifier = Modifier.padding(2.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${u.nombre} ${u.apellido}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${u.carnet} · ${u.correo ?: "—"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                    RolChip(u.rol)
                }
            }
        }
    }

    if (mostrarForm) {
        AdminFormDialog(
            titulo = "Nuevo Usuario",
            campos = listOf(
                AdminFormField.Text("carnet", "Carnet"),
                AdminFormField.Text("nombre", "Nombre"),
                AdminFormField.Text("apellido", "Apellido"),
                AdminFormField.Text("correo", "Correo", required = false),
                AdminFormField.Select(
                    "rol", "Rol",
                    listOf(
                        "ESTUDIANTE" to "Estudiante",
                        "PROFESOR" to "Profesor",
                        "LOCAL" to "Local",
                        "ADMIN" to "Admin"
                    ),
                    initial = "ESTUDIANTE"
                ),
                AdminFormField.Text("password", "Contraseña", isPassword = true)
            ),
            textoConfirmar = "Crear",
            onCancelar = { mostrarForm = false },
            onConfirmar = { valores, _ ->
                viewModel.crearUsuario(
                    carnet = valores["carnet"].orEmpty(),
                    nombre = valores["nombre"].orEmpty(),
                    apellido = valores["apellido"].orEmpty(),
                    correo = valores["correo"]?.takeIf { it.isNotBlank() },
                    rol = valores["rol"].orEmpty(),
                    password = valores["password"].orEmpty()
                )
                mostrarForm = false
            }
        )
    }
}

@Composable
fun RolChip(rol: String) {
    val (bg, fg) = when (rol) {
        "ADMIN" -> Red100 to Red700
        "LOCAL" -> Blue100 to Blue700
        "ESTUDIANTE" -> Green100 to Green700
        "PROFESOR" -> Purple100 to Purple700
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = rol,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.SemiBold
        )
    }
}
