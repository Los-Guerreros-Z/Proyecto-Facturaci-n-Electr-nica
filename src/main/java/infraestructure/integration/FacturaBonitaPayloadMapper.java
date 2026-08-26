package infraestructure.integration;

import domain.entity.Factura;
import domain.entity.FacturaDetalle;
import domain.enums.TipoIdentificacion;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class FacturaBonitaPayloadMapper {

    private static final ZoneOffset COSTA_RICA = ZoneOffset.ofHours(-6);

    public Map<String, Object> convertir(Factura factura, String origen) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("perfilValidacion", "v44-visual");
        payload.put("origen", origen);
        payload.put("referenciaExterna", "factura:" + factura.getId());
        payload.put("fecha", fechaConZona(factura));
        payload.put("moneda", valorO(factura.getMoneda(), "CRC"));
        payload.put("condicionVenta", codigoCondicionVenta(factura.getCondicionVenta()));
        payload.put("detalleCondicionVenta", detalleCondicionVenta(factura.getCondicionVenta()));
        payload.put("medioPago", codigoMedioPago(factura.getMedioPago()));
        payload.put("plazoCredito", esCredito(factura.getCondicionVenta()) ? 30 : 0);

        payload.put("proveedorSistemas", Map.of(
                "nombre", "FacturaSmart - Servicio de Facturación Electrónica",
                "identificacion", factura.getCliente().getNumeroIdentificacion()
        ));

        payload.put("emisor", tercero(
                factura.getEmisor().getNombre(),
                factura.getEmisor().getTipoIdentificacion(),
                factura.getEmisor().getNumeroIdentificacion(),
                factura.getEmisor().getCorreo(),
                factura.getCliente().getTelefono()
        ));

        payload.put("receptor", tercero(
                factura.getReceptor().getNombre(),
                factura.getReceptor().getTipoIdentificacion(),
                factura.getReceptor().getNumeroIdentificacion(),
                factura.getReceptor().getCorreo(),
                null
        ));

        List<Map<String, Object>> items = new ArrayList<>();
        for (FacturaDetalle item : factura.getItems()) {
            items.add(item(item));
        }
        payload.put("items", items);
        payload.put("otrosCargos", List.of());
        payload.put("totales", totales(factura));
        payload.put("referencias", List.of());
        payload.put("otros", Map.of(
                "observaciones", "Comprobante generado por FacturaSmart.",
                "informacionAdicional", "Factura visual solicitada mediante la API REST compartida del curso."
        ));
        return payload;
    }

    private Map<String, Object> tercero(String nombre, TipoIdentificacion tipo, String numero,
                                         String correo, String telefono) {
        Map<String, Object> tercero = new LinkedHashMap<>();
        tercero.put("nombre", nombre);
        tercero.put("nombreComercial", nombre);
        tercero.put("identificacion", Map.of(
                "tipo", codigoIdentificacion(tipo),
                "numero", numero
        ));
        if (telefono != null && !telefono.isBlank()) {
            tercero.put("telefono", Map.of(
                    "codigoPais", "506",
                    "numero", telefono
            ));
        }
        if (correo != null && !correo.isBlank()) {
            tercero.put("correo", correo);
        }
        return tercero;
    }

    private Map<String, Object> item(FacturaDetalle item) {
        double cantidad = numero(item.getCantidad());
        double precio = numero(item.getPrecioUnitario());
        double montoTotal = cantidad * precio;
        double descuento = numero(item.getDescuento());
        double subtotal = numero(item.getSubtotal());
        double tarifa = numero(item.getTarifaImpuesto());
        double impuesto = redondear(subtotal * tarifa / 100.0);

        Map<String, Object> linea = new LinkedHashMap<>();
        linea.put("numeroLinea", item.getNumeroLinea());
        linea.put("cantidad", item.getCantidad());
        linea.put("unidadMedida", "Sp");
        linea.put("unidadMedidaComercial", "Servicio");
        linea.put("tipoTransaccion", "01");
        linea.put("detalle", item.getDetalle());
        linea.put("precioUnitario", precio);
        linea.put("montoTotal", redondear(montoTotal));
        linea.put("descuento", descuento);
        linea.put("descuentos", List.of());
        linea.put("subtotal", subtotal);
        linea.put("baseImponible", subtotal);
        linea.put("impuestoTarifa", tarifa);
        linea.put("impuesto", Map.of("tarifa", tarifa));

        if (tarifa > 0) {
            linea.put("impuestos", List.of(Map.of(
                    "codigo", "01",
                    "codigoTarifaIVA", codigoTarifaIva(tarifa),
                    "tarifa", tarifa,
                    "factorCalculoIVA", 1,
                    "monto", impuesto
            )));
        } else {
            linea.put("impuestos", List.of());
        }

        linea.put("impuestoNeto", impuesto);
        linea.put("montoTotalLinea", numero(item.getMontoTotalLinea()));
        return linea;
    }

    private Map<String, Object> totales(Factura factura) {
        double gravado = numero(factura.getTotalGravado());
        double exento = numero(factura.getTotalExento());
        double descuentos = numero(factura.getTotalDescuentos());
        double impuesto = numero(factura.getTotalImpuesto());
        double comprobante = numero(factura.getTotalComprobante());
        double venta = gravado + exento;
        double ventaNeta = venta - descuentos;

        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("totalServGravados", gravado);
        totales.put("totalServExentos", exento);
        totales.put("totalServExonerados", 0);
        totales.put("totalServNoSujetos", 0);
        totales.put("totalMercanciasGravadas", 0);
        totales.put("totalMercanciasExentas", 0);
        totales.put("totalMercanciasExoneradas", 0);
        totales.put("totalMercanciasNoSujetas", 0);
        totales.put("totalGravado", gravado);
        totales.put("totalExento", exento);
        totales.put("totalExonerado", 0);
        totales.put("totalNoSujeto", 0);
        totales.put("totalVenta", redondear(venta));
        totales.put("totalDescuentos", descuentos);
        totales.put("totalVentaNeta", redondear(ventaNeta));
        totales.put("totalImpuesto", impuesto);
        totales.put("totalIVADevuelto", 0);
        totales.put("totalOtrosCargos", 0);
        totales.put("totalComprobante", comprobante);
        totales.put("mediosPago", List.of(Map.of(
                "tipo", codigoMedioPago(factura.getMedioPago()),
                "total", comprobante
        )));
        return totales;
    }

    private String fechaConZona(Factura factura) {
        if (factura.getFecha() == null) {
            return OffsetDateTime.now(COSTA_RICA).toString();
        }
        return factura.getFecha().atOffset(COSTA_RICA).toString();
    }

    private String codigoIdentificacion(TipoIdentificacion tipo) {
        if (tipo == null) return "01";
        return switch (tipo) {
            case CEDULA_FISICA -> "01";
            case CEDULA_JURIDICA -> "02";
            case DIMEX -> "03";
            case NITE -> "04";
            case PASAPORTE -> "05";
            case EXTRANJERO -> "06";
        };
    }

    private String codigoCondicionVenta(String valor) {
        String normalizado = normalizar(valor);
        if (normalizado.matches("\\d{2}")) return normalizado;
        return switch (normalizado) {
            case "CREDITO", "CRÉDITO" -> "02";
            default -> "01";
        };
    }

    private String detalleCondicionVenta(String valor) {
        return esCredito(valor) ? "Crédito" : "Contado";
    }

    private boolean esCredito(String valor) {
        String normalizado = normalizar(valor);
        return normalizado.equals("02") || normalizado.equals("CREDITO") || normalizado.equals("CRÉDITO");
    }

    private String codigoMedioPago(String valor) {
        String normalizado = normalizar(valor);
        if (normalizado.matches("\\d{2}")) return normalizado;
        return switch (normalizado) {
            case "TARJETA", "TARJETA_CREDITO", "TARJETA_DEBITO", "BANKY" -> "02";
            case "CHEQUE" -> "03";
            case "TRANSFERENCIA", "SINPE", "SINPE_MOVIL" -> "04";
            case "RECAUDADO_POR_TERCEROS" -> "05";
            default -> "01";
        };
    }

    private String codigoTarifaIva(double tarifa) {
        if (Math.abs(tarifa - 13.0) < 0.001) return "08";
        if (Math.abs(tarifa - 4.0) < 0.001) return "04";
        if (Math.abs(tarifa - 2.0) < 0.001) return "03";
        if (Math.abs(tarifa - 1.0) < 0.001) return "02";
        if (Math.abs(tarifa) < 0.001) return "01";
        return "08";
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private String valorO(String valor, String defecto) {
        return valor == null || valor.isBlank() ? defecto : valor;
    }

    private double numero(Number valor) {
        return valor == null ? 0.0 : valor.doubleValue();
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
