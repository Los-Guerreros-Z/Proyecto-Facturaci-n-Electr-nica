# Integración de pagos BankyFinanzas por factura

## Objetivo

Cada factura de FacturaSmart puede pagarse desde su pantalla de detalle. El estado del pago se maneja separado del estado de la factura electrónica/DGTD.

## Flujo

1. El cliente abre una factura pendiente.
2. Presiona **Pagar con Banky**.
3. FacturaSmart abre el checkout usando el `MERCHANT_ID` configurado, el ID de factura como `orderId` y el total real como `amount`.
4. BankyFinanzas devuelve `completed`, `rejected` o `cancelled` mediante `postMessage` y se valida el origen del mensaje.
5. Solo para `completed`, FacturaSmart registra el resultado en `POST /api/v1/facturas/{id}/pago-banky`.
6. La factura pasa de `PENDIENTE` a `PAGADA` en su estado de pago y muestra los datos no sensibles de la transacción.

## Datos persistidos

- Estado de pago (`PENDIENTE` / `PAGADA`).
- Fecha de pago.
- Código de transacción Banky.
- Marca de tarjeta.
- Últimos cuatro dígitos.
- Titular informado por Banky.
- Monto pagado, tomado del total de la factura en el servidor.

No se guarda número completo de tarjeta, CVV ni contraseña de Banky.

## Compatibilidad

El estado electrónico de la factura (`PENDIENTE_INTEGRACION`, `ACEPTADA`, `RECHAZADA`, etc.) no se modifica cuando se registra un pago. Esto permite que DGTD y pagos evolucionen independientemente.
