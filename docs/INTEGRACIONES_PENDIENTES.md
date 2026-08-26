# Integraciones con otros módulos del curso

## 1. Firma digital y cifrado

La firma/cifrado **no se implementa dentro de este proyecto** porque corresponde al módulo del otro grupo. `FirmaDigitalClient` mantiene aislado el punto de integración.

- `SIMULADO`: permite completar el flujo local sin afirmar que existe una firma oficial.
- `REAL`: queda preparado para conectar `FIRMA_API_URL` cuando el grupo responsable entregue su contrato REST definitivo.
- Antes de activar `REAL` se deben confirmar: URL, método HTTP, JSON de entrada, JSON de salida, autenticación, manejo de errores y formato del XML firmado.

No se debe inventar ese contrato ni acoplar el proyecto a una firma ficticia.

## 2. DGTD / Mini Tributación

`DgtdClient` permite probar aceptación y rechazo sin depender del servicio externo.

- IDs normales -> aceptación simulada con acuse `SIM-*`.
- IDs que comienzan con `RECHAZO-` -> rechazo controlado de laboratorio.
- En `REAL`, `DGTD_API_URL` será la dirección que entregue el grupo responsable.

El sistema guarda estado, código de respuesta, mensaje, acuse y modo de integración.

## 3. BankyFinanzas / backend compartido

Se agregó el checkout suministrado para el proyecto, adaptado para que no utilice el comercio de otro grupo.

Variables:

```text
BANKY_URL=https://bankyfinanzas.netlify.app
BANKY_MERCHANT_ID=<ID DEL COMERCIO DE ESTE GRUPO>
BANKY_CURRENCY=CRC
```

El frontend consulta `/api/v1/config/public` y solo habilita un cobro real cuando existe `BANKY_MERCHANT_ID`. La integración valida `event.origin`, procesa `completed`, `rejected` y `cancelled`, y no recibe número completo de tarjeta ni CVV.

## 4. Validador XML

La validación local usa `src/main/resources/xsd/factura-electronica-4.3-base.xsd` en tres momentos:

1. al generar la factura;
2. al consultar el XML;
3. antes de enviar a DGTD.

El XSD incluido sigue siendo **base de laboratorio**. Debe sustituirse/ajustarse al XSD 4.3 final acordado por el profesor y los grupos antes de declarar compatibilidad oficial.

## 5. Integración con ParkSmart

Se deja fuera de esta entrega intermedia para no interferir con los cambios que todavía realizan los compañeros. La integración posterior debe hacerse únicamente a través de la API REST de Facturación, sin acceso directo a su base de datos.
