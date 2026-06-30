# UAMBite — Reglas de Negocio

> Documento de referencia para el agente que construye el cliente (Android/web).
> Acompaña a `api/api-docs.json` (contrato OpenAPI) y a `api/example/index.html` (referencia UX).

---

## 1. Autenticación

| Regla | Detalle |
|---|---|
| **Login** | `POST /auth/login` con `{ carnet, password }` → devuelve JWT + datos del usuario + `requiereCambioPassword` |
| **Register público** | Solo `ESTUDIANTE` o `PROFESOR` se pueden auto-registrar. `ADMIN` y `LOCAL` los crea el sistema |
| **Carnet** | 4–20 caracteres alfanuméricos, único |
| **Password** | 6–100 caracteres. Se guarda con BCrypt |
| **JWT** | Header `Authorization: Bearer <token>`. El token trae `carnet` (subject) y `userId` (claim) |
| **Rate limit** | `POST /auth/login`: máx 5 intentos por IP por minuto. Devuelve 429 con `Retry-After` |
| **Cambio de password** | `POST /auth/cambiar-password` (autenticado) con `{ passwordActual, passwordNuevo }`. Requiere que `passwordActual` sea correcta y que `nuevo` sea distinta |

## 2. Admin inicial (primer arranque)

| Regla | Detalle |
|---|---|
| **Creación automática** | Al arrancar la API, si no hay usuarios con rol `ADMIN`, `AdminBootstrap` crea uno |
| **Carnet default** | `ADMIN001` (configurable con `app.admin.carnet`) |
| **Password** | Generada aleatoriamente (12 chars alfanuméricos). Se imprime **una sola vez** en los logs del servidor |
| **Flag temporal** | Ese admin se crea con `passwordTemporal = true` |
| **Cambio forzado** | Al hacer login, si `requiereCambioPassword === true`, el cliente debe mostrar pantalla de cambio y bloquear el resto hasta que la cambie |
| **Idempotencia** | Si ya existe cualquier admin, `AdminBootstrap` no hace nada. Re-arrancar la API no duplica admins |

## 3. Roles y permisos

| Rol | Puede |
|---|---|
| **ADMIN** | Todo: CRUD de usuarios, locales, productos, franjas, descuentos, ingredientes, pedidos; gestionar imágenes |
| **LOCAL** | CRUD solo de **sus propios** locales y los productos/ingredientes/franjas/descuentos de esos locales. No ve ni toca otros locales |
| **ESTUDIANTE** | Ver home, ver locales, crear pedidos, ver sus pedidos, cancelar pedidos PENDIENTES propios, confirmar retiro LISTO propio, editar su perfil |
| **PROFESOR** | Igual que ESTUDIANTE |

> **Regla transversal:** nadie puede cambiar su propio `rol`. Solo ADMIN.

## 4. Reglas de ownership

```java
@PreAuthorize("hasRole('ADMIN') or @ownershipService.canEditLocal(#id, principal.id)")
@PreAuthorize("hasRole('ADMIN') or @ownershipService.canEditProducto(#id, principal.id)")
@PreAuthorize("hasAnyRole('ADMIN', 'LOCAL')")
@PreAuthorize("hasRole('ADMIN') or @ownershipService.canEditLocal(#request.localComidaId, principal.id)")
```

Resumen práctico:

| Recurso | Puede editar/borrar | Puede subir/borrar imagen |
|---|---|---|
| **Local** | ADMIN o dueño del local | ADMIN o dueño del local |
| **Producto** | ADMIN o dueño del local del producto | ADMIN o dueño del local del producto |
| **Ingrediente** | ADMIN o LOCAL (cualquiera) | ADMIN o LOCAL |
| **Franja** | ADMIN o dueño del local | — (sin imagen) |
| **Descuento** | ADMIN o dueño del local | — (sin imagen) |
| **Pedido** (transiciones) | ADMIN o dueño del local del pedido; o dueño del pedido si es PENDIENTE | — |
| **Usuario** | ADMIN o el propio usuario (no puede cambiar rol) | — |

`canEditLocal(localId, userId)` → true si `local.duenoId == userId` o si `duenoId` es null (escape hatch). `canEditProducto(productoId, userId)` delega en `canEditLocal` del local del producto.

## 5. Locales de Comida

| Regla | Detalle |
|---|---|
| Crear | Solo ADMIN. Se crea con un encargado (usuario LOCAL) en la misma operación atómica |
| Editar nombre/ubicación/horario | ADMIN o dueño |
| Asignar/reasignar encargado | Solo ADMIN. Crea el usuario LOCAL si no existe |
| Borrar | Solo ADMIN |

## 6. Productos

