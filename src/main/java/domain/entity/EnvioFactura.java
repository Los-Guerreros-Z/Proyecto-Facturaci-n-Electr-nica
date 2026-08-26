package domain.entity;

import domain.enums.EstadoEnvio;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "envios_factura")
public class EnvioFactura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    private LocalDateTime fechaEnvio;

    @Enumerated(EnumType.STRING)
    private EstadoEnvio estado;

    private String codigoRespuesta;
    @Column(length = 500)
    private String mensajeRespuesta;
    private String numeroAcuse;
    private String modoIntegracion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getModoIntegracion() { return modoIntegracion; }
    public void setModoIntegracion(String modoIntegracion) { this.modoIntegracion = modoIntegracion; }
}
