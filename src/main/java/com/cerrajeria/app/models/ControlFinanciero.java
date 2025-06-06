package com.cerrajeria.app.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Clase modelo que representa un registro de control financiero en la tabla 'control_financiero'.
 * Mapea las columnas de la base de datos a atributos Java, incluyendo el costo si aplica.
 */
public class ControlFinanciero {

    private int idControlFinanciero;
    private String tipo; // 'Ingreso' o 'Egreso'
    private String descripcion;
    private BigDecimal monto;
    private BigDecimal costo; // Nuevo campo para reflejar el costo
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Constructor completo
    public ControlFinanciero(int idControlFinanciero, String tipo, String descripcion,
                             BigDecimal monto, BigDecimal costo,
                             LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion) {
        this.idControlFinanciero = idControlFinanciero;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.monto = monto;
        this.costo = costo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    // Constructor para crear nuevos registros (sin ID ni fechas iniciales)
    public ControlFinanciero(String tipo, String descripcion, BigDecimal monto) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.monto = monto;
        this.costo = BigDecimal.ZERO; // Inicializar costo en cero por defecto
    }

    // --- Getters y Setters ---
    public int getIdControlFinanciero() {
        return idControlFinanciero;
    }

    public void setIdControlFinanciero(int idControlFinanciero) {
        this.idControlFinanciero = idControlFinanciero;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
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
        return "ControlFinanciero{" +
                "id=" + idControlFinanciero +
                ", tipo='" + tipo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", monto=" + monto +
                ", costo=" + costo +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaActualizacion=" + fechaActualizacion +
                '}';
    }
}
