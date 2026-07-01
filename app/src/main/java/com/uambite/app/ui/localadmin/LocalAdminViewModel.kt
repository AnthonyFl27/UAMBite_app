package com.uambite.app.ui.localadmin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uambite.app.domain.model.Descuento
import com.uambite.app.domain.model.FranjaHoraria
import com.uambite.app.domain.model.IngredienteExtra
import com.uambite.app.domain.model.Local
import com.uambite.app.domain.model.Pedido
import com.uambite.app.domain.model.Producto
import com.uambite.app.domain.repository.DescuentosRepository
import com.uambite.app.domain.repository.FranjasRepository
import com.uambite.app.domain.repository.IngredientesRepository
import com.uambite.app.domain.repository.LocalesRepository
import com.uambite.app.domain.repository.PedidosRepository
import com.uambite.app.domain.repository.ProductosRepository
import com.uambite.app.domain.repository.UsuariosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalAdminViewModel @Inject constructor(
    private val localesRepository: LocalesRepository,
    private val productosRepository: ProductosRepository,
    private val ingredientesRepository: IngredientesRepository,
    private val franjasRepository: FranjasRepository,
    private val descuentosRepository: DescuentosRepository,
    private val pedidosRepository: PedidosRepository,
    private val usuariosRepository: UsuariosRepository
) : ViewModel() {

    private val _misLocales = MutableStateFlow<List<Local>>(emptyList())
    val misLocales: StateFlow<List<Local>> = _misLocales

    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos: StateFlow<List<Pedido>> = _pedidos

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos

    private val _ingredientes = MutableStateFlow<List<IngredienteExtra>>(emptyList())
    val ingredientes: StateFlow<List<IngredienteExtra>> = _ingredientes

    private val _franjas = MutableStateFlow<List<FranjaHoraria>>(emptyList())
    val franjas: StateFlow<List<FranjaHoraria>> = _franjas

    private val _descuentos = MutableStateFlow<List<Descuento>>(emptyList())
    val descuentos: StateFlow<List<Descuento>> = _descuentos

    private val _loading = MutableStateFlow<Set<String>>(emptySet())
    val loading: StateFlow<Set<String>> = _loading

    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar

    fun resetSnackbar() { _snackbar.value = null }

    private fun setLoading(tab: String, value: Boolean) {
        val current = _loading.value.toMutableSet()
        if (value) current.add(tab) else current.remove(tab)
        _loading.value = current
    }

    fun loadTab(tab: String, userId: String?) {
        if (userId == null) return
        when (tab) {
            "pedidos" -> loadPedidos(userId)
            "local" -> loadLocalesPropios(userId)
            "productos" -> loadProductosPropios(userId)
            "franjas" -> loadFranjasPropias(userId)
            "descuentos" -> loadDescuentosPropios(userId)
            "ingredientes" -> loadIngredientes()
        }
    }

    private suspend fun cargarLocalesPropios(userId: String): List<Local> {
        val locales = localesRepository.getLocales().getOrNull().orEmpty()
        val mios = locales.filter { it.duenoId == userId }
        _misLocales.value = mios
        return mios
    }

    private fun loadLocalesPropios(userId: String) {
        viewModelScope.launch {
            setLoading("local", true)
            cargarLocalesPropios(userId)
            setLoading("local", false)
        }
    }

    private fun loadPedidos(userId: String) {
        viewModelScope.launch {
            setLoading("pedidos", true)
            val mios = cargarLocalesPropios(userId)
            val todos = pedidosRepository.getMisPedidos().getOrNull().orEmpty()
            val misIds = mios.map { it.id }.toSet()
            _pedidos.value = todos.filter { p ->
                p.localComidaId != null && p.localComidaId in misIds
            }.sortedByDescending { it.createdAt ?: "" }
            setLoading("pedidos", false)
        }
    }

    private fun loadProductosPropios(userId: String) {
        viewModelScope.launch {
            setLoading("productos", true)
            val mios = cargarLocalesPropios(userId)
            val todos = productosRepository.getProductos().getOrNull().orEmpty()
            val misNombres = mios.map { it.nombre }.toSet()
            _productos.value = todos.filter { p ->
                p.localComidaId != null && mios.any { it.id == p.localComidaId } ||
                p.localComida in misNombres
            }
            ingredientesRepository.getIngredientes().onSuccess { _ingredientes.value = it }
            setLoading("productos", false)
        }
    }

    private fun loadFranjasPropias(userId: String) {
        viewModelScope.launch {
            setLoading("franjas", true)
            cargarLocalesPropios(userId)
            val mios = _misLocales.value
            val misIds = mios.map { it.id }.toSet()
            val todas = franjasRepository.getAll().getOrNull().orEmpty()
            _franjas.value = todas.filter { it.localComidaId in misIds }
            setLoading("franjas", false)
        }
    }

    private fun loadDescuentosPropios(userId: String) {
        viewModelScope.launch {
            setLoading("descuentos", true)
            cargarLocalesPropios(userId)
            val mios = _misLocales.value
            val misIds = mios.map { it.id }.toSet()
            val todos = descuentosRepository.getAll().getOrNull().orEmpty()
            _descuentos.value = todos.filter { it.localComidaId in misIds }
            setLoading("descuentos", false)
        }
    }

    private fun loadIngredientes() {
        viewModelScope.launch {
            setLoading("ingredientes", true)
            ingredientesRepository.getIngredientes().onSuccess { _ingredientes.value = it }
            setLoading("ingredientes", false)
        }
    }

    fun cambiarEstadoPedido(pedidoId: String, estado: String) {
        viewModelScope.launch {
            val r = pedidosRepository.cambiarEstado(pedidoId, estado)
            r.onSuccess { _snackbar.value = "Pedido → $estado" }
                .onFailure { _snackbar.value = it.message }
        }
    }

    fun setPrioridad(pedidoId: String, prioridad: Int) {
        viewModelScope.launch {
            pedidosRepository.setPrioridad(pedidoId, prioridad)
        }
    }

    fun actualizarLocal(id: String, nombre: String, ubicacion: String, horario: String?) {
        viewModelScope.launch {
            val r = localesRepository.actualizar(id, nombre, ubicacion, horario)
            r.onSuccess { _snackbar.value = "Local actualizado" }
                .onFailure { _snackbar.value = it.message }
            setLoading("local", true)
            setLoading("local", false)
        }
    }

    fun eliminarLocal(id: String) {
        viewModelScope.launch {
            val r = localesRepository.eliminar(id)
            r.onSuccess { _snackbar.value = "Local eliminado" }
                .onFailure { _snackbar.value = it.message }
        }
    }

    fun crearProducto(
        nombre: String,
        descripcion: String?,
        precio: Double,
        stock: Int,
        permitePersonalizacion: Boolean,
        localComidaId: String
    ) {
        viewModelScope.launch {
            val r = productosRepository.crear(
                nombre, descripcion, precio, stock, permitePersonalizacion, localComidaId
            )
            r.onSuccess { _snackbar.value = "Producto creado" }
                .onFailure { _snackbar.value = it.message }
        }
    }

    fun actualizarProducto(
        id: String,
        nombre: String,
        descripcion: String?,
        precio: Double,
        stock: Int,
        permitePersonalizacion: Boolean,
        localComidaId: String
    ) {
        viewModelScope.launch {
            val r = productosRepository.actualizar(
                id, nombre, descripcion, precio, stock, permitePersonalizacion, localComidaId
            )
            r.onSuccess { _snackbar.value = "Producto actualizado" }
                .onFailure { _snackbar.value = it.message }
        }
    }

    fun eliminarProducto(id: String) {
        viewModelScope.launch {
            val r = productosRepository.eliminar(id)
            r.onSuccess { _snackbar.value = "Producto eliminado" }
                .onFailure { _snackbar.value = it.message }
        }
    }

    fun crearFranja(horaInicio: String, horaFin: String, capacidadMaxima: Int, localComidaId: String) {
        viewModelScope.launch {
            val r = franjasRepository.crear(horaInicio, horaFin, capacidadMaxima, localComidaId)
            r.onSuccess { _snackbar.value = "Franja creada" }
                .onFailure { _snackbar.value = it.message }
        }
    }

    fun actualizarFranja(
        id: String,
        horaInicio: String,
        horaFin: String,
        capacidadMaxima: Int,
        disponible: Boolean,
        localComidaId: String
    ) {
        viewModelScope.launch {
            val r = franjasRepository.actualizar(
                id, horaInicio, horaFin, capacidadMaxima, disponible, localComidaId
            )
            r.onSuccess { _snackbar.value = "Franja actualizada" }
                .onFailure { _snackbar.value = it.message }
        }
    }

    fun eliminarFranja(id: String) {
        viewModelScope.launch {
            val r = franjasRepository.eliminar(id)
            r.onSuccess { _snackbar.value = "Franja eliminada" }
                .onFailure { _snackbar.value = it.message }
        }
    }

    fun crearDescuento(
        codigo: String,
        porcentaje: Double,
        fechaVencimiento: String?,
        activo: Boolean,
        localComidaId: String?
    ) {
        viewModelScope.launch {
            val r = descuentosRepository.crear(
                codigo, porcentaje, fechaVencimiento, activo, localComidaId
            )
            r.onSuccess { _snackbar.value = "Descuento creado" }
                .onFailure { _snackbar.value = it.message }
        }
    }

    fun actualizarDescuento(
        id: String,
        codigo: String,
        porcentaje: Double,
        fechaVencimiento: String?,
        activo: Boolean,
        localComidaId: String?
    ) {
        viewModelScope.launch {
            val r = descuentosRepository.actualizar(
                id, codigo, porcentaje, fechaVencimiento, activo, localComidaId
            )
            r.onSuccess { _snackbar.value = "Descuento actualizado" }
                .onFailure { _snackbar.value = it.message }
        }
    }

    fun eliminarDescuento(id: String) {
        viewModelScope.launch {
            val r = descuentosRepository.eliminar(id)
            r.onSuccess { _snackbar.value = "Descuento eliminado" }
                .onFailure { _snackbar.value = it.message }
        }
    }

    fun crearIngrediente(nombre: String, precioExtra: Double) {
        viewModelScope.launch {
            val r = ingredientesRepository.crear(nombre, precioExtra)
            r.onSuccess { _snackbar.value = "Ingrediente creado" }
                .onFailure { _snackbar.value = it.message }
        }
    }

    fun actualizarIngrediente(id: String, nombre: String, precioExtra: Double) {
        viewModelScope.launch {
            val r = ingredientesRepository.actualizar(id, nombre, precioExtra)
            r.onSuccess { _snackbar.value = "Ingrediente actualizado" }
                .onFailure { _snackbar.value = it.message }
        }
    }

    fun eliminarIngrediente(id: String) {
        viewModelScope.launch {
            val r = ingredientesRepository.eliminar(id)
            r.onSuccess { _snackbar.value = "Ingrediente eliminado" }
                .onFailure { _snackbar.value = it.message }
        }
    }
}
