# Facturas con múltiples líneas

El formulario web de FacturaSmart permite agregar varios productos o servicios antes de generar el comprobante.

Flujo:
1. Complete los datos generales y del receptor.
2. Escriba descripción, cantidad, precio unitario e IVA de un producto o servicio.
3. Pulse **Agregar línea**.
4. Repita el proceso para todas las líneas necesarias. Cada línea puede editarse o eliminarse antes de emitir la factura.
5. El portal calcula subtotal gravado, subtotal exento, IVA y total general.
6. Pulse **Generar factura** cuando la lista esté completa.

El frontend envía todas las líneas en `items[]`, numeradas consecutivamente. El backend existente procesa la lista completa, valida los totales, genera el XML/XSD, mantiene el PDF local y permite consumir Factura Bonita. El pago Banky se realiza posteriormente sobre el total completo de la factura.

## BankyFinanzas

El código no incluye un identificador de comercio real. Para uso local configure `BANKY_MERCHANT_ID` o establezca temporalmente `integraciones.banky-merchant-id` en `application.properties`. No se deben guardar números completos de tarjeta ni CVV en FacturaSmart.
