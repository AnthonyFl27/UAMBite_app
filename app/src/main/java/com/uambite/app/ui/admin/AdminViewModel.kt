package com.uambite.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uambite.app.data.api.UsuarioResponse
import com.uambite.app.domain.model.Descuento
import com.uambite.app.domain.model.Entrega
import com.uambite.app.domain.model.FranjaHoraria
import com.uambite.app.domain.model.IngredienteExtra
import com.uambite.app.domain.model.Local
import com.uambite.app.domain.model.Pedido
import com.uambite.app.domain.model.Producto
import com.uambite.app.domain.repository.DescuentosRepository
import com.uambite.app.domain.repository.EntregasRepository
import com.uambite.app.domain.repository.FranjasRepository
import com.uambite.app.domain.repository.IngredientesRepository
import com.uambite.app.domain.repository.LocalesRepository
import com.uambite.app.domain.repository.PagosRepository
import com.uambite.app.domain.repository.PedidosRepository
import com.uambite.app.domain.repository.ProductoIngredienteRepository
import com.uambite.app.domain.repository.ProductosRepository
import com.uambite.app.domain.repository.UsuariosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val localesRepository: LocalesRepository,
    private val productosRepository: ProductosRepository,
    private val ingredientesRepository: IngredientesRepository,
    private val productoIngredienteRepository: ProductoIngredienteRepository,
    private val franjasRepository: FranjasRepository,
    private val descuentosRepository: DescuentosRepository,
    private val usuariosRepository: UsuariosRepository,
    private val pedidosRepository: PedidosRepository,
    private val entregasRepository: EntregasRepository,
    private val pagosRepository: PagosRepository
) : ViewModel() {

    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos: StateFlow<List<Pedido>> = _pedidos

    private val _usuarios = MutableStateFlow<List<UsuarioResponse>>(emptyList())
    val usuarios: StateFlow<List<UsuarioResponse>> = _usuarios

    private val _locales = MutableStateFlow<List<Local>>(emptyList())
    val locales: StateFlow<List<Local>> = _locales

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

    fun loadTab(tab: String) {
        when (tab) {
            "pedidos" -> loadPedidos()
            "usuarios" -> loadUsuarios()
            "locales" -> loadLocales()
            "productos" -> loadProductos()
            "franjas" -> loadFranjas()
            "descuentos" -> loadDescuentos()
            "ingredientes" -> loadIngredientes()
        }
    }

    private fun setLoading(tab: String, value: Boolean) {
        val current = _loading.value.toMutableSet()
        if (value) current.add(tab) else current.remove(tab)
        _loading.value = current
    }

    fun resetSnackbar() { _snackbar.value = null }

    private fun loadPedidos() {
        viewModelScope.launch {
            setLoading("pedidos", true)
            val result = pedidosRepository.getAllPedidos()
            result.onSuccess { _pedidos.value = it.sortedByDescending { p -> p.createdAt ?: "" } }
            setLoading("pedidos", false)
        }
    }

    fun loadAllPedidos() {
        viewModelScope.launch {
            setLoading("pedidos", true)
            val allLocales = localesRepository.getLocales().getOrNull().orEmpty()
            val result = pedidosRepository.getAllPedidos()
            result.onSuccess { _pedidos.value = it.sortedByDescending { p -> p.createdAt ?: "" } }
            _locales.value = allLocales
            setLoading("pedidos", false)
        }
    }

    private fun loadUsuarios() {
        viewModelScope.launch {
            setLoading("usuarios", true)
            usuariosRepository.getAll().onSuccess { _usuarios.value = it }
            setLoading("usuarios", false)
        }
    }

    private fun loadLocales() {
        viewModelScope.launch {
            setLoading("locales", true)
            localesRepository.getLocales().onSuccess { _locales.value = it }
            setLoading("locales", false)
        }
    }

    private fun loadProductos() {
        viewModelScope.launch {
            setLoading("productos", true)
            productosRepository.getProductos().onSuccess { _productos.value = it }
            ingredientesRepository.getIngredientes().onSuccess { _ingredientes.value = it }
            localesRepository.getLocales().onSuccess { _locales.value = it }
            setLoading("productos", false)
        }
    }

    private fun loadFranjas() {
        viewModelScope.launch {
            setLoading("franjas", true)
            franjasRepository.getAll().onSuccess { _franjas.value = it }
            localesRepository.getLocales().onSuccess { _locales.value = it }
            setLoading("franjas", false)
        }
    }

    private fun loadDescuentos() {
        viewModelScope.launch {
            setLoading("descuentos", true)
            descuentosRepository.getAll().onSuccess { _descuentos.value = it }
            localesRepository.getLocales().onSuccess { _locales.value = it }
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
            loadPedidos()
        }
    }

    fun setPrioridad(pedidoId: String, prioridad: Int) {
        viewModelScope.launch {
            pedidosRepository.setPrioridad(pedidoId, prioridad)
            loadPedidos()
        }
    }

    fun crearEntrega(pedidoId: String, ubicacion: String) {
        viewModelScope.launch {
            val r = entregasRepository.crear(pedidoId, ubicacion)
            r.onSuccess { _snackbar.value = "Entrega creada (EN_CAMINO)" }
                .onFailure { _snackbar.value = it.message }
            loadPedidos()
        }
    }

    fun finalizarEntrega(entregaId: String) {
        viewModelScope.launch {
            val r = entregasRepository.finalizar(entregaId)
            r.onSuccess { _snackbar.value = "Entrega finalizada" }
                .onFailure { _snackbar.value = it.message }
            loadPedidos()
        }
    }

    fun crearUsuario(
        carnet: String,
        nombre: String,
        apellido: String,
        correo: String?,
        rol: String,
        password: String
    ) {
        viewModelScope.launch {
            val r = usuariosRepository.save(carnet, nombre, apellido, correo, rol, password)
            r.onSuccess { _snackbar.value = "Usuario creado" }
                .onFailure { _snackbar.value = it.message }
            loadUsuarios()
        }
    }

    fun eliminarUsuario(id: String) {
        viewModelScope.launch {
            val r = usuariosRepository.update(id, carnet = "", rol = "", nombre = "", apellido = "", correo = null, password = null)
            // sin delete en api, no implementado
            _snackbar.value = "Eliminar no soportado en API"
        }
    }

    fun crearLocal(
        nombre: String,
        ubicacion: String,
        horario: String?,
        encCarnet: String,
        encNombre: String,
        encApellido: String,
        encCorreo: String?,
        encPassword: String
    ) {
        viewModelScope.launch {
            val r = localesRepository.crear(
                nombre, ubicacion, horario,
                encCarnet, encNombre, encApellido, encCorreo, encPassword
            )
            r.onSuccess { _snackbar.value = "Local creado" }
                .onFailure { _snackbar.value = it.message }
            loadLocales()
        }
    }

    fun actualizarLocal(id: String, nombre: String, ubicacion: String, horario: String?) {
        viewModelScope.launch {
            val r = localesRepository.actualizar(id, nombre, ubicacion, horario)
            r.onSuccess { _snackbar.value = "Local actualizado" }
                .onFailure { _snackbar.value = it.message }
            loadLocales()
        }
    }

    fun asignarEncargado(
        localId: String,
        carnet: String,
        nombre: String,
        apellido: String,
        correo: String?,
        password: String
    ) {
        viewModelScope.launch {
            val r = localesRepository.asignarEncargado(localId, carnet, nombre, apellido, correo, password)
            r.onSuccess { _snackbar.value = "Encargado asignado" }
                .onFailure { _snackbar.value = it.message }
            loadLocales()
        }
    }

    fun eliminarLocal(id: String) {
        viewModelScope.launch {
            val r = localesRepository.eliminar(id)
            r.onSuccess { _snackbar.value = "Local eliminado" }
                .onFailure { _snackbar.value = it.message }
            loadLocales()
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
            loadProductos()
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
            loadProductos()
        }
    }

    fun eliminarProducto(id: String) {
        viewModelScope.launch {
            val r = productosRepository.eliminar(id)
            r.onSuccess { _snackbar.value = "Producto eliminado" }
                .onFailure { _snackbar.value = it.message }
            loadProductos()
        }
    }

    fun crearFranja(horaInicio: String, horaFin: String, capacidadMaxima: Int, localComidaId: String) {
        viewModelScope.launch {
            val r = franjasRepository.crear(horaInicio, horaFin, capacidadMaxima, localComidaId)
            r.onSuccess { _snackbar.value = "Franja creada" }
                .onFailure { _snackbar.value = it.message }
            loadFranjas()
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
            loadFranjas()
        }
    }

    fun eliminarFranja(id: String) {
        viewModelScope.launch {
            val r = franjasRepository.eliminar(id)
            r.onSuccess { _snackbar.value = "Franja eliminada" }
                .onFailure { _snackbar.value = it.message }
            loadFranjas()
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
            loadDescuentos()
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
            loadDescuentos()
        }
    }

    fun eliminarDescuento(id: String) {
        viewModelScope.launch {
            val r = descuentosRepository.eliminar(id)
            r.onSuccess { _snackbar.value = "Descuento eliminado" }
                .onFailure { _snackbar.value = it.message }
            loadDescuentos()
        }
    }

    fun crearIngrediente(nombre: String, precioExtra: Double) {
        viewModelScope.launch {
            val r = ingredientesRepository.crear(nombre, precioExtra)
            r.onSuccess { _snackbar.value = "Ingrediente creado" }
                .onFailure { _snackbar.value = it.message }
            loadIngredientes()
        }
    }

    fun actualizarIngrediente(id: String, nombre: String, precioExtra: Double) {
        viewModelScope.launch {
            val r = ingredientesRepository.actualizar(id, nombre, precioExtra)
            r.onSuccess { _snackbar.value = "Ingrediente actualizado" }
                .onFailure { _snackbar.value = it.message }
            loadIngredientes()
        }
    }

    fun eliminarIngrediente(id: String) {
        viewModelScope.launch {
            val r = ingredientesRepository.eliminar(id)
            r.onSuccess { _snackbar.value = "Ingrediente eliminado" }
                .onFailure { _snackbar.value = it.message }
            loadIngredientes()
        }
    }
}
