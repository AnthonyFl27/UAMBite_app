package com.uambite.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.uambite.app.domain.model.CartItem
import com.uambite.app.domain.model.Descuento
import com.uambite.app.domain.repository.CartRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.cartDataStore: DataStore<Preferences> by preferencesDataStore(name = "uambite_cart")

@Singleton
class CartRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CartRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    override val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    private val _descuento = MutableStateFlow<Descuento?>(null)
    override val descuento: StateFlow<Descuento?> = _descuento.asStateFlow()

    @Volatile
    private var currentUserId: String? = null

    init {
        scope.launch { cargarDesdeDisco() }
    }

    override fun setUserId(userId: String?) {
        currentUserId = userId
        scope.launch { cargarDesdeDisco() }
    }

    override fun agregar(
        productoId: String,
        nombre: String,
        precioBase: Double,
        cantidad: Int,
        localNombre: String,
        localComidaId: String?,
        extrasIds: List<String>,
        extrasPrecios: Map<String, Double>,
        extrasNombres: List<String>
    ) {
        val sortedExtras = extrasIds.sorted()
        val key = productoId + "__" + sortedExtras.joinToString(",")
        val extrasTotal = sortedExtras.sumOf { extrasPrecios[it] ?: 0.0 }
        val precioUnitario = precioBase + extrasTotal

        val current = _items.value
        val existente = current.find { it.cartKey == key }
        _items.value = if (existente != null) {
            current.map {
                if (it.cartKey == key) it.copy(cantidad = it.cantidad + cantidad) else it
            }
        } else {
            current + CartItem(
                cartKey = key,
                productoId = productoId,
                nombre = nombre,
                precioUnitario = precioUnitario,
                cantidad = cantidad,
                localNombre = localNombre,
                localComidaId = localComidaId,
                extrasIds = sortedExtras,
                extrasNombres = extrasNombres
            )
        }
        persistir()
    }

    override fun cambiarCantidad(cartKey: String, delta: Int) {
        _items.value = _items.value.mapNotNull { item ->
            if (item.cartKey != cartKey) return@mapNotNull item
            val nuevaCantidad = item.cantidad + delta
            if (nuevaCantidad <= 0) null else item.copy(cantidad = nuevaCantidad)
        }
        persistir()
    }

    override fun eliminar(cartKey: String) {
        _items.value = _items.value.filter { it.cartKey != cartKey }
        persistir()
    }

    override fun vaciar() {
        _items.value = emptyList()
        _descuento.value = null
        persistir()
    }

    override fun aplicarDescuento(descuento: Descuento) {
        _descuento.value = descuento
        persistir()
    }

    override fun quitarDescuento() {
        _descuento.value = null
        persistir()
    }

    override fun cantidadTotal(): Int = _items.value.sumOf { it.cantidad }

    private fun persistir() {
        val userId = currentUserId ?: return
        val itemsSnapshot = _items.value
        val descuentoSnapshot = _descuento.value
        scope.launch {
            try {
                val key = stringPreferencesKey("cart_$userId")
                val data = PersistedCart(itemsSnapshot.map { it.toDto() }, descuentoSnapshot?.toDto())
                context.cartDataStore.edit { prefs ->
                    prefs[key] = json.encodeToString(data)
                }
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun cargarDesdeDisco() {
        val userId = currentUserId ?: return
        val key = stringPreferencesKey("cart_$userId")
        val prefs = context.cartDataStore.data.first()
        val raw = prefs[key] ?: run {
            _items.value = emptyList()
            _descuento.value = null
            return
        }
        try {
            val data = json.decodeFromString<PersistedCart>(raw)
            _items.value = data.items.map { it.toDomain() }
            _descuento.value = data.descuento?.toDomain()
        } catch (_: Exception) {
            _items.value = emptyList()
            _descuento.value = null
        }
    }

    @Serializable
    private data class PersistedCart(
        val items: List<CartItemDto> = emptyList(),
        val descuento: DescuentoDto? = null
    )

    @Serializable
    private data class CartItemDto(
        val cartKey: String,
        val productoId: String,
        val nombre: String,
        val precioUnitario: Double,
        val cantidad: Int,
        val localNombre: String,
        val localComidaId: String? = null,
        val extrasIds: List<String> = emptyList(),
        val extrasNombres: List<String> = emptyList()
    ) {
        fun toDomain() = CartItem(
            cartKey = cartKey,
            productoId = productoId,
            nombre = nombre,
            precioUnitario = precioUnitario,
            cantidad = cantidad,
            localNombre = localNombre,
            localComidaId = localComidaId,
            extrasIds = extrasIds,
            extrasNombres = extrasNombres
        )
    }

    @Serializable
    private data class DescuentoDto(
        val id: String,
        val codigo: String,
        val porcentaje: Double,
        val fechaVencimiento: String? = null,
        val activo: Boolean = true,
        val localComida: String? = null,
        val localComidaId: String? = null
    ) {
        fun toDomain() = Descuento(
            id = id,
            codigo = codigo,
            porcentaje = porcentaje,
            fechaVencimiento = fechaVencimiento,
            activo = activo,
            localComida = localComida,
            localComidaId = localComidaId
        )
    }

    private fun CartItem.toDto() = CartItemDto(
        cartKey = cartKey,
        productoId = productoId,
        nombre = nombre,
        precioUnitario = precioUnitario,
        cantidad = cantidad,
        localNombre = localNombre,
        localComidaId = localComidaId,
        extrasIds = extrasIds,
        extrasNombres = extrasNombres
    )

    private fun Descuento.toDto() = DescuentoDto(
        id = id,
        codigo = codigo,
        porcentaje = porcentaje,
        fechaVencimiento = fechaVencimiento,
        activo = activo,
        localComida = localComida,
        localComidaId = localComidaId
    )
}
