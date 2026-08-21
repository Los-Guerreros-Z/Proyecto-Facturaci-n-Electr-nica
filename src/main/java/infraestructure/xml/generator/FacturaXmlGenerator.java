package infraestructure.xml.generator;

import domain.entity.Factura;
import domain.entity.FacturaDetalle;
import org.springframework.stereotype.Component;

@Component
public class FacturaXmlGenerator {

    public String generarXml(Factura factura) {
        StringBuilder xml = new StringBuilder();
        
        // Encabezado XML y etiqueta raíz según versión 4.3
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<FacturaElectronica xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-properties/v4.3\">\n");
        
        // Datos generales
        xml.append("  <Clave>").append(factura.getId()).append("</Clave>\n");
        xml.append("  <NumeroConsecutivo>").append(factura.getId()).append("</NumeroConsecutivo>\n");
        xml.append("  <FechaEmision>").append(factura.getFecha().toString()).append("</FechaEmision>\n");
        xml.append("  <CondicionVenta>").append(factura.getCondicionVenta()).append("</CondicionVenta>\n");
        xml.append("  <MedioPago>").append(factura.getMedioPago()).append("</MedioPago>\n");

        // Nodo Emisor
        if (factura.getEmisor() != null) {
            xml.append("  <Emisor>\n");
            xml.append("    <Nombre>").append(factura.getEmisor().getNombre()).append("</Nombre>\n");
            xml.append("    <Identificacion>\n");
            xml.append("      <Tipo>").append(factura.getEmisor().getTipoIdentificacion()).append("</Tipo>\n");
            xml.append("      <Numero>").append(factura.getEmisor().getNumeroIdentificacion()).append("</Numero>\n");
            xml.append("    </Identificacion>\n");
            xml.append("    <CorreoElectronico>").append(factura.getEmisor().getCorreo()).append("</CorreoElectronico>\n");
            xml.append("  </Emisor>\n");
        }

        // Nodo Receptor
        if (factura.getReceptor() != null) {
            xml.append("  <Receptor>\n");
            xml.append("    <Nombre>").append(factura.getReceptor().getNombre()).append("</Nombre>\n");
            xml.append("    <Identificacion>\n");
            xml.append("      <Tipo>").append(factura.getReceptor().getTipoIdentificacion()).append("</Tipo>\n");
            xml.append("      <Numero>").append(factura.getReceptor().getNumeroIdentificacion()).append("</Numero>\n");
            xml.append("    </Identificacion>\n");
            xml.append("    <CorreoElectronico>").append(factura.getReceptor().getCorreo()).append("</CorreoElectronico>\n");
            xml.append("  </Receptor>\n");
        }

        // Nodo DetalleServicio (Los items)
        if (factura.getItems() != null && !factura.getItems().isEmpty()) {
            xml.append("  <DetalleServicio>\n");
            for (FacturaDetalle item : factura.getItems()) {
                xml.append("    <LineaDetalle>\n");
                xml.append("      <NumeroLinea>").append(item.getNumeroLinea()).append("</NumeroLinea>\n");
                xml.append("      <Detalle>").append(item.getDetalle()).append("</Detalle>\n");
                xml.append("      <Cantidad>").append(item.getCantidad()).append("</Cantidad>\n");
                xml.append("      <PrecioUnitario>").append(item.getPrecioUnitario()).append("</PrecioUnitario>\n");
                xml.append("      <MontoTotal>").append(item.getSubtotal()).append("</MontoTotal>\n");
                xml.append("      <SubTotal>").append(item.getSubtotal()).append("</SubTotal>\n");
                xml.append("      <MontoTotalLinea>").append(item.getMontoTotalLinea()).append("</MontoTotalLinea>\n");
                xml.append("    </LineaDetalle>\n");
            }
            xml.append("  </DetalleServicio>\n");
        }

        // Nodo ResumenFactura (Los totales)
        xml.append("  <ResumenFactura>\n");
        xml.append("    <TotalGravado>").append(factura.getTotalGravado()).append("</TotalGravado>\n");
        xml.append("    <TotalExento>").append(factura.getTotalExento()).append("</TotalExento>\n");
        xml.append("    <TotalDescuentos>").append(factura.getTotalDescuentos()).append("</TotalDescuentos>\n");
        xml.append("    <TotalImpuesto>").append(factura.getTotalImpuesto()).append("</TotalImpuesto>\n");
        xml.append("    <TotalComprobante>").append(factura.getTotalComprobante()).append("</TotalComprobante>\n");
        xml.append("  </ResumenFactura>\n");

        xml.append("</FacturaElectronica>");
        
        return xml.toString();
    }
}