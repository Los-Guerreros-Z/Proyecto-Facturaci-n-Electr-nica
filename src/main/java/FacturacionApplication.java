import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"api", "application", "domain", "infraestructure"})
@EnableMongoRepositories(basePackages = "infraestructure.persistence.repository") // <-- ESTA ES LA MAGIA
public class FacturacionApplication {

    public static void main(String[] args) {
        SpringApplication.run(FacturacionApplication.class, args);
        System.out.println("¡API de Facturación Electrónica iniciada pura vida! 🚀");
    }
}