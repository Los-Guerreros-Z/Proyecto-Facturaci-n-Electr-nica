package infraestructure.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HsmSignClientTest {

    private HttpServer server;

    @AfterEach
    void detenerServidor() {
        if (server != null) server.stop(0);
    }

    @Test
    void debeValidarFirmaConServicioHsm() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/documentos/validar", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(body.contains("xmlFirmado"));

            byte[] response = """
                    {
                      "esValida": true,
                      "signerId": "1-1678-0456",
                      "signerName": "Usuario Prueba",
                      "algorithm": "SHA256withRSA",
                      "certificateStatus": "VIGENTE",
                      "signatureDate": "2026-08-28T01:00:00-06:00"
                    }
                    """.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        HsmSignClient client = new HsmSignClient(
                new ObjectMapper(),
                "http://localhost:" + server.getAddress().getPort()
        );

        HsmSignClient.ValidacionFirmaResult result =
                client.validarFirma("<FacturaElectronica><Signature/></FacturaElectronica>");

        assertTrue(result.esValida());
        assertEquals("1-1678-0456", result.signerId());
        assertEquals("Usuario Prueba", result.signerName());
        assertEquals("VIGENTE", result.certificateStatus());
    }

    @Test
    void debeConservarMotivoCuandoHsmIndicaFirmaInvalida() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/documentos/validar", exchange -> {
            byte[] response = """
                    {"esValida": false, "motivo": "La firma no corresponde al documento"}
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        HsmSignClient client = new HsmSignClient(
                new ObjectMapper(),
                "http://localhost:" + server.getAddress().getPort()
        );

        HsmSignClient.ValidacionFirmaResult result = client.validarFirma("<xml/>");

        assertFalse(result.esValida());
        assertEquals("La firma no corresponde al documento", result.motivo());
    }
}
