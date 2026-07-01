package com.uambite.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions

sealed class AdminFormField {
    data class Text(
        val key: String,
        val label: String,
        val initial: String = "",
        val required: Boolean = true,
        val isPassword: Boolean = false,
        val isNumber: Boolean = false
    ) : AdminFormField()

    data class Select(
        val key: String,
        val label: String,
        val options: List<Pair<String, String>>,
        val initial: String,
        val required: Boolean = true
    ) : AdminFormField()

    data class Bool(
        val key: String,
        val label: String,
        val initial: Boolean = false
    ) : AdminFormField()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFormDialog(
    titulo: String,
    campos: List<AdminFormField>,
    textoConfirmar: String = "Guardar",
    procesando: Boolean = false,
    onCancelar: () -> Unit,
    onConfirmar: (valores: Map<String, String>, checks: Map<String, Boolean>) -> Unit
) {
    val valores = remember { mutableMapOf<String, String>() }
    val checks = remember { mutableMapOf<String, Boolean>() }
    campos.forEach { campo ->
        when (campo) {
            is AdminFormField.Text -> if (campo.key !in valores) valores[campo.key] = campo.initial
            is AdminFormField.Select -> if (campo.key !in valores) valores[campo.key] = campo.initial
            is AdminFormField.Bool -> if (campo.key !in checks) checks[campo.key] = campo.initial
        }
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                campos.forEach { campo ->
                    when (campo) {
                        is AdminFormField.Text -> {
                            OutlinedTextField(
                                value = valores[campo.key] ?: "",
                                onValueChange = { valores[campo.key] = it },
                                label = { Text(campo.label + if (campo.required) " *" else "") },
                                singleLine = true,
                                visualTransformation = if (campo.isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                                keyboardOptions = if (campo.isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is AdminFormField.Select -> {
                            var expanded by remember { mutableStateOf(false) }
                            val current = valores[campo.key] ?: campo.initial
                            val currentLabel = campo.options.firstOrNull { it.first == current }?.second ?: current
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = currentLabel,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(campo.label + if (campo.required) " *" else "") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    campo.options.forEach { (key, text) ->
                                        DropdownMenuItem(
                                            text = { Text(text) },
                                            onClick = {
                                                valores[campo.key] = key
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        is AdminFormField.Bool -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = checks[campo.key] ?: false,
                                    onCheckedChange = { checks[campo.key] = it }
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(campo.label)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(valores.toMap(), checks.toMap()) },
                enabled = !procesando
            ) {
                if (procesando) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(textoConfirmar)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
