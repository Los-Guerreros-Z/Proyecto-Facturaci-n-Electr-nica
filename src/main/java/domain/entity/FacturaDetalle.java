package domain.entity;

public class FacturaDetalle {
    private Integer numeroLinea;
    private String detalle;
    private Integer cantidad;
    private Double precioUnitario;
    private Double descuento;
    private Double tarifaImpuesto; // Mapeado de impuesto.tarifa [cite: 112]
    private Double subtotal;
    private Double montoTotalLinea;

    public FacturaDetalle() {}

    // Getters y Setters
    public Integer getNumeroLinea() { return numeroLinea; }
    public void setNumeroLinea(Integer numeroLinea) { this.numeroLinea = numeroLinea; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }

    public Double getDescuento() { return descuento; }
    public void setDescuento(Double descuento) { this.descuento = descuento; }

    public Double getTarifaImpuesto() { return tarifaImpuesto; }
    public void setTarifaImpuesto(Double tarifaImpuesto) { this.tarifaImpuesto = tarifaImpuesto; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Double getMontoTotalLinea() { return montoTotalLinea; }
    public void setMontoTotalLinea(Double montoTotalLinea) { this.montoTotalLinea = montoTotalLinea; }
}