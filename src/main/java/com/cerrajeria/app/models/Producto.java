package com.cerrajeria.app.models;

import java.math.BigDecimal; // Para manejar precios y costos con precisión
import java.time.LocalDateTime;

/**
 * Clase modelo que representa un producto en la tabla 'producto'.
 * Mapea las columnas de la base de datos a atributos Java.
 */
public class Producto {

    private int idProducto;
    private String nombre;
    private int idCategoriaProducto; // Relación con CategoriaProducto
    private BigDecimal precio; // Usar BigDecimal para dinero
    private int stock;
    private int stockMinimo;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private BigDecimal costoInterno; // Columna 'costo_interno'
    private boolean activo; // Columna 'activo' para borrado lógico

    // Constructor completo
    public Producto(int idProducto, String nombre, int idCategoriaProducto, BigDecimal precio, int stock,
                    int stockMinimo, String estado, LocalDateTime fechaCreacion,
                    LocalDateTime fechaActualizacion, BigDecimal costoInterno, boolean activo) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.idCategoriaProducto = idCategoriaProducto;
        this.precio = precio;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.costoInterno = costoInterno;
        this.activo = activo;
    }

    // Constructor para crear nuevos productos (sin ID, fechas iniciales ni estado, que la DB puede manejar)
    public Producto(String nombre, int idCategoriaProducto, BigDecimal precio, int stock,
                    int stockMinimo, BigDecimal costoInterno) {
        this.nombre = nombre;
        this.idCategoriaProducto = idCategoriaProducto;
        this.precio = precio;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.costoInterno = costoInterno;
        this.activo = true; // Por defecto activo al crear
        // El estado inicial ('Disponible') y las fechas se pueden dejar que la DB las establezca
    }

    // --- Getters y Setters ---
    // (IntelliJ puede generarlos automáticamente: Alt+Insert o Cmd+N -> Getter and Setter)

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getIdCategoriaProducto() {
        return idCategoriaProducto;
    }

    public void setIdCategoriaProducto(int idCategoriaProducto) {
        this.idCategoriaProducto = idCategoriaProducto;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    public BigDecimal getCostoInterno() {
        return costoInterno;
    }

    public void setCostoInterno(BigDecimal costoInterno) {
        this.costoInterno = costoInterno;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "idProducto=" + idProducto +
                ", nombre='" + nombre + '\'' +
                ", idCategoria=" + idCategoriaProducto +
                ", precio=" + precio +
                ", stock=" + stock +
                ", estado='" + estado + '\'' +
                ", costoInterno=" + costoInterno +
                ", activo=" + activo +
                '}';
    }
}