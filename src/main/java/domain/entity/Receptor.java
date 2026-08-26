package domain.entity;

import domain.enums.TipoIdentificacion;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class Receptor {
    private String nombre;
    @Enumerated(EnumType.STRING)
    private TipoIdentificacion tipoIdentificacion;
    private String numeroIdentificacion;
    private String correo;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoIdentificacion getTipoIdentificacion() { return tipoIdentificacion; }
    public void setTipoIdentificacion(TipoIdentificacion tipoIdentificacion) { this.tipoIdentificacion = tipoIdentificacion; }
    public String getNumeroIdentificacion() { return numeroIdentificacion; }
    public void setNumeroIdentificacion(String numeroIdentificacion) { this.numeroIdentificacion = numeroIdentificacion; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}
