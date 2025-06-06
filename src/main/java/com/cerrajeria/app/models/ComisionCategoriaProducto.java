package com.cerrajeria.app.models;

import java.time.LocalDateTime;

/**
 * Clase modelo que representa la configuración de comisión por categoría de producto
 * en la tabla 'comision_categoria_producto'.
 */
public class ComisionCategoriaProducto {

    private int idComisionCategoriaProducto; // id_com_cat_pro en DB
    private int idCategoriaProducto;         // Relación con CategoriaProducto
    private int porcentajeComision;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Constructor completo
    public ComisionCategoriaProducto(int idComisionCategoriaProducto, int idCategoriaProducto,
                                     int porcentajeComision, LocalDateTime fechaCreacion,
                                     LocalDateTime fechaActualizacion) {
        this.idComisionCategoriaProducto = idComisionCategoriaProducto;
        this.idCategoriaProducto = idCategoriaProducto;
        this.porcentajeComision = porcentajeComision;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    // Constructor para crear nuevas configuraciones de comisión (sin ID ni fechas iniciales)
    public ComisionCategoriaProducto(int idCategoriaProducto, int porcentajeComision) {
        this.idCategoriaProducto = idCategoriaProducto;
        this.porcentajeComision = porcentajeComision;
    }

    // --- Getters y Setters ---
    public int getIdComisionCategoriaProducto() {
        return idComisionCategoriaProducto;
    }

    public void setIdComisionCategoriaProducto(int idComisionCategoriaProducto) {
        this.idComisionCategoriaProducto = idComisionCategoriaProducto;
    }

    public int getIdCategoriaProducto() {
        return idCategoriaProducto;
    }

    public void setIdCategoriaProducto(int idCategoriaProducto) {
        this.idCategoriaProducto = idCategoriaProducto;
    }

    public int getPorcentajeComision() {
        return porcentajeComision;
    }

    public void setPorcentajeComision(int porcentajeComision) {
        this.porcentajeComision = porcentajeComision;
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

    @Override
    public String toString() {
        return "ComisionCategoriaProducto{" +
                "idComisionCategoriaProducto=" + idComisionCategoriaProducto +
                ", idCategoriaProducto=" + idCategoriaProducto +
                ", porcentajeComision=" + porcentajeComision + "%" +
                '}';
    }
}