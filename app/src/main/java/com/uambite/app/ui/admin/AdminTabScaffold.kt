package com.uambite.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uambite.app.ui.common.EmptyBox
import com.uambite.app.ui.common.ErrorBox
import com.uambite.app.ui.common.LoadingBox
import com.uambite.app.ui.theme.Blue600

@Composable
fun <T> AdminTabScaffold(
    items: List<T>,
    loading: Boolean,
    error: String? = null,
    onRetry: () -> Unit = {},
    textoVacio: String = "No hay elementos",
    textoNuevo: String = "Nuevo",
    onNuevo: () -> Unit,
    key: (T) -> Any,
    itemContent: @Composable (T) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            loading && items.isEmpty() -> LoadingBox()
            error != null && items.isEmpty() -> ErrorBox(message = error, onRetry = onRetry)
            items.isEmpty() -> EmptyBox(message = textoVacio)
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = key) { item ->
                        itemContent(item)
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onNuevo,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text(textoNuevo) },
            containerColor = Blue600,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(4.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
fun AdminCard(
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        content()
    }
}

@Composable
fun ConfirmarAccion(
    procesando: Boolean,
    texto: String = "Confirmar",
    onConfirmar: () -> Unit
) {
    Button(
        onClick = onConfirmar,
        enabled = !procesando,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.height(36.dp)
    ) {
        if (procesando) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Text(texto, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun AccionBoton(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.height(36.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
    ) {
        Text(texto, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun AccionIcono(
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.error,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        content()
    }
}

@Composable
fun SpacerH(dp: Int) {
    Spacer(modifier = Modifier.size(dp.dp))
}
