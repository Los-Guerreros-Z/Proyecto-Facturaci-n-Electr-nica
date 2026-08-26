package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.facturacion.electronica.FacturacionApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FacturacionApplication.class)
@AutoConfigureMockMvc
class SeguridadYValidacionApiTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void configuracionPublicaNoExponeSecretos() throws Exception {
        mvc.perform(get("/api/v1/config/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankyCurrency").value("CRC"))
                .andExpect(jsonPath("$.bankyConfigurado").value(false))
                .andExpect(jsonPath("$.facturaBonitaConfigurada").value(true));
    }

    @Test
    void unaRutaProtegidaRechazaSolicitudSinToken() throws Exception {
        mvc.perform(get("/api/v1/clientes/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rechazaFacturaConTotalesInconsistentes() throws Exception {
        String token = registrarYObtenerToken(
                "Totales Test", "3101999901", "totales@facturas.local"
        );

        String factura = facturaJson(
                "F-TOTALES-001", "3101999901", "totales@facturas.local", 9999
        );

        mvc.perform(post("/api/v1/facturas/procesar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(factura))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El total del comprobante no coincide con la suma de las líneas de detalle"));
    }

    @Test
    void unClienteNoPuedeConsultarFacturaDeOtroCliente() throws Exception {
        String tokenA = registrarYObtenerToken(
                "Cliente A", "3101999902", "cliente.a@facturas.local"
        );
        String tokenB = registrarYObtenerToken(
                "Cliente B", "3101999903", "cliente.b@facturas.local"
        );

        mvc.perform(post("/api/v1/facturas/procesar")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(facturaJson("F-PRIVADA-001", "3101999902", "cliente.a@facturas.local", 1130)))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/facturas/F-PRIVADA-001")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }

    private String registrarYObtenerToken(String nombre, String identificacion, String correo) throws Exception {
        String registro = """
                {"nombreRazonSocial":"%s","tipoIdentificacion":"CEDULA_JURIDICA",
                 "numeroIdentificacion":"%s","correo":"%s","telefono":"88888888",
                 "password":"Prueba2026!"}
                """.formatted(nombre, identificacion, correo);

        String body = mvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registro))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = mapper.readTree(body);
        return json.get("accessToken").asText();
    }

    private String facturaJson(String id, String identificacion, String correo, double total) {
        return """
                {"id":"%s","fecha":"2026-08-25T16:00:00","moneda":"CRC",
                 "condicionVenta":"CONTADO","medioPago":"SINPE","tipoDocumento":"FACTURA_ELECTRONICA",
                 "emisor":{"nombre":"Cliente Prueba","identificacion":{"tipo":"CEDULA_JURIDICA","numero":"%s"},"correo":"%s"},
                 "receptor":{"nombre":"Receptor","identificacion":{"tipo":"CEDULA_FISICA","numero":"101110111"},"correo":"cliente@example.com"},
                 "items":[{"numeroLinea":1,"detalle":"Servicio","cantidad":1,"precioUnitario":1000,"descuento":0,"impuesto":{"tarifa":13},"subtotal":1000,"montoTotalLinea":1130}],
                 "totales":{"totalGravado":1000,"totalExento":0,"totalDescuentos":0,"totalImpuesto":130,"totalComprobante":%s}}
                """.formatted(id, identificacion, correo, total);
    }
}
