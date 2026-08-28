package application.dto;

import jakarta.validation.constraints.NotBlank;

public record FirmaHsmRequest(
        @NotBlank(message = "El XML firmado es obligatorio")
        String xmlFirmado,
        String hashDocumento,
        String serialCertificado
) {
}
