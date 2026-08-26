package api.controller;

import application.dto.ClienteResponse;
import application.dto.ClienteUpdateRequest;
import application.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {
    private final ClienteService clienteService;
    public ClienteController(ClienteService clienteService) { this.clienteService = clienteService; }

    @GetMapping("/me")
    public ResponseEntity<ClienteResponse> me(Authentication auth) {
        return ResponseEntity.ok(clienteService.obtenerPorCorreo(auth.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<ClienteResponse> actualizar(@Valid @RequestBody ClienteUpdateRequest request, Authentication auth) {
        return ResponseEntity.ok(clienteService.actualizar(auth.getName(), request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> desactivar(Authentication auth) {
        clienteService.desactivar(auth.getName());
        return ResponseEntity.noContent().build();
    }
}
