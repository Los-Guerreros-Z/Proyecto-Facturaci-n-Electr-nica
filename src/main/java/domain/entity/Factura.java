package domain.entity;

import domain.enums.EstadoFactura;
import domain.enums.EstadoPago;
import domain.enums.TipoDocumento;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facturas")
public class Factura {
    @Id
    @Column(length = 60)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    private LocalDateTime fecha;
    private String moneda;
    private String condicionVenta;
    private String medioPago;

    @Enumerated(EnumType.STRING)
    private TipoDocumento tipoDocumento = TipoDocumento.FACTURA_ELECTRONICA;

    @Enumerated(EnumType.STRING)
    private EstadoFactura estado = EstadoFactura.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago")
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "codigo_transaccion_banky", length = 120)
    private String codigoTransaccionBanky;

    @Column(name = "marca_tarjeta", length = 40)
    private String marcaTarjeta;

    @Column(name = "ultimos_digitos_tarjeta", length = 8)
    private String ultimosDigitosTarjeta;

    @Column(name = "titular_tarjeta", length = 160)
    private String titularTarjeta;

    @Column(name = "monto_pagado")
    private Double montoPagado;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "nombre", column = @Column(name = "emisor_nombre")),
            @AttributeOverride(name = "tipoIdentificacion", column = @Column(name = "emisor_tipo_identificacion")),
            @AttributeOverride(name = "numeroIdentificacion", column = @Column(name = "emisor_numero_identificacion")),
            @AttributeOverride(name = "correo", column = @Column(name = "emisor_correo"))
    })
    private Emisor emisor;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "nombre", column = @Column(name = "receptor_nombre")),
            @AttributeOverride(name = "tipoIdentificacion", column = @Column(name = "receptor_tipo_identificacion")),
            @AttributeOverride(name = "numeroIdentificacion", column = @Column(name = "receptor_numero_identificacion")),
            @AttributeOverride(name = "correo", column = @Column(name = "receptor_correo"))
    })
    private Receptor receptor;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "factura_detalles", joinColumns = @JoinColumn(name = "factura_id"))
    private List<FacturaDetalle> items = new ArrayList<>();

    private Double totalGravado;
    private Double totalExento;
    private Double totalDescuentos;
    private Double totalImpuesto;
    private Double totalComprobante;

    @Lob
    private String xmlGenerado;

    @Column(nullable = false)
    private boolean xmlValido;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public String getCondicionVenta() { return condicionVenta; }
    public void setCondicionVenta(String condicionVenta) { this.condicionVenta = condicionVenta; }
    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String medioPago) { this.medioPago = medioPago; }
    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public EstadoFactura getEstado() { return estado; }
    public void setEstado(EstadoFactura estado) { this.estado = estado; }
    public EstadoPago getEstadoPago() { return estadoPago == null ? EstadoPago.PENDIENTE : estadoPago; }
    public void setEstadoPago(EstadoPago estadoPago) { this.estadoPago = estadoPago; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
    public String getCodigoTransaccionBanky() { return codigoTransaccionBanky; }
    public void setCodigoTransaccionBanky(String codigoTransaccionBanky) { this.codigoTransaccionBanky = codigoTransaccionBanky; }
    public String getMarcaTarjeta() { return marcaTarjeta; }
    public void setMarcaTarjeta(String marcaTarjeta) { this.marcaTarjeta = marcaTarjeta; }
    public String getUltimosDigitosTarjeta() { return ultimosDigitosTarjeta; }
    public void setUltimosDigitosTarjeta(String ultimosDigitosTarjeta) { this.ultimosDigitosTarjeta = ultimosDigitosTarjeta; }
    public String getTitularTarjeta() { return titularTarjeta; }
    public void setTitularTarjeta(String titularTarjeta) { this.titularTarjeta = titularTarjeta; }
    public Double getMontoPagado() { return montoPagado; }
    public void setMontoPagado(Double montoPagado) { this.montoPagado = montoPagado; }
    public Emisor getEmisor() { return emisor; }
    public void setEmisor(Emisor emisor) { this.emisor = emisor; }
    public Receptor getReceptor() { return receptor; }
    public void setReceptor(Receptor receptor) { this.receptor = receptor; }
    public List<FacturaDetalle> getItems() { return items; }
    public void setItems(List<FacturaDetalle> items) { this.items = items; }
    public Double getTotalGravado() { return totalGravado; }
    public void setTotalGravado(Double totalGravado) { this.totalGravado = totalGravado; }
    public Double getTotalExento() { return totalExento; }
    public void setTotalExento(Double totalExento) { this.totalExento = totalExento; }
    public Double getTotalDescuentos() { return totalDescuentos; }
    public void setTotalDescuentos(Double totalDescuentos) { this.totalDescuentos = totalDescuentos; }
    public Double getTotalImpuesto() { return totalImpuesto; }
    public void setTotalImpuesto(Double totalImpuesto) { this.totalImpuesto = totalImpuesto; }
    public Double getTotalComprobante() { return totalComprobante; }
    public void setTotalComprobante(Double totalComprobante) { this.totalComprobante = totalComprobante; }
    public String getXmlGenerado() { return xmlGenerado; }
    public void setXmlGenerado(String xmlGenerado) { this.xmlGenerado = xmlGenerado; }
    public boolean isXmlValido() { return xmlValido; }
    public void setXmlValido(boolean xmlValido) { this.xmlValido = xmlValido; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
