package domain.repository;

import domain.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, String> {
    List<Factura> findByClienteIdOrderByFechaRegistroDesc(Long clienteId);
    List<Factura> findByEmisorNumeroIdentificacion(String numeroIdentificacion);
}