| Regla | Detalle |
|---|---|
| Campos requeridos | `nombre`, `precio`, `stock`, `permitePersonalizacion`, `localComidaId` |
| `version` | `@Version` (optimistic locking). No se expone al cliente |
| Crear | ADMIN o dueño del local destino |
| Editar/borrar | ADMIN o dueño del local del producto |
| Filtrar | `GET /producto/all?localComidaId=...&nombre=...&maxPrecio=...` (opcionales) |

## 7. Ingredientes Extra

| Regla | Detalle |
|---|---|
| Campos | `nombre`, `precioExtra` |
| CRUD | ADMIN o LOCAL (sin chequeo de ownership) |
| Vincular a producto | `POST /productoingredienteextra/save` con `{ productoId, ingredienteExtraId }` |

## 8. Pedidos — flujo de estados

```
PENDIENTE
  ├─► CONFIRMADO (ADMIN/LOCAL)
  │     ├─► EN_PREPARACION (ADMIN/LOCAL)
  │     │     └─► LISTO (ADMIN/LOCAL)
  │     │           ├─► [RETIRO_LOCAL] cliente confirma → ENTREGADO
  │     │           └─► [ENTREGA_INTERNA + PAGADO] LOCAL crea entrega → EN_CAMINO → ENTREGADA → ENTREGADO
  │     └─► CANCELADO (ADMIN/LOCAL o el dueño del pedido si PENDIENTE)
  └─► CANCELADO (ADMIN/LOCAL o el dueño del pedido)
```

Reglas:

- **Estados terminales** (no se pueden transicionar): `ENTREGADO`, `CANCELADO`
- **Cancelar PENDIENTE**: el dueño del pedido puede hacerlo. ADMIN/LOCAL también
- **Confirmar retiro** (RETIRO_LOCAL + LISTO): solo el dueño del pedido pasa a ENTREGADO
- **Crear entrega** (ENTREGA_INTERNA + LISTO + PAGADO): solo ADMIN/LOCAL del local
- **Finalizar entrega** (EN_CAMINO): solo ADMIN/LOCAL del local → estado pedido a ENTREGADO
- **Prioridad**: número editable por ADMIN/LOCAL con `PUT /pedido/{id}/prioridad`
- **Eliminar pedido**: solo estados terminales (PENDIENTE, CANCELADO, ENTREGADO)

## 9. Carrito (gestión 100% del lado cliente)

| Regla | Detalle |
|---|---|
| Persistencia | Local (DataStore en Android, localStorage en web), key por `userId` |
| `cartKey` | `productoId + "__" + ingredientesExtraIds.sort().join(",")` — el mismo producto con distintos extras son líneas distintas |
| Validación al checkout | El carrito se valida contra stock actual al confirmar |

## 10. Checkout (flujo de 3 llamadas)

```
1. POST /pedido/save
   { tipoEntrega, usuarioId, franjaHorariaId?, descuentoId? }
   → devuelve pedido con id, total, estado=PENDIENTE

2. Por cada item del carrito:
   POST /detallepedido/save
   { pedidoId, productoId, cantidad, ingredientesExtraIds[] }

3. POST /pago/save
   { metodoPago, pedidoId }
   → pago en estado PENDIENTE (luego se valida)
```

Si cualquiera falla, el cliente debe decidir si hacer rollback manual. La API no tiene transacción end-to-end.

## 11. Franjas Horarias

| Regla | Detalle |
|---|---|
| Campos | `horaInicio`, `horaFin`, `capacidadMaxima`, `pedidosActuales` (auto), `disponible` (auto), `localComidaId` |
| `disponibles` (público) | `GET /franja/disponibles` devuelve solo franjas con `pedidosActuales < capacidadMaxima` |
| Al crear pedido con franja | Incrementa `pedidosActuales` automáticamente |
| Al cambiar estado a terminal | Decrementa automáticamente |

## 12. Descuentos

| Regla | Detalle |
|---|---|
| Campos | `codigo`, `porcentaje`, `fechaVencimiento?`, `activo` |
| Global o por local | `localComidaId` puede ser null (global) o un UUID específico |
| Aplicar al carrito | El cliente busca por código, valida `activo === true` y `fechaVencimiento >= hoy` |
| Cálculo del descuento | `subtotal * porcentaje / 100` |

## 13. Pagos

| Regla | Detalle |
|---|---|
| `metodoPago` | `EFECTIVO`, `TARJETA`, `TRANSFERENCIA` |
| `estado` | `PENDIENTE` (al crear), `PAGADO`, `FALLIDO`, `REEMBOLSADO` |
| 1 pago por pedido | No hay pagos parciales |

## 14. Entregas

| Regla | Detalle |
|---|---|
| Solo para `ENTREGA_INTERNA` | No se crea entrega si el pedido es `RETIRO_LOCAL` |
| Estados | `PENDIENTE`, `EN_CAMINO`, `ENTREGADA`, `FALLIDA` |
| `ubicacion` | Texto libre (ej: "Recepción Edificio A") |

