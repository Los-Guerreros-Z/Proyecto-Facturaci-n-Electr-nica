package domain.repository;

import domain.entity.Factura;
import java.util.List;
import java.util.Optional;

public interface FacturaRepository {
    Factura save(Factura factura);
    Optional<Factura> findById(String id);
    List<Factura> findAll();
    // Aquí podés meter más métodos que ocupen los otros grupos, por ejemplo:
    List<Factura> findByEmisorNumeroIdentificacion(String numeroIdentificacion);
}