# Validación y puesta en marcha

## Ejecutar

En IntelliJ puede ejecutarse `FacturacionApplication`. Por terminal, usar Maven 3.9+:

```powershell
mvn clean test
mvn spring-boot:run
```

Si Maven no está agregado al PATH, se puede usar el Maven incluido en IntelliJ.

## Pruebas incluidas

- Flujo principal: registro -> JWT -> perfil -> factura -> XML -> PDF -> DGTD simulado.
- XML válido e XML inválido contra el XSD.
- Ruta protegida sin token.
- Factura con totales inconsistentes.
- Aislamiento entre clientes: un cliente no puede consultar la factura de otro.
- Configuración pública sin secretos.

## Comprobaciones manuales recomendadas

1. Abrir `http://localhost:8080` y registrar/iniciar sesión.
2. Crear una factura desde el nuevo formulario web.
3. Consultarla desde **Facturas**.
4. Abrir XML y descargar PDF.
5. Enviar a DGTD simulado y comprobar el acuse.
6. Crear una factura `RECHAZO-001` para demostrar un rechazo controlado.
7. Importar la colección Postman y repetir el flujo.
8. Configurar `BANKY_MERCHANT_ID` únicamente cuando el comercio del grupo haya sido creado.

## Servicios externos

Firma/cifrado y DGTD permanecen desacoplados. No activar `INTEGRACIONES_MODO=REAL` hasta contar con los contratos REST definitivos de los grupos responsables.
