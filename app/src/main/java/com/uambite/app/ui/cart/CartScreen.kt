package com.uambite.app.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uambite.app.domain.model.CartItem
import com.uambite.app.domain.model.Descuento
import com.uambite.app.domain.model.FranjaHoraria
import com.uambite.app.ui.common.EmptyBox
import com.uambite.app.ui.common.ErrorBox
import com.uambite.app.ui.common.LoadingBox
import com.uambite.app.ui.franjas.FranjasViewModel
import com.uambite.app.ui.theme.Blue100
import com.uambite.app.ui.theme.Blue600
import com.uambite.app.ui.theme.Blue700
import com.uambite.app.ui.theme.Gray500
import com.uambite.app.ui.theme.Green100
import com.uambite.app.ui.theme.Green700
import com.uambite.app.ui.theme.Purple500
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onPedidoCreado: () -> Unit,
    cartViewModel: CartViewModel = hiltViewModel(),
    checkoutViewModel: CheckoutViewModel = hiltViewModel(),
    franjasViewModel: FranjasViewModel = hiltViewModel(),
    sessionViewModel: com.uambite.app.ui.nav.SessionViewModel = hiltViewModel()
) {
    val items by cartViewModel.items.collectAsState()
    val descuento by cartViewModel.descuento.collectAsState()
    val checkoutState by checkoutViewModel.state.collectAsState()
    val franjasState by franjasViewModel.uiState.collectAsState()
    val user by sessionViewModel.user.collectAsState()

    var codigoDescuento by remember { mutableStateOf("") }
    var codigoError by remember { mutableStateOf<String?>(null) }
    var franjaSeleccionadaId by remember { mutableStateOf<String?>(null) }
    var tipoEntrega by remember { mutableStateOf("RETIRO_LOCAL") }
    var metodoPago by remember { mutableStateOf("EFECTIVO") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(checkoutState) {
        when (val s = checkoutState) {
            is CheckoutViewModel.CheckoutState.Success -> {
                checkoutViewModel.reset()
                onPedidoCreado()
            }
            is CheckoutViewModel.CheckoutState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                checkoutViewModel.reset()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CartTopBar(
                itemCount = items.sumOf { it.cantidad },
                onBack = onBack,
                onVaciar = { cartViewModel.vaciar() }
            )

            if (items.isEmpty()) {
                Box(modifier = Modifier.weight(1f)) {
                    EmptyBox(message = "Carrito vacío")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.cartKey }) { item ->
                        CartItemRow(
                            item = item,
                            onCambiarCantidad = { delta -> cartViewModel.cambiarCantidad(item.cartKey, delta) },
                            onEliminar = { cartViewModel.eliminar(item.cartKey) }
                        )
                    }
                }

                CartFooter(
                    subtotal = cartViewModel.subtotal(),
                    descuentoAplicado = descuento,
                    montoDescuento = cartViewModel.montoDescuento(),
                    total = cartViewModel.total(),
                    codigo = codigoDescuento,
                    onCodigoChange = {
                        codigoDescuento = it
                        codigoError = null
                    },
                    codigoError = codigoError,
                    onAplicarDescuento = {
                        val codigo = codigoDescuento.trim()
                        if (codigo.isEmpty()) {
                            codigoError = "Ingresa un código"
                        } else {
                            franjasViewModel.buscarYAplicarDescuento(
                                codigo = codigo,
                                onAplicado = { d: Descuento ->
                                    codigoDescuento = d.codigo
                                    codigoError = null
                                    scope.launch { snackbarHostState.showSnackbar("${d.porcentaje.toInt()}% de descuento aplicado") }
                                },
                                onError = { msg -> codigoError = msg }
                            )
                        }
                    },
                    onQuitarDescuento = {
                        cartViewModel.quitarDescuento()
                        codigoDescuento = ""
                    },
                    franjas = (franjasState as? FranjasViewModel.UiState.Success)?.franjas ?: emptyList(),
                    franjaSeleccionadaId = franjaSeleccionadaId,
                    onFranjaChange = { franjaSeleccionadaId = it },
                    franjasLoading = franjasState is FranjasViewModel.UiState.Loading,
                    franjasError = (franjasState as? FranjasViewModel.UiState.Error)?.message,
                    onRecargarFranjas = { franjasViewModel.load() },
                    tipoEntrega = tipoEntrega,
                    onTipoEntregaChange = { tipoEntrega = it },
                    metodoPago = metodoPago,
                    onMetodoPagoChange = { metodoPago = it },
                    procesando = checkoutState is CheckoutViewModel.CheckoutState.Loading,
                    onConfirmar = {
                        if (user == null) {
                            scope.launch { snackbarHostState.showSnackbar("No hay sesión activa") }
                            return@CartFooter
                        }
                        checkoutViewModel.confirmar(
                            usuarioId = user!!.id,
                            tipoEntrega = tipoEntrega,
                            metodoPago = metodoPago,
                            franjaHorariaId = franjaSeleccionadaId
                        )
                    }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CartTopBar(
    itemCount: Int,
    onBack: () -> Unit,
    onVaciar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = Blue600
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Carrito",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (itemCount > 0) {
                Text(
                    text = "$itemCount ${if (itemCount == 1) "ítem" else "ítems"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }
        if (itemCount > 0) {
            IconButton(onClick = onVaciar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Vaciar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onCambiarCantidad: (Int) -> Unit,
    onEliminar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.nombre,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${formatPrecio(item.precioUnitario)} c/u · ${item.localNombre}",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
            if (item.extrasNombres.isNotEmpty()) {
                Text(
                    text = "+ ${item.extrasNombres.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Purple500,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Subtotal: ${formatPrecio(item.subtotal)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Blue700,
                fontWeight = FontWeight.Medium
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onCambiarCantidad(-1) },
                enabled = item.cantidad > 1,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Disminuir", tint = Blue600)
            }
            Text(
                text = item.cantidad.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(
                onClick = { onCambiarCantidad(1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aumentar", tint = Blue600)
            }
            IconButton(
                onClick = onEliminar,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartFooter(
    subtotal: Double,
    descuentoAplicado: Descuento?,
    montoDescuento: Double,
    total: Double,
    codigo: String,
    onCodigoChange: (String) -> Unit,
    codigoError: String?,
    onAplicarDescuento: () -> Unit,
    onQuitarDescuento: () -> Unit,
    franjas: List<FranjaHoraria>,
    franjaSeleccionadaId: String?,
    onFranjaChange: (String?) -> Unit,
    franjasLoading: Boolean,
    franjasError: String?,
    onRecargarFranjas: () -> Unit,
    tipoEntrega: String,
    onTipoEntregaChange: (String) -> Unit,
    metodoPago: String,
    onMetodoPagoChange: (String) -> Unit,
    procesando: Boolean,
    onConfirmar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ResumenLinea("Subtotal", formatPrecio(subtotal))
        if (descuentoAplicado != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Descuento (${descuentoAplicado.porcentaje.toInt()}%)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Green700
                )
                Text(
                    text = "-${formatPrecio(montoDescuento)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Green700,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatPrecio(total),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Blue700
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = codigo,
                onValueChange = onCodigoChange,
                placeholder = { Text("Código descuento") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                isError = codigoError != null,
                supportingText = {
                    if (codigoError != null) {
                        Text(codigoError, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(modifier = Modifier.size(8.dp))
            if (descuentoAplicado != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Green100)
                        .clickable { onQuitarDescuento() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Tag,
                            contentDescription = null,
                            tint = Green700,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "${descuentoAplicado.porcentaje.toInt()}%",
                            color = Green700,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Green100)
                        .clickable { onAplicarDescuento() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Aplicar",
                        color = Green700,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        FranjaSelector(
            franjas = franjas,
            seleccionada = franjaSeleccionadaId,
            onChange = onFranjaChange,
            loading = franjasLoading,
            error = franjasError,
            onRecargar = onRecargarFranjas
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectField(
                label = "Tipo Entrega",
                value = tipoEntrega,
                options = listOf(
                    "RETIRO_LOCAL" to "Retiro en Local",
                    "ENTREGA_INTERNA" to "Entrega Interna"
                ),
                onValueChange = onTipoEntregaChange,
                modifier = Modifier.weight(1f)
            )
            SelectField(
                label = "Método Pago",
                value = metodoPago,
                options = listOf(
                    "EFECTIVO" to "Efectivo",
                    "TARJETA" to "Tarjeta",
                    "TRANSFERENCIA" to "Transferencia"
                ),
                onValueChange = onMetodoPagoChange,
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = onConfirmar,
            enabled = !procesando,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (procesando) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "Confirmar Pedido",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ResumenLinea(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Gray500)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FranjaSelector(
    franjas: List<FranjaHoraria>,
    seleccionada: String?,
    onChange: (String?) -> Unit,
    loading: Boolean,
    error: String?,
    onRecargar: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = franjas.firstOrNull { it.id == seleccionada }?.descripcionCompleta
        ?: "Sin franja — pedir ya"

    Column {
        if (loading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Cargando franjas...", style = MaterialTheme.typography.bodySmall, color = Gray500)
            }
        } else if (error != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Reintentar",
                    color = Blue600,
                    modifier = Modifier.clickable { onRecargar() }
                )
            }
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Franja Horaria (opcional)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Sin franja — pedir ya") },
                    onClick = {
                        onChange(null)
                        expanded = false
                    }
                )
                franjas.forEach { f ->
                    DropdownMenuItem(
                        text = { Text(f.descripcionCompleta) },
                        onClick = {
                            onChange(f.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectField(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = options.firstOrNull { it.first == value }?.second ?: value
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = currentLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, style = MaterialTheme.typography.bodySmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (key, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onValueChange(key)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatPrecio(value: Double): String =
    "$${String.format(Locale.US, "%.2f", value)}"
