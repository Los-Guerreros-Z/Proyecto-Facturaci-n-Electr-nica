package application.service;

import application.dto.AuthResponse;
import application.dto.ClienteResponse;
import application.dto.LoginRequest;
import application.dto.RegistroClienteRequest;
import domain.entity.Cliente;
import domain.enums.EstadoCliente;
import domain.enums.Rol;
import domain.repository.ClienteRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import security.JwtService;

@Service
public class AuthService {
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse registrar(RegistroClienteRequest request) {
        if (clienteRepository.existsByCorreoIgnoreCase(request.getCorreo())) {
            throw new IllegalArgumentException("Ya existe un cliente con ese correo");
        }
        if (clienteRepository.existsByNumeroIdentificacion(request.getNumeroIdentificacion())) {
            throw new IllegalArgumentException("Ya existe un cliente con ese número de identificación");
        }

        Cliente cliente = new Cliente();
        cliente.setNombreRazonSocial(request.getNombreRazonSocial());
        cliente.setTipoIdentificacion(request.getTipoIdentificacion());
        cliente.setNumeroIdentificacion(request.getNumeroIdentificacion());
        cliente.setCorreo(request.getCorreo().trim().toLowerCase());
        cliente.setTelefono(request.getTelefono());
        cliente.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        cliente.setRol(Rol.CLIENTE);
        cliente.setEstado(EstadoCliente.ACTIVO);
        cliente = clienteRepository.save(cliente);

        String token = jwtService.generarToken(cliente);
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds(), ClienteResponse.from(cliente));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo().trim().toLowerCase(), request.getPassword())
        );
        Cliente cliente = clienteRepository.findByCorreoIgnoreCase(request.getCorreo())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));
        String token = jwtService.generarToken(cliente);
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds(), ClienteResponse.from(cliente));
    }
}
