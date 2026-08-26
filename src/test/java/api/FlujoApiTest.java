package api;

import com.facturacion.electronica.FacturacionApplication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = FacturacionApplication.class)
@AutoConfigureMockMvc
class FlujoApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void flujoPrincipalFunciona() throws Exception {
        String registro = """
                {"nombreRazonSocial":"ParkSmart Test","tipoIdentificacion":"CEDULA_JURIDICA",
                 "numeroIdentificacion":"3101999998","correo":"test@parksmart.local",
                 "telefono":"88888888","password":"ParkSmart2026!"}
                """;
        String authBody = mvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON).content(registro))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode auth = mapper.readTree(authBody);
        String token = auth.get("accessToken").asText();

        mvc.perform(get("/api/v1/clientes/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.numeroIdentificacion").value("3101999998"));

        String factura = """
                {"id":"F-TEST-001","fecha":"2026-08-24T20:00:00","moneda":"CRC",
                 "condicionVenta":"01","medioPago":"01","tipoDocumento":"FACTURA_ELECTRONICA",
                 "emisor":{"nombre":"ParkSmart Test","identificacion":{"tipo":"CEDULA_JURIDICA","numero":"3101999998"},"correo":"test@parksmart.local"},
                 "receptor":{"nombre":"Cliente Final","identificacion":{"tipo":"CEDULA_FISICA","numero":"101110111"},"correo":"cliente@example.com"},
                 "items":[{"numeroLinea":1,"detalle":"Servicio","cantidad":1,"precioUnitario":1000,"descuento":0,"impuesto":{"tarifa":13},"subtotal":1000,"montoTotalLinea":1130}],
                 "totales":{"totalGravado":1000,"totalExento":0,"totalDescuentos":0,"totalImpuesto":130,"totalComprobante":1130}}
                """;
        mvc.perform(post("/api/v1/facturas/procesar").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(factura))
                .andExpect(status().isOk()).andExpect(jsonPath("$.xmlValido").value(true));

        mvc.perform(get("/api/v1/facturas/F-TEST-001/xml").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));

        mvc.perform(get("/api/v1/facturas/F-TEST-001/pdf").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_PDF));

        mvc.perform(post("/api/v1/facturas/F-TEST-001/enviar").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("ACEPTADO"));
    }
}
