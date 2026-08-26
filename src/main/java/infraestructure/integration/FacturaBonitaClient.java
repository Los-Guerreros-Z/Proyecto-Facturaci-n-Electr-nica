package infraestructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.entity.Factura;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Component
public class FacturaBonitaClient {

    private final ObjectMapper objectMapper;
    private final FacturaBonitaPayloadMapper payloadMapper;
    private final HttpClient httpClient;
    private final String baseUrl;
    private final String origen;

    public FacturaBonitaClient(
            ObjectMapper objectMapper,
            FacturaBonitaPayloadMapper payloadMapper,
            @Value("${integraciones.factura-bonita-url:https://proyecto-kn7p.onrender.com}") String baseUrl,
            @Value("${integraciones.factura-bonita-origen:facturasmart}") String origen
    ) {
        this.objectMapper = objectMapper;
        this.payloadMapper = payloadMapper;
        this.baseUrl = limpiarBaseUrl(baseUrl);
        this.origen = origen == null || origen.isBlank() ? "facturasmart" : origen.trim();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public byte[] generarPdf(Factura factura) {
        String idRemoto = registrarORecuperar(factura);
        return descargarDocumento(idRemoto, "pdf");
    }

    public byte[] generarHtml(Factura factura) {
        String idRemoto = registrarORecuperar(factura);
        return descargarDocumento(idRemoto, "html");
    }

    public boolean configurado() {
        return baseUrl != null && !baseUrl.isBlank();
    }

    private String registrarORecuperar(Factura factura) {
        try {
            Map<String, Object> payload = payloadMapper.convertir(factura, origen);
            String json = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/facturas"))
                    .timeout(Duration.ofSeconds(45))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String existente = buscarPorReferencia(factura.getId());
                if (existente != null) return existente;
                throw new IntegracionExternaException(
                        "Factura Bonita rechazó la factura (HTTP " + response.statusCode() + "): "
                                + mensajeSeguro(response.body())
                );
            }

            String id = extraerId(response.body());
            if (id != null) return id;

            String location = response.headers().firstValue("Location").orElse(null);
            id = extraerIdLocation(location);
            if (id != null) return id;

            id = buscarPorReferencia(factura.getId());
            if (id != null) return id;

            throw new IntegracionExternaException(
                    "Factura Bonita recibió la factura, pero no devolvió un identificador utilizable"
            );
        } catch (IntegracionExternaException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IntegracionExternaException(
                    "No fue posible comunicarse con el servicio de Factura Bonita", ex
            );
        }
    }

    private byte[] descargarDocumento(String idRemoto, String formato) {
        try {
            String idSeguro = encodePath(idRemoto);
            URI uri = URI.create(baseUrl + "/api/documentos/facturas/" + idSeguro
                    + "?formato=" + formato + "&plantilla=auto");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(60))
                    .header("Accept", formato.equals("pdf") ? "application/pdf" : "text/html")
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofByteArray()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IntegracionExternaException(
                        "No se pudo generar la Factura Bonita (HTTP " + response.statusCode() + ")"
                );
            }
            return response.body();
        } catch (IntegracionExternaException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IntegracionExternaException(
                    "No fue posible descargar el documento de Factura Bonita", ex
            );
        }
    }

    private String buscarPorReferencia(String facturaId) {
        try {
            String referencia = "factura:" + facturaId;
            String query = "?origen=" + encodeQuery(origen)
                    + "&referenciaExterna=" + encodeQuery(referencia)
                    + "&limit=1&offset=0";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/facturas" + query))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) return null;
            return extraerId(response.body());
        } catch (Exception ignored) {
            return null;
        }
    }

    private String extraerId(String body) {
        if (body == null || body.isBlank()) return null;
        try {
            JsonNode root = objectMapper.readTree(body);
            String direct = valorId(root);
            if (direct != null) return direct;

            if (root.isArray() && !root.isEmpty()) {
                return valorId(root.get(0));
            }

            for (String contenedor : new String[]{"data", "factura", "resultado", "item", "items", "facturas"}) {
                JsonNode node = root.get(contenedor);
                if (node == null) continue;
                String value = valorId(node);
                if (value != null) return value;
                if (node.isArray() && !node.isEmpty()) {
                    value = valorId(node.get(0));
                    if (value != null) return value;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String valorId(JsonNode node) {
        if (node == null || node.isNull()) return null;
        for (String campo : new String[]{"id", "facturaId", "factura_id", "uuid"}) {
            JsonNode value = node.get(campo);
            if (value != null && !value.isNull()) {
                String text = value.asText();
                if (!text.isBlank()) return text;
            }
        }
        return null;
    }

    private String extraerIdLocation(String location) {
        if (location == null || location.isBlank()) return null;
        int slash = location.lastIndexOf('/');
        if (slash < 0 || slash == location.length() - 1) return null;
        String value = location.substring(slash + 1);
        int query = value.indexOf('?');
        return query >= 0 ? value.substring(0, query) : value;
    }

    private String mensajeSeguro(String body) {
        if (body == null || body.isBlank()) return "sin detalle";
        String texto = body.replaceAll("\\s+", " ").trim();
        return texto.length() > 300 ? texto.substring(0, 300) + "…" : texto;
    }

    private String encodeQuery(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String encodePath(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String limpiarBaseUrl(String value) {
        String result = value == null ? "" : value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
