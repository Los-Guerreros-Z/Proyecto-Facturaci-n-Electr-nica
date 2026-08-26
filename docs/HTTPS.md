# HTTPS

Para laboratorio se puede generar un PKCS12 local:

```powershell
keytool -genkeypair -alias facturacion -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore facturacion-local.p12 -validity 365 -dname "CN=localhost"
$env:SPRING_PROFILES_ACTIVE="https"
$env:SSL_KEYSTORE_PATH="file:./facturacion-local.p12"
$env:SSL_KEYSTORE_PASSWORD="LA_PASSWORD_ELEGIDA"
$env:JWT_SECRET="UN_SECRETO_LARGO_DE_AL_MENOS_32_CARACTERES"
mvn spring-boot:run
```

Para producción se recomienda terminar TLS en el proxy/plataforma y mantener las credenciales fuera del repositorio.
