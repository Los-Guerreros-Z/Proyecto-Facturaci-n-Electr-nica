# Integración final: HSM Sign CR + Mini Tributación Directa

## Alcance

Esta versión conserva las funcionalidades existentes de FacturaSmart (JWT, PostgreSQL/H2, BankyFinanzas, Factura Bonita, XML/PDF y consultas) e incorpora únicamente el flujo necesario de Firma Digital HSM y Mini Tributación.

## Firma Digital

- Servicio: `https://hsm-sign-cr.onrender.com`
- El PIN se captura exclusivamente en el popup de HSM Sign CR.
- FacturaSmart nunca solicita ni almacena el PIN.
- Endpoint propio: `POST /api/v1/facturas/{id}/firma-hsm`.
- El backend comprueba que el XML firmado corresponda al XML original y vuelve a validar la firma con HSM.
- Se conserva `xml_generado` y se almacena el firmado por separado en `xml_firmado`.
- Una firma válida cambia la factura a `FIRMADA`.

## Mini Tributación Directa

- Backend: `https://mini-tributacion-backend.onrender.com`
- Recepción: `POST /api/facturas`.
- El endpoint existente de FacturaSmart `POST /api/v1/facturas/{id}/enviar` utiliza `DgtdClient`.
- En modo REAL, `DgtdClient` envía los datos de la factura y `archivoXML` a Mini Tributación.
- Si existe una firma HSM válida se envía `xml_firmado`; en caso contrario se conserva la compatibilidad existente y se envía el XML original.
- FacturaSmart registra aceptación/rechazo, código, mensaje y número de acuse.

## Variables

```text
INTEGRACIONES_MODO=SIMULADO|REAL
HSM_SIGN_URL=https://hsm-sign-cr.onrender.com
DGTD_API_URL=https://mini-tributacion-backend.onrender.com
```

El modo predeterminado continúa siendo `SIMULADO` para no alterar pruebas existentes. Para probar Tributación real, usar `INTEGRACIONES_MODO=REAL`.

## Flujo

1. Crear factura.
2. FacturaSmart genera y valida el XML.
3. Firmar con HSM Sign CR.
4. FacturaSmart valida la firma y almacena el XML firmado.
5. Enviar con `/api/v1/facturas/{id}/enviar`.
6. Mini Tributación responde Aceptada/Rechazada y, si aplica, número de acuse.
