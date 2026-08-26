package application.service;

import application.dto.ClienteResponse;
import application.dto.ClienteUpdateRequest;
import domain.entity.Cliente;
import domain.enums.EstadoCliente;
import domain.repository.ClienteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Cliente obtenerEntidadPorCorreo(String correo) {
        return clienteRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
    }

    public ClienteResponse obtenerPorCorreo(String correo) {
        return ClienteResponse.from(obtenerEntidadPorCorreo(correo));
    }

    @Transactional
    public ClienteResponse actualizar(String correoActual, ClienteUpdateRequest request) {
        Cliente cliente = obtenerEntidadPorCorreo(correoActual);

        if (request.getCorreo() != null && !request.getCorreo().equalsIgnoreCase(cliente.getCorreo())) {
            throw new IllegalArgumentException("El correo de inicio de sesión no se modifica desde este endpoint");
        }
        if (request.getNumeroIdentificacion() != null
                && !request.getNumeroIdentificacion().equals(cliente.getNumeroIdentificacion())
                && clienteRepository.existsByNumeroIdentificacion(request.getNumeroIdentificacion())) {
            throw new IllegalArgumentException("La identificación ya está registrada");
        }

        if (request.getNombreRazonSocial() != null && !request.getNombreRazonSocial().isBlank())
            cliente.setNombreRazonSocial(request.getNombreRazonSocial());
        if (request.getTipoIdentificacion() != null)
            cliente.setTipoIdentificacion(request.getTipoIdentificacion());
        if (request.getNumeroIdentificacion() != null && !request.getNumeroIdentificacion().isBlank())
            cliente.setNumeroIdentificacion(request.getNumeroIdentificacion());
        if (request.getTelefono() != null)
            cliente.setTelefono(request.getTelefono());
        if (request.getNuevaPassword() != null && !request.getNuevaPassword().isBlank()) {
            if (request.getNuevaPassword().length() < 8)
                throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres");
            cliente.setPasswordHash(passwordEncoder.encode(request.getNuevaPassword()));
        }
        return ClienteResponse.from(clienteRepository.save(cliente));
    }

    @Transactional
    public void desactivar(String correo) {
        Cliente cliente = obtenerEntidadPorCorreo(correo);
        cliente.setEstado(EstadoCliente.INACTIVO);
        clienteRepository.save(cliente);
    }
}
