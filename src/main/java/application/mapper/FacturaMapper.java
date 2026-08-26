package application.mapper;

import application.dto.FacturaRequest;
import domain.entity.Emisor;
import domain.entity.Factura;
import domain.entity.FacturaDetalle;
import domain.entity.Receptor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Component
public class FacturaMapper {
    public Factura toEntity(FacturaRequest request) {
        Factura factura = new Factura();
        factura.setId(request.getId());
        factura.setFecha(parseFecha(request.getFecha()));
        factura.setMoneda(request.getMoneda());
        factura.setCondicionVenta(request.getCondicionVenta());
        factura.setMedioPago(request.getMedioPago());
        factura.setTipoDocumento(request.getTipoDocumento());

        Emisor emisor = new Emisor();
        emisor.setNombre(request.getEmisor().getNombre());
        emisor.setTipoIdentificacion(request.getEmisor().getIdentificacion().getTipo());
        emisor.setNumeroIdentificacion(request.getEmisor().getIdentificacion().getNumero());
        emisor.setCorreo(request.getEmisor().getCorreo());
        factura.setEmisor(emisor);

        Receptor receptor = new Receptor();
        receptor.setNombre(request.getReceptor().getNombre());
        receptor.setTipoIdentificacion(request.getReceptor().getIdentificacion().getTipo());
        receptor.setNumeroIdentificacion(request.getReceptor().getIdentificacion().getNumero());
        receptor.setCorreo(request.getReceptor().getCorreo());
        factura.setReceptor(receptor);

        List<FacturaDetalle> detalles = request.getItems().stream().map(itemReq -> {
            FacturaDetalle detalle = new FacturaDetalle();
            detalle.setNumeroLinea(itemReq.getNumeroLinea());
            detalle.setDetalle(itemReq.getDetalle());
            detalle.setCantidad(itemReq.getCantidad());
            detalle.setPrecioUnitario(itemReq.getPrecioUnitario());
            detalle.setDescuento(itemReq.getDescuento());
            detalle.setTarifaImpuesto(itemReq.getImpuesto() != null ? itemReq.getImpuesto().getTarifa() : 0.0);
            detalle.setSubtotal(itemReq.getSubtotal());
            detalle.setMontoTotalLinea(itemReq.getMontoTotalLinea());
            return detalle;
        }).toList();
        factura.setItems(detalles);

        factura.setTotalGravado(request.getTotales().getTotalGravado());
        factura.setTotalExento(request.getTotales().getTotalExento());
        factura.setTotalDescuentos(request.getTotales().getTotalDescuentos());
        factura.setTotalImpuesto(request.getTotales().getTotalImpuesto());
        factura.setTotalComprobante(request.getTotales().getTotalComprobante());
        return factura;
    }

    private LocalDateTime parseFecha(String value) {
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (Exception ignored) {
            return LocalDateTime.parse(value);
        }
    }
}
