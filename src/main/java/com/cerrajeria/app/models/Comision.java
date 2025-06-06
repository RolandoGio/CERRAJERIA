package com.cerrajeria.app.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Clase modelo que representa una comisión individual en la tabla 'comision'.
 */
public class Comision {

    private int idComision;
    private int idUsuario;     // ID del usuario (vendedor/asistente) al que se le asigna la comisión
    private Integer idVenta;   // ID de la venta asociada (puede ser NULL si es manual o por servicio)
    private Integer idServicio; // ID del servicio asociado (puede ser NULL si es por producto o manual)
    private BigDecimal montoComision;
    private String estado;     // 'Pendiente', 'Pagado'
    private String comentario; // Comentario del vendedor/sistema
    private boolean esManual;  // BIT en DB, para indicar si fue generada manualmente
    private String comentarioAdmin; // Columna 'comentario_admin' agregada después
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Constructor completo
    public Comision(int idComision, int idUsuario, Integer idVenta, Integer idServicio,
                    BigDecimal montoComision, String estado, String comentario,
                    boolean esManual, String comentarioAdmin,
                    LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion) {
        this.idComision = idComision;
        this.idUsuario = idUsuario;
        this.idVenta = idVenta;
        this.idServicio = idServicio;
        this.montoComision = montoComision;
        this.estado = estado;
        this.comentario = comentario;
        this.esManual = esManual;
        this.comentarioAdmin = comentarioAdmin;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    // Constructor para crear nuevas comisiones (sin ID ni fechas)
    public Comision(int idUsuario, Integer idVenta, Integer idServicio,
                    BigDecimal montoComision, String estado, String comentario,
                    boolean esManual, String comentarioAdmin) {
        this.idUsuario = idUsuario;
        this.idVenta = idVenta;
        this.idServicio = idServicio;
        this.montoComision = montoComision;
        this.estado = estado;
        this.comentario = comentario;
        this.esManual = esManual;
        this.comentarioAdmin = comentarioAdmin;
    }

    // --- Getters y Setters ---
    public int getIdComision() {
        return idComision;
    }

    public void setIdComision(int idComision) {
        this.idComision = idComision;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Integer idVenta) {
        this.idVenta = idVenta;
    }

    public Integer getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Integer idServicio) {
        this.idServicio = idServicio;
    }

    public BigDecimal getMontoComision() {
        return montoComision;
    }

    public void setMontoComision(BigDecimal montoComision) {
        this.montoComision = montoComision;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public boolean isEsManual() {
        return esManual;
    }

    public void setEsManual(boolean esManual) {
        this.esManual = esManual;
    }

    public String getComentarioAdmin() {
        return comentarioAdmin;
    }

    public void setComentarioAdmin(String comentarioAdmin) {
        this.comentarioAdmin = comentarioAdmin;
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
        return "Comision{" +
                "idComision=" + idComision +
                ", idUsuario=" + idUsuario +
                ", idVenta=" + idVenta +
                ", idServicio=" + idServicio +
                ", montoComision=" + montoComision +
                ", estado='" + estado + '\'' +
                ", esManual=" + esManual +
                ", comentarioAdmin='" + comentarioAdmin + '\'' +
                '}';
    }
}