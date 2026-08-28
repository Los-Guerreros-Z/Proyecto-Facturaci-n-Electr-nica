# FacturaSmart - Empresa de Facturación Electrónica (ITI-523)

Servicio Spring Boot para el módulo **Empresa que vende el servicio de Facturación Electrónica**.

## Estado actual

- Registro/login de clientes con JWT y BCrypt.
- Perfil de cliente y persistencia relacional JPA.
- H2 persistente para laboratorio, MySQL opcional y PostgreSQL para despliegue en Render.
- API REST para recepción y consulta de facturas.
- Validación de duplicados, datos positivos y coherencia de totales.
- XML generado y validado mediante XSD base v4.3 del curso.
- Revalidación de XML al consultar y antes de enviar.
- PDF no editable.
- DGTD con modo simulado y cliente real para Mini Tributación Directa.
- Firma digital integrada con HSM Sign CR, manteniendo el PIN fuera de FacturaSmart.
- Integración BankyFinanzas preparada con `MERCHANT_ID` configurable.
- Integración con la API compartida de Factura Bonita para PDF/HTML visual, manteniendo el PDF local como respaldo.
- Frontend renovado inspirado visualmente en ParkSmart: login, registro, resumen, facturas, XML, PDF, DGTD, nueva factura, perfil e integraciones.
- Swagger, Postman y pruebas JUnit.

> El XSD incluido es una base del proyecto. No debe presentarse como XSD oficial completo de Hacienda hasta sustituirlo por el esquema 4.3 definitivo aprobado en clase. Firma Digital HSM y Mini Tributación ya cuentan con puntos de integración; ParkSmart queda como la integración siguiente.

## Ejecutar

Requiere Java 17+ y Maven 3.9+.

```powershell
mvn clean test
mvn spring-boot:run
```

Abrir:

- Portal: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/api/v1/health`

## Variables de integración

```text
JWT_SECRET=<secreto largo>
INTEGRACIONES_MODO=SIMULADO
HSM_SIGN_URL=https://hsm-sign-cr.onrender.com
DGTD_API_URL=https://mini-tributacion-backend.onrender.com
XML_VALIDATOR_API_URL=
BANKY_URL=https://bankyfinanzas.netlify.app
BANKY_MERCHANT_ID=<ID DEL COMERCIO DEL GRUPO>
BANKY_CURRENCY=CRC
FACTURA_BONITA_URL=https://proyecto-kn7p.onrender.com
FACTURA_BONITA_ORIGEN=facturasmart
```

Nunca reutilizar el `MERCHANT_ID` de otro grupo.

## MySQL opcional

```powershell
$env:SPRING_PROFILES_ACTIVE="mysql"
$env:DB_URL="jdbc:mysql://localhost:3306/facturacion_electronica?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Costa_Rica"
$env:DB_USER="facturacion_app"
$env:DB_PASSWORD="SU_PASSWORD"
$env:JWT_SECRET="UN_SECRETO_LARGO_DE_AL_MENOS_32_CARACTERES"
mvn spring-boot:run
```

## Documentación

- `docs/MANUAL_API.md`
- `docs/INTEGRACIONES_PENDIENTES.md`
- `docs/INTEGRACION_FACTURA_BONITA.md`
- `docs/INTEGRACION_HSM_SIGN_CR.md`
- `docs/INTEGRACION_FIRMA_Y_TRIBUTACION.md`
- `docs/VALIDACION_Y_PUESTA_EN_MARCHA.md`
- `docs/HTTPS.md`
- `docs/INFORME_BASE_PROYECTO.md`

### Pago asociado a factura

Las facturas ahora pueden pagarse directamente con BankyFinanzas desde su detalle. Un pago `completed` queda persistido como `PAGADA` con código de transacción, fecha, marca y últimos cuatro dígitos, sin almacenar datos completos de tarjeta. El estado de pago es independiente del estado DGTD.

## Mejora: facturas con múltiples líneas
El portal permite agregar, editar y eliminar varias líneas de productos/servicios antes de generar una factura. Los totales e IVA se calculan en pantalla y se envían como `items[]` al backend. Consulte `docs/MEJORA_MULTIPLES_LINEAS.md`.
