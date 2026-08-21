package domain.entity;

import java.time.LocalDateTime;
import domain.enums.EstadoEnvio;

public class EnvioFactura {

    private String id; // Recordá que cambiamos a String para Mongo
    private Factura factura;
    private LocalDateTime fechaEnvio;
    private EstadoEnvio estado;
    private String codigoRespuesta;
    private String mensajeRespuesta;
    private String numeroAcuse;

    // Constructores
    public EnvioFactura() {}

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Factura getFactura() { return factura; }
    public void setFactura(Factura factura) { this.factura = factura; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public EstadoEnvio getEstado() { return estado; }
    public void setEstado(EstadoEnvio estado) { this.estado = estado; }

    public String getCodigoRespuesta() { return codigoRespuesta; }
    public void setCodigoRespuesta(String codigoRespuesta) { this.codigoRespuesta = codigoRespuesta; }

    public String getMensajeRespuesta() { return mensajeRespuesta; }
    public void setMensajeRespuesta(String mensajeRespuesta) { this.mensajeRespuesta = mensajeRespuesta; }

    public String getNumeroAcuse() { return numeroAcuse; }
    public void setNumeroAcuse(String numeroAcuse) { this.numeroAcuse = numeroAcuse; }
}