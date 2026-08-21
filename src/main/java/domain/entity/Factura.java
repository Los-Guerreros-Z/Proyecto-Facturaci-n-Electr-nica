package domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "facturas")
public class Factura {

    @Id
    private String id; // Tomará el valor de tu JSON (ej: "F-0001") 

    private LocalDateTime fecha;
    private String moneda;
    private String condicionVenta;
    private String medioPago;
    
    // En Mongo, estos objetos se guardan incrustados directamente
    private Emisor emisor;
    private Receptor receptor;
    private List<FacturaDetalle> items;

    // Totales de la factura
    private Double totalGravado;
    private Double totalExento;
    private Double totalDescuentos;
    private Double totalImpuesto;
    private Double totalComprobante;

    // Constructores
    public Factura() {}

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public String getCondicionVenta() { return condicionVenta; }
    public void setCondicionVenta(String condicionVenta) { this.condicionVenta = condicionVenta; }

    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String medioPago) { this.medioPago = medioPago; }

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
}