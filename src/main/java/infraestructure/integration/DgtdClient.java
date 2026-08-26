package infraestructure.integration;

import domain.entity.Factura;
import domain.enums.EstadoEnvio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DgtdClient {

    private final String modo;
    private final String dgtdUrl;

    public DgtdClient(
            @Value("${integraciones.modo:SIMULADO}") String modo,
            @Value("${integraciones.dgtd-url:}") String dgtdUrl
    ) {
        this.modo = modo;
        this.dgtdUrl = dgtdUrl;
    }

    public DgtdResult enviar(Factura factura, String xml) {

        if ("SIMULADO".equalsIgnoreCase(modo)) {

            // Si el ID empieza con RECHAZO-, simula rechazo
            if (factura.getId() != null &&
                    factura.getId().toUpperCase().startsWith("RECHAZO-")) {

                return new DgtdResult(
                        EstadoEnvio.RECHAZADO,
                        "400",
                        "Factura rechazada por DGTD simulado",
                        null,
                        "SIMULADO"
                );
            }

            // En cualquier otro caso, simula aceptación
            String acuse = "SIM-" + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            return new DgtdResult(
                    EstadoEnvio.ACEPTADO,
                    "202",
                    "Factura recibida por DGTD simulado",
                    acuse,
                    "SIMULADO"
            );
        }

        if (dgtdUrl == null || dgtdUrl.isBlank()) {
            return new DgtdResult(
                    EstadoEnvio.PENDIENTE_INTEGRACION,
                    "PENDIENTE",
                    "DGTD externo no configurado",
                    null,
                    "REAL"
            );
        }

        // Punto listo para futura integración real con DGTD
        return new DgtdResult(
                EstadoEnvio.PENDIENTE_INTEGRACION,
                "PENDIENTE",
                "Contrato DGTD pendiente de integración",
                null,
                "REAL"
        );
    }

    public record DgtdResult(
            EstadoEnvio estado,
            String codigo,
            String mensaje,
            String numeroAcuse,
            String modo
    ) {}
}