package infraestructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Component
public class HsmSignClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String baseUrl;

    public HsmSignClient(
            ObjectMapper objectMapper,
            @Value("${integraciones.hsm-url:https://hsm-sign-cr.onrender.com}") String baseUrl
    ) {
        this.objectMapper = objectMapper;
        this.baseUrl = limpiarBaseUrl(baseUrl);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public boolean configurado() {
        return baseUrl != null && !baseUrl.isBlank();
    }

    /**
     * Valida en HSM Sign CR un XML que ya fue firmado por el usuario en el popup.
     * FacturaSmart nunca recibe ni transporta el PIN de firma.
     */
    public ValidacionFirmaResult validarFirma(String xmlFirmado) {
        if (!configurado()) {
            throw new IntegracionExternaException("HSM Sign CR no está configurado");
        }
        if (xmlFirmado == null || xmlFirmado.isBlank()) {
            throw new IllegalArgumentException("El XML firmado es obligatorio");
        }

        try {
            String json = objectMapper.writeValueAsString(Map.of("xmlFirmado", xmlFirmado));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/documentos/validar"))
                    .timeout(Duration.ofSeconds(45))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            JsonNode body = parsearJson(response.body());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IntegracionExternaException(
                        "HSM Sign CR rechazó la validación (HTTP " + response.statusCode() + "): "
                                + texto(body, "error", texto(body, "motivo", "respuesta no disponible"))
                );
            }

            boolean esValida = body.path("esValida").asBoolean(false);
            return new ValidacionFirmaResult(
                    esValida,
                    texto(body, "signerId", null),
                    texto(body, "signerName", null),
                    texto(body, "algorithm", null),
                    texto(body, "certificateStatus", null),
                    texto(body, "signatureDate", null),
                    texto(body, "motivo", esValida ? null : "HSM Sign CR indicó que la firma no es válida")
            );
        } catch (IntegracionExternaException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IntegracionExternaException("La validación con HSM Sign CR fue interrumpida", ex);
        } catch (Exception ex) {
            throw new IntegracionExternaException("No fue posible comunicarse con HSM Sign CR", ex);
        }
    }

    private JsonNode parsearJson(String body) throws Exception {
        if (body == null || body.isBlank()) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(body);
    }

    private String texto(JsonNode node, String field, String defecto) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) return defecto;
        String text = value.asText();
        return text == null || text.isBlank() ? defecto : text;
    }

    private String limpiarBaseUrl(String url) {
        if (url == null) return "";
        String limpia = url.trim();
        while (limpia.endsWith("/")) {
            limpia = limpia.substring(0, limpia.length() - 1);
        }
        return limpia;
    }

    public record ValidacionFirmaResult(
            boolean esValida,
            String signerId,
            String signerName,
            String algorithm,
            String certificateStatus,
            String signatureDate,
            String motivo
    ) {
    }
}
