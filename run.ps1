$ErrorActionPreference = "Stop"
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    throw "Maven no está disponible en PATH. En IntelliJ puede ejecutar FacturacionApplication directamente o instalar Maven."
}
mvn clean spring-boot:run
