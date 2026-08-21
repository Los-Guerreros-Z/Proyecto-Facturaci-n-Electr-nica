package infraestructure.persistence.repository;

import domain.entity.Factura;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

// Esta interfaz hereda todo el poder de Spring Data Mongo de forma nativa
@Repository
interface SpringDataMongoFacturaRepository extends MongoRepository<Factura, String> {
}