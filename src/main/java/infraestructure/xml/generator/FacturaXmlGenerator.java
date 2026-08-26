package infraestructure.xml.generator;

import domain.entity.Factura;
import domain.entity.FacturaDetalle;
import org.springframework.stereotype.Component;

@Component
public class FacturaXmlGenerator {
    public String generarXml(Factura factura) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<FacturaElectronica xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-properties/v4.3\">\n");
        tag(xml, 1, "Clave", factura.getId());
        tag(xml, 1, "NumeroConsecutivo", factura.getId());
        tag(xml, 1, "FechaEmision", factura.getFecha());
        tag(xml, 1, "CondicionVenta", factura.getCondicionVenta());
        tag(xml, 1, "MedioPago", factura.getMedioPago());

        xml.append("  <Emisor>\n");
        tag(xml, 2, "Nombre", factura.getEmisor().getNombre());
        xml.append("    <Identificacion>\n");
        tag(xml, 3, "Tipo", factura.getEmisor().getTipoIdentificacion());
        tag(xml, 3, "Numero", factura.getEmisor().getNumeroIdentificacion());
        xml.append("    </Identificacion>\n");
        tag(xml, 2, "CorreoElectronico", factura.getEmisor().getCorreo());
        xml.append("  </Emisor>\n");

        xml.append("  <Receptor>\n");
        tag(xml, 2, "Nombre", factura.getReceptor().getNombre());
        xml.append("    <Identificacion>\n");
        tag(xml, 3, "Tipo", factura.getReceptor().getTipoIdentificacion());
        tag(xml, 3, "Numero", factura.getReceptor().getNumeroIdentificacion());
        xml.append("    </Identificacion>\n");
        tag(xml, 2, "CorreoElectronico", factura.getReceptor().getCorreo());
        xml.append("  </Receptor>\n");

        xml.append("  <DetalleServicio>\n");
        for (FacturaDetalle item : factura.getItems()) {
            xml.append("    <LineaDetalle>\n");
            tag(xml, 3, "NumeroLinea", item.getNumeroLinea());
            tag(xml, 3, "Detalle", item.getDetalle());
            tag(xml, 3, "Cantidad", item.getCantidad());
            tag(xml, 3, "PrecioUnitario", item.getPrecioUnitario());
            tag(xml, 3, "Descuento", item.getDescuento());
            tag(xml, 3, "TarifaImpuesto", item.getTarifaImpuesto());
            tag(xml, 3, "SubTotal", item.getSubtotal());
            tag(xml, 3, "MontoTotalLinea", item.getMontoTotalLinea());
            xml.append("    </LineaDetalle>\n");
        }
        xml.append("  </DetalleServicio>\n");

        xml.append("  <ResumenFactura>\n");
        tag(xml, 2, "Moneda", factura.getMoneda());
        tag(xml, 2, "TotalGravado", factura.getTotalGravado());
        tag(xml, 2, "TotalExento", factura.getTotalExento());
        tag(xml, 2, "TotalDescuentos", factura.getTotalDescuentos());
        tag(xml, 2, "TotalImpuesto", factura.getTotalImpuesto());
        tag(xml, 2, "TotalComprobante", factura.getTotalComprobante());
        xml.append("  </ResumenFactura>\n");
        xml.append("</FacturaElectronica>");
        return xml.toString();
    }

    private void tag(StringBuilder xml, int indent, String name, Object value) {
        String spaces = "  ".repeat(indent);
        xml.append(spaces).append('<').append(name).append('>')
                .append(escape(value == null ? "" : String.valueOf(value)))
                .append("</").append(name).append(">\n");
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
