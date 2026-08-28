# Integración HSM Sign CR

## Objetivo

FacturaSmart integra el servicio HSM Sign CR sin solicitar ni almacenar el PIN de firma del usuario. El PIN se captura exclusivamente dentro del popup servido por HSM Sign CR.

## Flujo implementado

1. FacturaSmart genera el XML y lo valida contra el XSD local.
2. La factura queda en estado `VALIDADA`.
3. El usuario selecciona **Firmar con HSM**.
4. `hsmSignCheckout.js` abre `firmar_popup.html` en HSM Sign CR y envía el XML mediante `postMessage` únicamente al origen configurado.
5. HSM devuelve `xmlFirmado`, `hashDocumento` y `serialCertificado` al navegador.
6. El navegador envía el XML firmado a `POST /api/v1/facturas/{id}/firma-hsm` con el JWT de FacturaSmart.
7. El backend verifica que el XML firmado conserva el contenido del XML original.
8. El backend llama directamente a `POST {HSM_SIGN_URL}/api/documentos/validar`.
9. Se comprueba que la firma sea válida y que `signerId` corresponda a la identificación del emisor.
10. Se almacena el XML firmado y la metadata de validación, y la factura pasa a `FIRMADA`.

## Endpoint propio

`POST /api/v1/facturas/{id}/firma-hsm`

```json
{
  "xmlFirmado": "<FacturaElectronica>...</FacturaElectronica>",
  "hashDocumento": "hash-devuelto-por-hsm",
  "serialCertificado": "serial-devuelto-por-hsm"
}
```

El endpoint no confía en el estado enviado por el navegador. FacturaSmart vuelve a validar la firma directamente contra HSM Sign CR antes de persistirla.

## Configuración

```text
HSM_SIGN_URL=https://hsm-sign-cr.onrender.com
```

## Persistencia

El XML original permanece en `xml_generado`. El documento firmado se guarda en `xml_firmado`. Esto evita perder el comprobante validado contra XSD y permite conservar trazabilidad del proceso.

La columna `firma_valida` es nullable a nivel de esquema para permitir la actualización de bases con facturas preexistentes; en el dominio un valor nulo se interpreta como `false`.