## 15. Imágenes

| Regla | Detalle |
|---|---|
| Almacenamiento | **BYTEA en PostgreSQL** (no en disco) |
| Campos | `imagen byte[]` + `imagenTipo varchar(50)` |
| Validación servidor | Tamaño ≤ 5MB, tipo `image/jpeg\|png\|webp` |
| GET es **público** | Cualquiera puede ver la imagen, sin auth |
| POST/DELETE requieren auth | ADMIN o LOCAL (con ownership en local/producto) |
| Response boolean | El response de listar incluye `tieneImagen: boolean` para no arrastrar los bytes |
| `Content-Type` | El GET devuelve el `Content-Type` correcto según `imagenTipo` |

URLs:

```
GET    /localcomida/{id}/imagen       → image/jpeg (o el tipo guardado)
GET    /producto/{id}/imagen
GET    /ingredienteextra/{id}/imagen

POST   /localcomida/{id}/imagen       multipart/form-data, campo "file"
POST   /producto/{id}/imagen
POST   /ingredienteextra/{id}/imagen

DELETE /localcomida/{id}/imagen
DELETE /producto/{id}/imagen
DELETE /ingredienteextra/{id}/imagen
```

## 16. Paginación

| Endpoint | Comportamiento |
|---|---|
| `GET /*/all` (listados) | Spring Data `Page<T>`. El cliente debe mandar `?size=1000` para traer casi todo de una |
| `GET /pedido/mios` | NO pagina (devuelve lista plana del usuario actual) |
| `GET /franja/disponibles` | NO pagina |
| `GET /producto/all?...` (con filtros) | Igual, pagina, mismo truco del `?size=1000` |

## 17. CORS / Red

| Configuración | Valor |
|---|---|
| Orígenes permitidos (default) | `http://localhost:3000`, `http://localhost:4200`, `http://localhost:5173` |
| En Android (emulador) | URL: `http://10.0.2.2:8090/api/`. Habilitar `usesCleartextTraffic` en dev |
| En Android (dispositivo físico) | URL: `http://<IP-de-tu-PC>:8090/api/` |
| Para release | Usar `network_security_config.xml` con HTTPS |

## 18. Reglas de validación de los DTOs

| Campo | Validación |
|---|---|
| `carnet` | 4-20 chars, alfanumérico |
| `password` | 6-100 chars |
| `nombre` | max 100 |
| `apellido` | max 100 |
| `correo` | formato email válido, max 150 |
| `rol` en register | exactamente `ESTUDIANTE` o `PROFESOR` |
| `codigo` descuento | requerido |
| `porcentaje` descuento | número, no negativo |
| `precio` producto | > 0, máx 2 decimales |
| `stock` producto | > 0 |
| `capacidadMaxima` franja | > 0 |

## 19. Errores

Todos los errores del backend devuelven JSON con esta forma (lo maneja el `ControllerAdvice`):

```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "Producto no encontrado.",
  "status": 404
}
```

El `code` es estable y se puede usar para tomar decisiones en el cliente. Algunos comunes:

| `code` | Status | Cuándo |
|---|---|---|
| `RESOURCE_NOT_FOUND` | 404 | Recurso no existe |
| `BUSINESS_ERROR` | 400 | Validación de negocio genérica |
| `INVALID_CURRENT_PASSWORD` | 400 | Cambio de password con actual incorrecta |
| `SAME_PASSWORD` | 400 | Cambio de password con la misma |
| `IMAGE_EMPTY` | 400 | Sin archivo |
| `IMAGE_TOO_LARGE` | 413 | > 5MB |
| `IMAGE_UNSUPPORTED_TYPE` | 415 | No es jpg/png/webp |
| `IMAGE_READ_FAILED` | 500 | Error leyendo bytes |
| `RATE_LIMIT` | 429 | Demasiados login |
| `UNAUTHORIZED` | 401 | Token faltante o inválido |
| `FORBIDDEN_ROLE` | 403 | Rol no permitido en self-register |
| `INVALID_ROLE` | 400 | Rol no reconocido |
| `ROLE_MISMATCH` | 409 | Usuario no tiene rol esperado |

---

## Kit de contexto para el agente

Para construir el cliente (Android u otro), entregarle al agente:

1. `api/api-docs.json` — contrato OpenAPI
2. `api/example/index.html` — referencia visual y de UX
3. **Este archivo** — reglas de negocio, ownership, transiciones de estado, formato de errores
4. (Opcional) El código fuente de los controllers de Spring — para resolver dudas puntuales sobre un endpoint

Con estos 3–4 elementos tiene contexto suficiente para no consultar cada paso.
