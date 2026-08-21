package domain.enums;

import java.time.LocalDateTime;

import domain.entity.Factura;

public class EnvioFactura {

    private Long id;

    private Factura factura;

    private LocalDateTime fechaEnvio;

    private EstadoEnvio estado;

    private String codigoRespuesta;

    private String mensajeRespuesta;

    private String numeroAcuse;
}