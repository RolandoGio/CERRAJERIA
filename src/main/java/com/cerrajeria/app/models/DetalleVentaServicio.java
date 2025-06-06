package com.cerrajeria.app.models;

import java.math.BigDecimal; // Para precio_unitario_final
import java.time.LocalDateTime;

/**
 * Clase modelo que representa un detalle de servicio en la tabla 'detalle_venta_servicio'.
 * Mapea las columnas de la base de datos a atributos Java.
 */
public class DetalleVentaServicio {

    private int idDetalleVentaServicio;
    private int idVenta; // Relación con Venta
    private int idServicio; // Relación con Servicio
    private int cantidad;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private BigDecimal precioUnitarioFinal; // Columna 'precio_unitario_final'
    private String descripcion; // Columna 'descripcion' para comentarios del vendedor/asistente

    // Constructor completo
    public DetalleVentaServicio(int idDetalleVentaServicio, int idVenta, int idServicio,
                                int cantidad, LocalDateTime fechaCreacion,
                                LocalDateTime fechaActualizacion, BigDecimal precioUnitarioFinal,
                                String descripcion) {
        this.idDetalleVentaServicio = idDetalleVentaServicio;
        this.idVenta = idVenta;
        this.idServicio = idServicio;
        this.cantidad = cantidad;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.precioUnitarioFinal = precioUnitarioFinal;
        this.descripcion = descripcion;
    }

    // Constructor para crear nuevos detalles de servicio (sin ID ni fechas iniciales)
    public DetalleVentaServicio(int idVenta, int idServicio, int cantidad,
                                BigDecimal precioUnitarioFinal, String descripcion) {
        this.idVenta = idVenta;
        this.idServicio = idServicio;
        this.cantidad = cantidad;
        this.precioUnitarioFinal = precioUnitarioFinal;
        this.descripcion = descripcion;
    }

    // --- Getters y Setters ---
    // (IntelliJ puede generarlos automáticamente: Alt+Insert o Cmd+N -> Getter and Setter)

    public int getIdDetalleVentaServicio() {
        return idDetalleVentaServicio;
    }

    public void setIdDetalleVentaServicio(int idDetalleVentaServicio) {
        this.idDetalleVentaServicio = idDetalleVentaServicio;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public int getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(int idServicio) {
        this.idServicio = idServicio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
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

    public BigDecimal getPrecioUnitarioFinal() {
        return precioUnitarioFinal;
    }

    public void setPrecioUnitarioFinal(BigDecimal precioUnitarioFinal) {
        this.precioUnitarioFinal = precioUnitarioFinal;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "DetalleVentaServicio{" +
                "idDetalleVentaServicio=" + idDetalleVentaServicio +
                ", idVenta=" + idVenta +
                ", idServicio=" + idServicio +
                ", cantidad=" + cantidad +
                ", precioUnitarioFinal=" + precioUnitarioFinal +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }

    // Nuevo constructor por defecto
    public DetalleVentaServicio() {
        // Puedes dejarlo vacío o inicializar valores por defecto si es necesario
    }
}