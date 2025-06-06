package com.cerrajeria.app.models;

import java.math.BigDecimal; // Para manejar precios con precisión
import java.time.LocalDateTime;

/**
 * Clase modelo que representa un servicio en la tabla 'servicio'.
 * Mapea las columnas de la base de datos a atributos Java.
 */
public class Servicio {

    private int idServicio;
    private String nombre; // Columna 'nombre' agregada después
    private String descripcion;
    private BigDecimal precio; // Usar BigDecimal para dinero
    private int idCategoriaServicio; // Relación con categoria_servicio (agregada después)
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private boolean activo; // Columna 'activo' para borrado lógico (agregada después)

    // Constructor completo
    public Servicio(int idServicio, String nombre, String descripcion, BigDecimal precio, int idCategoriaServicio,
                    LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion, boolean activo) {
        this.idServicio = idServicio;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.idCategoriaServicio = idCategoriaServicio;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.activo = activo;
    }

    // Constructor para crear nuevos servicios (sin ID, fechas iniciales)
    public Servicio(String nombre, String descripcion, BigDecimal precio, int idCategoriaServicio) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.idCategoriaServicio = idCategoriaServicio;
        this.activo = true; // Por defecto activo al crear
    }

    // --- Getters y Setters ---
    // (IntelliJ puede generarlos automáticamente: Alt+Insert o Cmd+N -> Getter and Setter)

    public int getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(int idServicio) {
        this.idServicio = idServicio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getIdCategoriaServicio() {
        return idCategoriaServicio;
    }

    public void setIdCategoriaServicio(int idCategoriaServicio) {
        this.idCategoriaServicio = idCategoriaServicio;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Servicio{" +
                "idServicio=" + idServicio +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                ", idCategoriaServicio=" + idCategoriaServicio +
                ", activo=" + activo +
                '}';
    }
}