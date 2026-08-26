# Integración con API Factura Bonita

FacturaSmart consume la API compartida publicada por el grupo responsable de la representación visual de la factura.

## Servicio externo

- Base URL por defecto: `https://proyecto-kn7p.onrender.com`
- Contrato: `GET /api/contrato`
- Registro: `POST /api/facturas`
- PDF/HTML: `GET /api/documentos/facturas/:id?formato=pdf|html&plantilla=auto`

La URL se puede cambiar sin recompilar mediante:

```text
FACTURA_BONITA_URL=https://proyecto-kn7p.onrender.com
FACTURA_BONITA_ORIGEN=facturasmart
```

## Flujo implementado

1. El cliente crea su factura normalmente en FacturaSmart.
2. XML/XSD, PDF local, DGTD y los demás flujos existentes continúan sin cambios.
3. Cuando el cliente solicita **Factura bonita PDF** o **Vista bonita**, el backend obtiene únicamente una factura que pertenezca al usuario autenticado.
4. `FacturaBonitaPayloadMapper` adapta el modelo interno al JSON compartido `v44-visual`.
5. Se envía la factura a `POST /api/facturas` con `origen=facturasmart` y `referenciaExterna=factura:<ID_LOCAL>`.
6. Se recupera el identificador remoto. Si la factura ya existía, el cliente intenta localizarla mediante `origen` + `referenciaExterna`.
7. El backend solicita el PDF o HTML al servicio compartido y lo devuelve al navegador.

## Endpoints agregados a FacturaSmart

```text
GET /api/v1/facturas/{id}/pdf-bonita
GET /api/v1/facturas/{id}/html-bonita
```

Ambos requieren JWT y respetan la misma validación de propiedad de factura que el resto de endpoints.

## Campos que se adaptan

El mapper transforma los campos que FacturaSmart ya conoce: fecha, moneda, condición de venta, medio de pago, emisor, receptor, líneas, IVA y totales. También agrega la estructura de resumen solicitada por el contrato compartido.

Campos fiscales que FacturaSmart todavía no captura, como CABYS, ubicación fiscal o actividad económica, no se inventan. La API compartida indica que los campos ampliados se muestran/validan cuando se proporcionan. Si el profesor exige esos campos como obligatorios más adelante, deben añadirse al modelo de captura con datos reales.

## Protección de la versión estable

El generador PDF local original **no fue eliminado ni reemplazado**. `GET /api/v1/facturas/{id}/pdf` continúa funcionando como antes. La integración externa se agregó como una opción adicional para no romper el flujo estable.
