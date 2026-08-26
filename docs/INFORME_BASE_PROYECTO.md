# Informe base - Empresa de Facturación Electrónica

## Descripción del proceso asignado
El módulo recibe facturas generadas por sitios clientes mediante API REST, identifica al contribuyente autenticado, registra la factura, genera y valida el XML, mantiene el historial de envío y permite consultar el comprobante, XML y PDF.

## Metodología aplicada
1. Separación por capas Controller / Service / Repository / Domain.
2. Persistencia relacional para clientes y facturas.
3. Autenticación JWT y BCrypt.
4. Generación XML y validación XSD.
5. PDF para consulta del cliente.
6. Adaptadores para firma/cifrado y DGTD.
7. Pruebas automatizadas y Postman.
8. Frontend de cliente para facturas, XML, PDF, DGTD e integraciones.
9. Checkout BankyFinanzas parametrizado para el comercio del grupo.

## Problemas encontrados y solución
- La versión inicial almacenaba credenciales de MongoDB en el código: se eliminó la credencial y se migró el núcleo a persistencia relacional configurable.
- No existía cliente/login: se incorporó registro, autenticación y perfil.
- El validador buscaba un XSD inexistente: se agregó un XSD base empaquetado como recurso y se integró la validación al flujo.
- Envío/firma dependían de otros grupos: se aislaron mediante adaptadores y modo SIMULADO para pruebas. La simulación de firma no declara una factura como firmada.
- El checkout Banky recibido estaba ligado al comercio Centrix: se parametrizó para exigir el `MERCHANT_ID` propio del grupo.
- El frontend inicial era mínimo: se amplió para cubrir las funciones principales de consulta y demostración.

## Evidencias pendientes para el documento final
Agregar capturas de Swagger, Postman, login web, factura XML, PDF, envío simulado, pruebas Maven y posteriormente integración real con los módulos de los demás grupos.
