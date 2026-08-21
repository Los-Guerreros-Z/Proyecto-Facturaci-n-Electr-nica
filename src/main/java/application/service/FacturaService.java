package application.service;

import application.dto.FacturaRequest;
import domain.entity.Factura;
import java.util.List;
import java.util.Optional;

public interface FacturaService {
    String procesarFacturaElectronica(FacturaRequest request);
    List<Factura> obtenerTodas();
    Optional<Factura> obtenerPorId(String id);
}