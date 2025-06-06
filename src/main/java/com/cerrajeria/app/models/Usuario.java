package com.cerrajeria.app.models;

import java.time.LocalDateTime; // Usaremos LocalDateTime para manejar las fechas

/**
 * Clase modelo que representa un usuario en la tabla 'usuario'.
 * Mapea las columnas de la base de datos a atributos Java.
 */
public class Usuario {

    private int idUsuario;
    private String nombre;
    private String nombreUsuario; // 'usuario' en DB para evitar conflicto con la clase Usuario
    private String contrasena;
    private String rol;
    private boolean activo; // BIT en DB se mapea a boolean
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String codigo; // Columna 'codigo' que agregamos

    // Constructor completo
    public Usuario(int idUsuario, String nombre, String nombreUsuario, String contrasena, String rol,
                   boolean activo, LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion, String codigo) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.codigo = codigo;
    }

    // Constructor para crear nuevos usuarios (sin ID, fechaCreacion, fechaActualizacion, codigo iniciales)
    public Usuario(String nombre, String nombreUsuario, String contrasena, String rol) {
        this.nombre = nombre;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.activo = true; // Por defecto activo al crear
        // Las fechas y el código serán establecidos por la DB o en el DAO/servicio
    }

    // --- Getters y Setters ---
    // (IntelliJ puede generarlos automáticamente: Alt+Insert o Cmd+N -> Getter and Setter)

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "idUsuario=" + idUsuario +
                ", nombre='" + nombre + '\'' +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", rol='" + rol + '\'' +
                ", activo=" + activo +
                ", codigo='" + codigo + '\'' +
                '}';
    }
}