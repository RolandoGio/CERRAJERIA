package com.cerrajeria.app.models;

import java.math.BigDecimal; // Para manejar el total_venta con precisión
import java.time.LocalDateTime;

/**
 * Clase modelo que representa una venta en la tabla 'venta'.
 * Mapea las columnas de la base de datos a atributos Java.
 */
public class Venta {

    private int idVenta;
    private int idUsuario; // Relación con Usuario (el vendedor que realizó la venta)
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private BigDecimal totalVenta; // Columna 'total_venta' agregada después

    // Constructor completo
    public Venta(int idVenta, int idUsuario, LocalDateTime fechaCreacion,
                 LocalDateTime fechaActualizacion, BigDecimal totalVenta) {
        this.idVenta = idVenta;
        this.idUsuario = idUsuario;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.totalVenta = totalVenta;
    }

    // Constructor para crear nuevas ventas (sin ID ni fechas iniciales)
    // La fecha y el total_venta serán gestionados por la DB/DAO
    public Venta(int idUsuario) {
        this.idUsuario = idUsuario;
        this.totalVenta = BigDecimal.ZERO; // Valor por defecto inicial, se actualizará con los detalles
    }

    // --- Getters y Setters ---
    // (IntelliJ puede generarlos automáticamente: Alt+Insert o Cmd+N -> Getter and Setter)

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
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

    public BigDecimal getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(BigDecimal totalVenta) {
        this.totalVenta = totalVenta;
    }

    @Override
    public String toString() {
        return "Venta{" +
                "idVenta=" + idVenta +
                ", idVendedor=" + idUsuario +
                ", fechaCreacion=" + fechaCreacion +
                ", totalVenta=" + totalVenta +
                '}';
    }
}