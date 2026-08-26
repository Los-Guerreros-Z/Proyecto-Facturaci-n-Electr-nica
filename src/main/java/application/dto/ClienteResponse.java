package application.dto;

import domain.entity.Cliente;
import domain.enums.EstadoCliente;
import domain.enums.Rol;
import domain.enums.TipoIdentificacion;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        String nombreRazonSocial,
        TipoIdentificacion tipoIdentificacion,
        String numeroIdentificacion,
        String correo,
        String telefono,
        Rol rol,
        EstadoCliente estado,
        LocalDateTime fechaRegistro
) {
    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(), cliente.getNombreRazonSocial(), cliente.getTipoIdentificacion(),
                cliente.getNumeroIdentificacion(), cliente.getCorreo(), cliente.getTelefono(),
                cliente.getRol(), cliente.getEstado(), cliente.getFechaRegistro()
        );
    }
}
