package infraestructure.persistence.repository;

import domain.entity.Factura;
import domain.repository.FacturaRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class FacturaRepositoryImpl implements FacturaRepository {

    private final SpringDataMongoFacturaRepository mongoRepository;

    // Inyección por constructor
    public FacturaRepositoryImpl(SpringDataMongoFacturaRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public Factura save(Factura factura) {
        return mongoRepository.save(factura);
    }

    @Override
    public Optional<Factura> findById(String id) {
        return mongoRepository.findById(id);
    }

    @Override
    public List<Factura> findAll() {
        return mongoRepository.findAll();
    }

    @Override
    public List<Factura> findByEmisorNumeroIdentificacion(String numeroIdentificacion) {
        // Si llegás a ocupar este método, Spring Data te permite crear consultas personalizadas fácilmente
        return mongoRepository.findAll().stream()
            .filter(f -> f.getEmisor() != null && numeroIdentificacion.equals(f.getEmisor().getNumeroIdentificacion()))
            .toList();
    }
}