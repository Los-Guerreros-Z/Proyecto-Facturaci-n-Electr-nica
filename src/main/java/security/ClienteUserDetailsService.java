package security;

import domain.entity.Cliente;
import domain.repository.ClienteRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ClienteUserDetailsService implements UserDetailsService {
    private final ClienteRepository clienteRepository;
    public ClienteUserDetailsService(ClienteRepository clienteRepository) { this.clienteRepository = clienteRepository; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Cliente cliente = clienteRepository.findByCorreoIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente no encontrado"));
        return new ClientePrincipal(cliente);
    }
}
