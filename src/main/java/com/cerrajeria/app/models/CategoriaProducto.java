package com.cerrajeria.app.models;

import java.time.LocalDateTime;

/**
 * Clase modelo que representa una categoría de producto en la tabla 'categoria_producto'.
 * Mapea las columnas de la base de datos a atributos Java.
 */
public class CategoriaProducto {

    private int idCategoriaProducto;
    private String nombre;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private boolean activo; // Para el borrado lógico que decidimos implementar después

    // Constructor completo
    public CategoriaProducto(int idCategoriaProducto, String nombre,
                             LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion, boolean activo) {
        this.idCategoriaProducto = idCategoriaProducto;
        this.nombre = nombre;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.activo = activo;
    }

    // Constructor para crear nuevas categorías (sin ID ni fechas iniciales)
    public CategoriaProducto(String nombre) {
        this.nombre = nombre;
        this.activo = true; // Por defecto, una nueva categoría está activa
    }

    // --- Getters y Setters ---
    // Puedes generarlos automáticamente en IntelliJ: Alt+Insert o Cmd+N -> Getter and Setter

    public int getIdCategoriaProducto() {
        return idCategoriaProducto;
    }

    public void setIdCategoriaProducto(int idCategoriaProducto) {
        this.idCategoriaProducto = idCategoriaProducto;
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
        return "CategoriaProducto{" +
                "idCategoriaProducto=" + idCategoriaProducto +
                ", nombre='" + nombre + '\'' +
                ", activo=" + activo +
                '}';
    }
}