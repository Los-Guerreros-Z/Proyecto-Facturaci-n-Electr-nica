package security;

import domain.entity.Cliente;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class ClientePrincipal implements UserDetails {
    private final Cliente cliente;

    public ClientePrincipal(Cliente cliente) { this.cliente = cliente; }
    public Cliente getCliente() { return cliente; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + cliente.getRol().name()));
    }
    @Override public String getPassword() { return cliente.getPasswordHash(); }
    @Override public String getUsername() { return cliente.getCorreo(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return cliente.getEstado().name().equals("ACTIVO"); }
}
