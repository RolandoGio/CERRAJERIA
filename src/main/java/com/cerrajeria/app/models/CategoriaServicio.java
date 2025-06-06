package com.cerrajeria.app.models;

import java.time.LocalDateTime;

/**
 * Clase modelo que representa una categoría de servicio en la tabla 'categoria_servicio'.
 * Mapea las columnas de la base de datos a atributos Java.
 */
public class CategoriaServicio {

    private int idCategoriaServicio;
    private String nombre;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private boolean activo; // Para el borrado lógico que decidimos implementar después

    // Constructor completo
    public CategoriaServicio(int idCategoriaServicio, String nombre,
                             LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion, boolean activo) {
        this.idCategoriaServicio = idCategoriaServicio;
        this.nombre = nombre;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.activo = activo;
    }

    // Constructor para crear nuevas categorías (sin ID ni fechas iniciales)
    public CategoriaServicio(String nombre) {
        this.nombre = nombre;
        this.activo = true; // Por defecto, una nueva categoría de servicio está activa
    }

    // --- Getters y Setters ---
    // (IntelliJ puede generarlos automáticamente: Alt+Insert o Cmd+N -> Getter and Setter)

    public int getIdCategoriaServicio() {
        return idCategoriaServicio;
    }

    public void setIdCategoriaServicio(int idCategoriaServicio) {
        this.idCategoriaServicio = idCategoriaServicio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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
        return "CategoriaServicio{" +
                "idCategoriaServicio=" + idCategoriaServicio +
                ", nombre='" + nombre + '\'' +
                ", activo=" + activo +
                '}';
    }
}