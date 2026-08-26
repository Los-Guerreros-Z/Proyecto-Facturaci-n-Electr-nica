package domain.repository;

import domain.entity.EnvioFactura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnvioFacturaRepository extends JpaRepository<EnvioFactura, Long> {
    List<EnvioFactura> findByFacturaIdOrderByFechaEnvioDesc(String facturaId);
}
