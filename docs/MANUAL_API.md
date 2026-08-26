# Manual API REST - FacturaSmart

Base URL local: `http://localhost:8080`

## Autenticación

Los endpoints privados requieren:

```http
Authorization: Bearer <accessToken>
```

El token se obtiene al registrar un cliente o al iniciar sesión.

## Endpoints públicos

| Método | Ruta | Uso |
|---|---|---|
| GET | `/api/v1/health` | Estado básico del servicio |
| GET | `/api/v1/config/public` | Configuración pública de integraciones (sin secretos) |
| POST | `/api/v1/auth/registro` | Crear cliente |
| POST | `/api/v1/auth/login` | Obtener JWT |

## Cliente autenticado

| Método | Ruta | Uso |
|---|---|---|
| GET | `/api/v1/clientes/me` | Consultar perfil |
| PUT | `/api/v1/clientes/me` | Actualizar perfil |
| DELETE | `/api/v1/clientes/me` | Desactivar cliente |
| POST | `/api/v1/facturas/procesar` | Registrar, generar y validar factura |
| GET | `/api/v1/facturas` | Listar facturas del cliente |
| GET | `/api/v1/facturas/{id}` | Consultar factura propia |
| GET | `/api/v1/facturas/{id}/xml` | Validar nuevamente y devolver XML |
| GET | `/api/v1/facturas/{id}/pdf` | Descargar PDF no editable |
| POST | `/api/v1/facturas/{id}/enviar` | Validar XML y enviar a DGTD |
| GET | `/api/v1/facturas/{id}/envios` | Historial de estados/acuse |

## Reglas principales de factura

- El ID no se puede repetir.
- El emisor debe corresponder al cliente autenticado.
- Cantidad y precios deben ser positivos.
- Descuentos, impuestos y subtotales no pueden ser negativos.
- `totalComprobante` debe ser positivo.
- La suma de las líneas debe coincidir con `totalComprobante` (tolerancia de 0.01).
- `totalGravado + totalExento - totalDescuentos + totalImpuesto` debe coincidir con `totalComprobante`.
- El XML debe aprobar el XSD configurado antes de guardarse y antes de enviarse.

## Respuestas de error

Formato común:

```json
{
  "timestamp": "2026-08-25T16:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Descripción del problema"
}
```

## Swagger

`http://localhost:8080/swagger-ui.html`

## Postman

Importar `postman/FacturacionElectronica.postman_collection.json`. La colección guarda automáticamente `accessToken` y `facturaId` cuando corresponde.


## Factura Bonita (servicio compartido)

La integración visual se mantiene separada del PDF local para no alterar el flujo estable.

```text
GET /api/v1/facturas/{id}/pdf-bonita
Authorization: Bearer <token>
```

Sincroniza la factura del cliente autenticado con la API compartida y devuelve el PDF visual.

```text
GET /api/v1/facturas/{id}/html-bonita
Authorization: Bearer <token>
```

Devuelve la representación HTML imprimible generada por el mismo servicio.

Si la API externa no responde o rechaza el contrato, FacturaSmart devuelve HTTP `502 Bad Gateway` con un mensaje de integración. El endpoint `/pdf` local no depende de este servicio y continúa disponible.


## Factura Bonita (servicio compartido)

La integración visual se mantiene separada del PDF local para no alterar el flujo estable.

```text
GET /api/v1/facturas/{id}/pdf-bonita
Authorization: Bearer <token>
```

Sincroniza la factura del cliente autenticado con la API compartida y devuelve el PDF visual.

```text
GET /api/v1/facturas/{id}/html-bonita
Authorization: Bearer <token>
```

Devuelve la representación HTML imprimible generada por el mismo servicio.

Si la API externa no responde o rechaza el contrato, FacturaSmart devuelve HTTP `502 Bad Gateway` con un mensaje de integración. El endpoint `/pdf` local no depende de este servicio y continúa disponible.

## Registrar pago confirmado por BankyFinanzas

`POST /api/v1/facturas/{id}/pago-banky`

Requiere JWT y la factura debe pertenecer al cliente autenticado. Este endpoint se utiliza después de que el checkout compartido de BankyFinanzas devuelve `status=completed`.

Ejemplo:

```json
{
  "status": "completed",
  "transactionCode": "BANKY-ABC123",
  "cardBrand": "VISA",
  "cardLastFourDigits": "4242",
  "cardholderName": "Cliente Prueba"
}
```

Al registrarse, la factura conserva su estado electrónico/DGTD por separado y cambia `estadoPago` a `PAGADA`. También guarda fecha, código de transacción, marca y últimos cuatro dígitos. El número completo de tarjeta y el código de seguridad nunca se almacenan.

> Nota de integración: el contrato compartido de BankyFinanzas comunica el resultado al navegador mediante `postMessage`. El backend registra el resultado recibido por FacturaSmart; si posteriormente Banky publica un endpoint de verificación servidor-a-servidor, debe incorporarse como validación adicional antes de marcar la factura como pagada.
