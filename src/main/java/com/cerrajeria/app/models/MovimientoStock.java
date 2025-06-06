package com.cerrajeria.app.models;

import java.time.LocalDateTime;

/**
 * Clase modelo que representa un movimiento de stock en la tabla 'movimiento_stock'.
 * Mapea las columnas de la base de datos a atributos Java.
 */
public class MovimientoStock {

    private int idMovimientoStock;
    private int idProducto; // Relación con Producto
    private String tipo; // 'Entrada' o 'Salida'
    private int cantidad;
    private String motivo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Constructor completo
    public MovimientoStock(int idMovimientoStock, int idProducto, String tipo, int cantidad,
                           String motivo, LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion) {
        this.idMovimientoStock = idMovimientoStock;
        this.idProducto = idProducto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    // Constructor para crear nuevos movimientos (sin ID ni fechas iniciales)
    public MovimientoStock(int idProducto, String tipo, int cantidad, String motivo) {
        this.idProducto = idProducto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.motivo = motivo;
        // Las fechas serán establecidas por la DB
    }

    // --- Getters y Setters ---
    // (IntelliJ puede generarlos automáticamente: Alt+Insert o Cmd+N -> Getter and Setter)

    public int getIdMovimientoStock() {
        return idMovimientoStock;
    }

    public void setIdMovimientoStock(int idMovimientoStock) {
        this.idMovimientoStock = idMovimientoStock;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
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
        return "MovimientoStock{" +
                "idMovimientoStock=" + idMovimientoStock +
                ", idProducto=" + idProducto +
                ", tipo='" + tipo + '\'' +
                ", cantidad=" + cantidad +
                ", motivo='" + motivo + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}