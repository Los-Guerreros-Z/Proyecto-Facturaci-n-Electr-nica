package application.dto;

import domain.entity.Factura;
import domain.enums.EstadoFactura;
import domain.enums.EstadoPago;

import java.time.LocalDateTime;

public record FacturaResponse(
        String id,
        Long clienteId,
        LocalDateTime fecha,
        String emisor,
        String receptor,
        String moneda,
        Double totalComprobante,
        EstadoFactura estado,
        EstadoPago estadoPago,
        LocalDateTime fechaPago,
        String codigoTransaccionBanky,
        String marcaTarjeta,
        String ultimosDigitosTarjeta,
        String titularTarjeta,
        Double montoPagado,
        boolean xmlValido,
        boolean firmaValida,
        String hashDocumento,
        String serialCertificado,
        String firmanteIdentificacion,
        String firmanteNombre,
        String algoritmoFirma,
        String estadoCertificado,
        String fechaFirmaHsm,
        LocalDateTime fechaRegistro
) {
    public static FacturaResponse from(Factura f) {
        return new FacturaResponse(
                f.getId(),
                f.getCliente().getId(),
                f.getFecha(),
                f.getEmisor() != null ? f.getEmisor().getNombre() : null,
                f.getReceptor() != null ? f.getReceptor().getNombre() : null,
                f.getMoneda(),
                f.getTotalComprobante(),
                f.getEstado(),
                f.getEstadoPago(),
                f.getFechaPago(),
                f.getCodigoTransaccionBanky(),
                f.getMarcaTarjeta(),
                f.getUltimosDigitosTarjeta(),
                f.getTitularTarjeta(),
                f.getMontoPagado(),
                f.isXmlValido(),
                f.isFirmaValida(),
                f.getHashDocumento(),
                f.getSerialCertificado(),
                f.getFirmanteIdentificacion(),
                f.getFirmanteNombre(),
                f.getAlgoritmoFirma(),
                f.getEstadoCertificado(),
                f.getFechaFirmaHsm(),
                f.getFechaRegistro()
        );
    }
}
