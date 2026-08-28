# Estado de integraciones con otros módulos del curso

## 1. Firma digital — HSM Sign CR

La integración de firma digital ya está incorporada.

- Servicio configurado por defecto: `https://hsm-sign-cr.onrender.com`.
- El PIN se captura únicamente dentro del popup de HSM Sign CR; FacturaSmart no lo solicita ni lo almacena.
- Endpoint propio: `POST /api/v1/facturas/{id}/firma-hsm`.
- FacturaSmart conserva el XML original, recibe el XML firmado, comprueba que corresponde al comprobante original y vuelve a validar la firma contra HSM antes de marcar la factura como `FIRMADA`.
- Variable de entorno: `HSM_SIGN_URL`.

Consulte `docs/INTEGRACION_HSM_SIGN_CR.md`.

## 2. DGTD / Mini Tributación Directa

`DgtdClient` conserva el modo simulado para pruebas existentes y ya incluye la integración real con Mini Tributación Directa.

- Backend: `https://mini-tributacion-backend.onrender.com`.
- Endpoint externo utilizado: `POST /api/facturas`.
- Endpoint propio de FacturaSmart: `POST /api/v1/facturas/{id}/enviar`.
- En modo `REAL` se envían los datos de la factura y `archivoXML`.
- Cuando existe una firma HSM válida, se envía el XML firmado.
- FacturaSmart guarda estado, código de respuesta, mensaje, número de acuse y modo de integración.

Variables:

```text
INTEGRACIONES_MODO=SIMULADO|REAL
DGTD_API_URL=https://mini-tributacion-backend.onrender.com
```

El valor predeterminado continúa siendo `SIMULADO` para no alterar las pruebas y flujos anteriores. Para consumir Mini Tributación real se debe usar `INTEGRACIONES_MODO=REAL`.

## 3. BankyFinanzas

La integración existente se conserva sin cambios funcionales en esta entrega.

```text
BANKY_URL=https://bankyfinanzas.netlify.app
BANKY_MERCHANT_ID=<ID DEL COMERCIO DE ESTE GRUPO>
BANKY_CURRENCY=CRC
```

## 4. Factura Bonita

La integración existente con Factura Bonita se conserva sin cambios funcionales en esta entrega.

```text
FACTURA_BONITA_URL=https://proyecto-kn7p.onrender.com
FACTURA_BONITA_ORIGEN=facturasmart
```

## 5. Validador XML

La validación local sigue usando `src/main/resources/xsd/factura-electronica-4.3-base.xsd`. El XSD incluido continúa siendo una base de laboratorio y no debe presentarse como el XSD oficial completo de Hacienda.

## 6. ParkSmart

ParkSmart queda como la integración pendiente siguiente. Debe consumir FacturaSmart mediante API REST y no mediante acceso directo a la base de datos.
