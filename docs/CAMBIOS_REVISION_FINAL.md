# Cambios de esta revisión

## Backend

- Se conserva la arquitectura Controller / Service / Repository / Domain.
- Se reforzó la validación de totales para evitar comprobantes aritméticamente inconsistentes.
- `FacturaResponse` incluye moneda para que frontend/clientes muestren el monto correctamente.
- Se mantiene validación XML/XSD al generar, consultar y antes de DGTD.
- Firma digital simulada ya **no** marca una factura como firmada; queda en pendiente de integración hasta que el otro grupo aplique una firma real.
- DGTD mantiene aceptación/rechazo controlados para laboratorio.
- Se agregó `/api/v1/config/public` para entregar al frontend únicamente configuración pública de integraciones.

## BankyFinanzas

- Se adaptó el checkout compartido sin reutilizar el `MERCHANT_ID` de otro grupo.
- `BANKY_MERCHANT_ID` se configura por ambiente.
- Moneda por defecto CRC.
- Se conserva verificación de `event.origin` y manejo de `completed`, `rejected` y `cancelled`.
- El sistema no recibe número completo de tarjeta ni CVV.

## Frontend

- Rediseño completo inspirado visualmente en ParkSmart.
- Registro e inicio de sesión.
- Dashboard con indicadores.
- Listado y detalle de facturas.
- Consulta de XML, descarga PDF, envío DGTD e historial.
- Formulario web para generar una factura de laboratorio.
- Vista de integraciones y estado de Banky/firma/DGTD/XSD.
- Perfil de cliente.
- Diseño adaptable a escritorio y móvil.

## Pruebas

- Se conserva el flujo principal automatizado.
- Se conserva prueba directa del XSD.
- Se agregaron pruebas de seguridad, aislamiento entre clientes, totales inconsistentes y configuración pública.
- Se eliminó TestNG del `pom.xml`; Spring Boot Starter Test ya provee JUnit 5.

## Pendiente externo

- Contrato REST definitivo de firma/cifrado.
- Contrato REST definitivo de DGTD.
- XSD 4.3 definitivo acordado/aprobado para la integración del curso.
- `MERCHANT_ID` real de este grupo para BankyFinanzas.
- Integración con ParkSmart, deliberadamente pospuesta hasta que el equipo termine los cambios actuales.
