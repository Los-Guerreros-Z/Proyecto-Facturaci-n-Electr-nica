// Emisor.java
package domain.entity;

public class Emisor {
    private String nombre;
    private String tipoIdentificacion;
    private String numeroIdentificacion;
    private String correo;

    public Emisor() {}

    // Getters y Setters...

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public String getTipoIdentificacion(){
        return tipoIdentificacion;
    }

    public void setTipoIdentificacion(String tipoIdentificacion){
        this.tipoIdentificacion = tipoIdentificacion;
    }

    public String getCorreo(){
        return correo;
    }

    public void setCorreo(String correo){
        this.correo = correo;
    }
}