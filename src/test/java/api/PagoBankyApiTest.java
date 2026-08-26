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
class PagoBankyApiTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void registraPagoBankyYMarcaFacturaComoPagada() throws Exception {
        String registro = """
                {"nombreRazonSocial":"Pago Banky Test","tipoIdentificacion":"CEDULA_JURIDICA",
                 "numeroIdentificacion":"3101888801","correo":"pago.banky@facturas.local",
                 "telefono":"88887777","password":"Prueba2026!"}
                """;

        String authBody = mvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON).content(registro))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String token = mapper.readTree(authBody).get("accessToken").asText();

        String factura = """
                {"id":"F-PAGO-001","fecha":"2026-08-25T21:30:00","moneda":"CRC",
                 "condicionVenta":"CONTADO","medioPago":"TARJETA","tipoDocumento":"FACTURA_ELECTRONICA",
                 "emisor":{"nombre":"Pago Banky Test","identificacion":{"tipo":"CEDULA_JURIDICA","numero":"3101888801"},"correo":"pago.banky@facturas.local"},
                 "receptor":{"nombre":"Cliente Pago","identificacion":{"tipo":"CEDULA_FISICA","numero":"101110222"},"correo":"cliente.pago@example.com"},
                 "items":[{"numeroLinea":1,"detalle":"Servicio","cantidad":1,"precioUnitario":1000,"descuento":0,"impuesto":{"tarifa":13},"subtotal":1000,"montoTotalLinea":1130}],
                 "totales":{"totalGravado":1000,"totalExento":0,"totalDescuentos":0,"totalImpuesto":130,"totalComprobante":1130}}
                """;

        mvc.perform(post("/api/v1/facturas/procesar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(factura))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoPago").value("PENDIENTE"));

        String pago = """
                {"status":"completed","transactionCode":"BANKY-TEST-001",
                 "cardBrand":"VISA","cardLastFourDigits":"4242","cardholderName":"Mario Prueba"}
                """;

        mvc.perform(post("/api/v1/facturas/F-PAGO-001/pago-banky")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(pago))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoPago").value("PAGADA"))
                .andExpect(jsonPath("$.codigoTransaccionBanky").value("BANKY-TEST-001"))
                .andExpect(jsonPath("$.marcaTarjeta").value("VISA"))
                .andExpect(jsonPath("$.ultimosDigitosTarjeta").value("4242"))
                .andExpect(jsonPath("$.montoPagado").value(1130.0));

        mvc.perform(get("/api/v1/facturas/F-PAGO-001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoPago").value("PAGADA"));

        mvc.perform(post("/api/v1/facturas/F-PAGO-001/pago-banky")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(pago))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Esta factura ya se encuentra pagada"));
    }
}
