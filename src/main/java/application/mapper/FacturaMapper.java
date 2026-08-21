package application.mapper;

import application.dto.FacturaRequest;
import application.dto.FacturaRequest.FacturaDetalleRequest;
import domain.entity.Factura;
import domain.entity.FacturaDetalle;
import domain.entity.Emisor;
import domain.entity.Receptor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FacturaMapper {

    public Factura toEntity(FacturaRequest request) {
        Factura factura = new Factura();
        factura.setId(request.getId());
        
        // Parseo básico de ISO 8601 (manejando si viene con o sin la Z de UTC)
        if (request.getFecha() != null) {
            factura.setFecha(LocalDateTime.parse(request.getFecha().replace("Z", ""))); 
        }
        
        factura.setMoneda(request.getMoneda());
        factura.setCondicionVenta(request.getCondicionVenta());
        factura.setMedioPago(request.getMedioPago());

        // Mapear Emisor
        if (request.getEmisor() != null) {
            Emisor emisor = new Emisor();
            emisor.setNombre(request.getEmisor().getNombre());
            emisor.setTipoIdentificacion(request.getEmisor().getIdentificacion().getTipo());
            emisor.setNumeroIdentificacion(request.getEmisor().getIdentificacion().getNumero());
            emisor.setCorreo(request.getEmisor().getCorreo());
            factura.setEmisor(emisor);
        }

        // Mapear Receptor
        if (request.getReceptor() != null) {
            Receptor receptor = new Receptor();
            receptor.setNombre(request.getReceptor().getNombre());
            receptor.setTipoIdentificacion(request.getReceptor().getIdentificacion().getTipo());
            receptor.setNumeroIdentificacion(request.getReceptor().getIdentificacion().getNumero());
            receptor.setCorreo(request.getReceptor().getCorreo());
            factura.setReceptor(receptor);
        }

        // Mapear Items (FacturaDetalle)
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<FacturaDetalle> detalles = request.getItems().stream().map(itemReq -> {
                FacturaDetalle detalle = new FacturaDetalle();
                detalle.setNumeroLinea(itemReq.getNumeroLinea());
                detalle.setDetalle(itemReq.getDetalle());
                detalle.setCantidad(itemReq.getCantidad());
                detalle.setPrecioUnitario(itemReq.getPrecioUnitario());
                detalle.setDescuento(itemReq.getDescuento());
                
                // Mapear la tarifa del impuesto que viene anidada
                if (itemReq.getImpuesto() != null) {
                    detalle.setTarifaImpuesto(itemReq.getImpuesto().getTarifa());
                }
                
                detalle.setSubtotal(itemReq.getSubtotal());
                detalle.setMontoTotalLinea(itemReq.getMontoTotalLinea());
                return detalle;
            }).collect(Collectors.toList());
            
            factura.setItems(detalles);
        }

        // Mapear Totales
        if (request.getTotales() != null) {
            factura.setTotalGravado(request.getTotales().getTotalGravado());
            factura.setTotalExento(request.getTotales().getTotalExento());
            factura.setTotalDescuentos(request.getTotales().getTotalDescuentos());
            factura.setTotalImpuesto(request.getTotales().getTotalImpuesto());
            factura.setTotalComprobante(request.getTotales().getTotalComprobante());
        }
        
        return factura;
    }
}