package application.dto;

import java.util.List;

public class FacturaRequest {
    private String id;
    private String fecha;
    private String moneda;
    private String condicionVenta;
    private String medioPago;
    private TerceroDTO emisor;
    private TerceroDTO receptor;
    private List<FacturaDetalleRequest> items;
    private TotalesDTO totales;

    // --- GETTERS Y SETTERS PRINCIPALES ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public String getCondicionVenta() { return condicionVenta; }
    public void setCondicionVenta(String condicionVenta) { this.condicionVenta = condicionVenta; }

    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String medioPago) { this.medioPago = medioPago; }

    public TerceroDTO getEmisor() { return emisor; }
    public void setEmisor(TerceroDTO emisor) { this.emisor = emisor; }

    public TerceroDTO getReceptor() { return receptor; }
    public void setReceptor(TerceroDTO receptor) { this.receptor = receptor; }

    public List<FacturaDetalleRequest> getItems() { return items; }
    public void setItems(List<FacturaDetalleRequest> items) { this.items = items; }

    public TotalesDTO getTotales() { return totales; }
    public void setTotales(TotalesDTO totales) { this.totales = totales; }


    // --- CLASES ANIDADAS ---

    public static class TerceroDTO {
        private String nombre;
        private IdentificacionDTO identificacion;
        private String correo;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public IdentificacionDTO getIdentificacion() { return identificacion; }
        public void setIdentificacion(IdentificacionDTO identificacion) { this.identificacion = identificacion; }

        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
    }

    public static class IdentificacionDTO {
        private String tipo;
        private String numero;

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }

        public String getNumero() { return numero; }
        public void setNumero(String numero) { this.numero = numero; }
    }

    public static class FacturaDetalleRequest {
        private Integer numeroLinea;
        private String detalle;
        private Integer cantidad;
        private Double precioUnitario;
        private Double descuento;
        private ImpuestoDTO impuesto;
        private Double subtotal;
        private Double montoTotalLinea;

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

        public ImpuestoDTO getImpuesto() { return impuesto; }
        public void setImpuesto(ImpuestoDTO impuesto) { this.impuesto = impuesto; }

        public Double getSubtotal() { return subtotal; }
        public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

        public Double getMontoTotalLinea() { return montoTotalLinea; }
        public void setMontoTotalLinea(Double montoTotalLinea) { this.montoTotalLinea = montoTotalLinea; }
    }

    public static class ImpuestoDTO {
        private Double tarifa;
        
        public Double getTarifa() { return tarifa; }
        public void setTarifa(Double tarifa) { this.tarifa = tarifa; }
    }

    public static class TotalesDTO {
        private double totalGravado;
        private double totalExento;
        private double totalDescuentos;
        private double totalImpuesto;
        private double totalComprobante;

        public double getTotalGravado() { return totalGravado; }
        public void setTotalGravado(double totalGravado) { this.totalGravado = totalGravado; }

        public double getTotalExento() { return totalExento; }
        public void setTotalExento(double totalExento) { this.totalExento = totalExento; }

        public double getTotalDescuentos() { return totalDescuentos; }
        public void setTotalDescuentos(double totalDescuentos) { this.totalDescuentos = totalDescuentos; }

        public double getTotalImpuesto() { return totalImpuesto; }
        public void setTotalImpuesto(double totalImpuesto) { this.totalImpuesto = totalImpuesto; }

        public double getTotalComprobante() { return totalComprobante; }
        public void setTotalComprobante(double totalComprobante) { this.totalComprobante = totalComprobante; }
    }
}