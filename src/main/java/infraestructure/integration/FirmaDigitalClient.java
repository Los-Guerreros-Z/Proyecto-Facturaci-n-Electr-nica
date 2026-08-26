package infraestructure.integration;

import domain.entity.Cliente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FirmaDigitalClient {
    private final String modo;
    private final String firmaUrl;

    public FirmaDigitalClient(@Value("${integraciones.modo:SIMULADO}") String modo,
                              @Value("${integraciones.firma-url:}") String firmaUrl) {
        this.modo = modo;
        this.firmaUrl = firmaUrl;
    }

    public FirmaResult prepararFirma(String xml, Cliente cliente) {
        if ("SIMULADO".equalsIgnoreCase(modo)) {
            // No se marca como firmada: el grupo responsable todavía no ha aplicado
            // una firma digital real. Se conserva el XML validado para poder probar
            // el resto del flujo sin falsear el estado de la factura.
            return new FirmaResult(false, xml,
                    "SIMULADO: firma/cifrado pendiente del servicio del grupo responsable");
        }
        if (firmaUrl == null || firmaUrl.isBlank()) {
            return new FirmaResult(false, xml, "Servicio de firma no configurado");
        }
        // Punto de integración preparado. Cuando el otro grupo publique el contrato definitivo,
        // aquí se sustituye por la llamada HTTP al servicio de firma/cifrado.
        return new FirmaResult(false, xml, "Contrato externo de firma pendiente de integración");
    }

    public record FirmaResult(boolean aplicada, String xmlResultado, String mensaje) {}
}
