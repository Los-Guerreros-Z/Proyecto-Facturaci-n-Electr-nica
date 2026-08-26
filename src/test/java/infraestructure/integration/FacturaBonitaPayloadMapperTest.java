package infraestructure.integration;

import domain.entity.Cliente;
import domain.entity.Emisor;
import domain.entity.Factura;
import domain.entity.FacturaDetalle;
import domain.entity.Receptor;
import domain.enums.TipoIdentificacion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FacturaBonitaPayloadMapperTest {

    @Test
    void adaptaFacturaAlContratoCompartido() {
        Factura factura = facturaEjemplo();
        Map<String, Object> payload = new FacturaBonitaPayloadMapper().convertir(factura, "facturasmart");

        assertEquals("v44-visual", payload.get("perfilValidacion"));
        assertEquals("facturasmart", payload.get("origen"));
        assertEquals("factura:FB-001", payload.get("referenciaExterna"));
        assertEquals("CRC", payload.get("moneda"));
        assertEquals("01", payload.get("condicionVenta"));
        assertEquals("04", payload.get("medioPago"));
        assertNotNull(payload.get("items"));
        assertNotNull(payload.get("totales"));

        @SuppressWarnings("unchecked")
        Map<String, Object> emisor = (Map<String, Object>) payload.get("emisor");
        @SuppressWarnings("unchecked")
        Map<String, Object> identificacion = (Map<String, Object>) emisor.get("identificacion");
        assertEquals("02", identificacion.get("tipo"));

        @SuppressWarnings("unchecked")
        Map<String, Object> totales = (Map<String, Object>) payload.get("totales");
        assertEquals(1130.0, (Double) totales.get("totalComprobante"), 0.001);
    }

    private Factura facturaEjemplo() {
        Cliente cliente = new Cliente();
        cliente.setNombreRazonSocial("FacturaSmart Test");
        cliente.setTipoIdentificacion(TipoIdentificacion.CEDULA_JURIDICA);
        cliente.setNumeroIdentificacion("3101999998");
        cliente.setCorreo("test@facturas.local");
        cliente.setTelefono("88888888");

        Emisor emisor = new Emisor();
        emisor.setNombre("FacturaSmart Test");
        emisor.setTipoIdentificacion(TipoIdentificacion.CEDULA_JURIDICA);
        emisor.setNumeroIdentificacion("3101999998");
        emisor.setCorreo("test@facturas.local");

        Receptor receptor = new Receptor();
        receptor.setNombre("Cliente Final");
        receptor.setTipoIdentificacion(TipoIdentificacion.CEDULA_FISICA);
        receptor.setNumeroIdentificacion("101110111");
        receptor.setCorreo("cliente@example.com");

        FacturaDetalle detalle = new FacturaDetalle();
        detalle.setNumeroLinea(1);
        detalle.setDetalle("Servicio de facturación");
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(1000.0);
        detalle.setDescuento(0.0);
        detalle.setTarifaImpuesto(13.0);
        detalle.setSubtotal(1000.0);
        detalle.setMontoTotalLinea(1130.0);

        Factura factura = new Factura();
        factura.setId("FB-001");
        factura.setCliente(cliente);
        factura.setFecha(LocalDateTime.of(2026, 8, 25, 20, 0));
        factura.setMoneda("CRC");
        factura.setCondicionVenta("CONTADO");
        factura.setMedioPago("SINPE");
        factura.setEmisor(emisor);
        factura.setReceptor(receptor);
        factura.setItems(List.of(detalle));
        factura.setTotalGravado(1000.0);
        factura.setTotalExento(0.0);
        factura.setTotalDescuentos(0.0);
        factura.setTotalImpuesto(130.0);
        factura.setTotalComprobante(1130.0);
        return factura;
    }
}
