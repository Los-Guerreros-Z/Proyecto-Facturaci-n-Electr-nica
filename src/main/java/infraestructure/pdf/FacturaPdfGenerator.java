package infraestructure.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import domain.entity.Factura;
import domain.entity.FacturaDetalle;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class FacturaPdfGenerator {
    public byte[] generar(Factura factura) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
        document.add(new Paragraph("FACTURA ELECTRONICA - " + factura.getId()));
        document.add(new Paragraph("Emisor: " + factura.getEmisor().getNombre()));
        document.add(new Paragraph("Receptor: " + factura.getReceptor().getNombre()));
        document.add(new Paragraph("Fecha: " + factura.getFecha()));
        document.add(new Paragraph("Estado: " + factura.getEstado()));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(5);
        table.addCell("Linea"); table.addCell("Detalle"); table.addCell("Cantidad"); table.addCell("Precio"); table.addCell("Total");
        for (FacturaDetalle d : factura.getItems()) {
            table.addCell(String.valueOf(d.getNumeroLinea()));
            table.addCell(d.getDetalle());
            table.addCell(String.valueOf(d.getCantidad()));
            table.addCell(String.valueOf(d.getPrecioUnitario()));
            table.addCell(String.valueOf(d.getMontoTotalLinea()));
        }
        document.add(table);
        document.add(new Paragraph("Total comprobante: " + factura.getTotalComprobante() + " " + factura.getMoneda()));
        document.add(new Paragraph("Documento generado para consulta; no editable desde el sistema."));
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible generar el PDF de la factura", e);
        }
    }
}
