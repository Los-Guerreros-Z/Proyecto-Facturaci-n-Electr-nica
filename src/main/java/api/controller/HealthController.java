package api.controller;

import domain.enums.TipoIdentificacion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {
    private final String modo;
    public HealthController(@Value("${integraciones.modo:SIMULADO}") String modo) { this.modo = modo; }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "service", "Facturacion Electronica",
                "integraciones", modo
        ));
    }

    @GetMapping("/catalogos/tipos-identificacion")
    public ResponseEntity<TipoIdentificacion[]> tiposIdentificacion() {
        return ResponseEntity.ok(TipoIdentificacion.values());
    }
}
