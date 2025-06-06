package com.cerrajeria.app.models;

import java.math.BigDecimal; // Para precio_unitario_final
import java.time.LocalDateTime;

/**
 * Clase modelo que representa un detalle de producto en la tabla 'detalle_venta_producto'.
 * Mapea las columnas de la base de datos a atributos Java.
 */
public class DetalleVentaProducto {

    private int idDetalleVentaProducto;
    private int idVenta; // Relación con Venta
    private int idProducto; // Relación con Producto
    private int cantidad;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private BigDecimal precioUnitarioFinal; // Columna 'precio_unitario_final'
    private String descripcion; // Columna 'descripcion' para comentarios del vendedor/asistente

    // Constructor completo
    public DetalleVentaProducto(int idDetalleVentaProducto, int idVenta, int idProducto,
                                int cantidad, LocalDateTime fechaCreacion,
                                LocalDateTime fechaActualizacion, BigDecimal precioUnitarioFinal,
                                String descripcion) {
        this.idDetalleVentaProducto = idDetalleVentaProducto;
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.precioUnitarioFinal = precioUnitarioFinal;
        this.descripcion = descripcion;
    }

    // Constructor para crear nuevos detalles de producto (sin ID ni fechas iniciales)
    public DetalleVentaProducto(int idVenta, int idProducto, int cantidad,
                                BigDecimal precioUnitarioFinal, String descripcion) {
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitarioFinal = precioUnitarioFinal;
        this.descripcion = descripcion;
    }

    // --- Getters y Setters ---
    // (IntelliJ puede generarlos automáticamente: Alt+Insert o Cmd+N -> Getter and Setter)



    public int getIdDetalleVentaProducto() {
        return idDetalleVentaProducto;
    }

    public void setIdDetalleVentaProducto(int idDetalleVentaProducto) {
        this.idDetalleVentaProducto = idDetalleVentaProducto;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
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
        return "DetalleVentaProducto{" +
                "idDetalleVentaProducto=" + idDetalleVentaProducto +
                ", idVenta=" + idVenta +
                ", idProducto=" + idProducto +
                ", cantidad=" + cantidad +
                ", precioUnitarioFinal=" + precioUnitarioFinal +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }



        public DetalleVentaProducto() {
            // Puedes dejarlo vacío o inicializar valores por defecto si es necesario
        }



}