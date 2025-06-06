package com.cerrajeria.app.ui.models;

import java.math.BigDecimal;

/**
 * Clase modelo auxiliar para representar un item en el "carrito" de ventas en la UI.
 * Combina información de Producto o Servicio para mostrar en una tabla.
 */
public class VentaItem {
    private int id; // ID del producto o servicio
    private String nombre; // Nombre del producto o servicio
    private String tipo; // "Producto" o "Servicio"
    private int cantidad;
    private BigDecimal precioUnitario; // Este será el precio de venta real
    private BigDecimal subtotal; // Cantidad * Precio Unitario
    private String comentario; // ¡NUEVO CAMPO PARA COMENTARIOS!

    public VentaItem(int id, String nombre, String tipo, int cantidad, BigDecimal precioUnitario, String comentario) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.comentario = comentario; // Inicializa el comentario
        calcularSubtotal();
    }

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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
        calcularSubtotal();
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    private void calcularSubtotal() {
        if (precioUnitario != null) {
            this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    @Override
    public String toString() {
        return tipo + ": " + nombre + " (x" + cantidad + ") - $" + subtotal +
                (comentario != null && !comentario.isEmpty() ? " [" + comentario + "]" : "");
    }
}