package application.dto;

import domain.entity.EnvioFactura;
import domain.enums.EstadoEnvio;

import java.time.LocalDateTime;

public record EnvioResponse(Long id, String facturaId, LocalDateTime fechaEnvio, EstadoEnvio estado,
                            String codigoRespuesta, String mensajeRespuesta, String numeroAcuse,
                            String modoIntegracion) {
    public static EnvioResponse from(EnvioFactura e) {
        return new EnvioResponse(e.getId(), e.getFactura().getId(), e.getFechaEnvio(), e.getEstado(),
                e.getCodigoRespuesta(), e.getMensajeRespuesta(), e.getNumeroAcuse(), e.getModoIntegracion());
    }
}
