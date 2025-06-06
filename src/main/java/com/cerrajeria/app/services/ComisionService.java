package com.cerrajeria.app.services;

import com.cerrajeria.app.dao.*;
import com.cerrajeria.app.models.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Clase de servicio para la gestión de comisiones.
 */
public class ComisionService {

    private final ComisionDAO comisionDAO;
    private final ComisionCategoriaProductoDAO comisionCategoriaProductoDAO;
    private final UsuarioDAO usuarioDAO;
    private final ProductoDAO productoDAO;
    private final ServicioDAO servicioDAO;
    private final DetalleVentaProductoDAO detalleVentaProductoDAO;

    public ComisionService() {
        this.comisionDAO = new ComisionDAO();
        this.comisionCategoriaProductoDAO = new ComisionCategoriaProductoDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.productoDAO = new ProductoDAO();
        this.servicioDAO = new ServicioDAO();
        this.detalleVentaProductoDAO = new DetalleVentaProductoDAO();
    }

    public boolean registrarComisionManual(int idUsuario, BigDecimal montoComision, String comentario, Integer idVenta, Integer idServicio) {
        if (montoComision == null || montoComision.compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("Error: Monto debe ser mayor a cero.");
            return false;
        }
        if (comentario == null || comentario.trim().isEmpty()) {
            System.err.println("Error: Comentario es obligatorio.");
            return false;
        }

        Usuario usuario = usuarioDAO.obtenerUsuarioPorId(idUsuario);
        if (usuario == null || !usuario.isActivo()) {
            System.err.println("Error: Usuario con ID " + idUsuario + " no existe o está inactivo.");
            return false;
        }

        Comision nuevaComision = new Comision(idUsuario, idVenta, idServicio, montoComision, "Pendiente", comentario, true, null);
        return comisionDAO.insertarComision(nuevaComision) != -1;
    }

    public boolean actualizarEstadoYComentarioAdminComision(int idComision, String nuevoEstado, String comentarioAdmin) {
        Comision comision = comisionDAO.obtenerComisionPorId(idComision);
        if (comision == null) {
            System.err.println("Error: Comisión no encontrada.");
            return false;
        }

        if (nuevoEstado == null || (!nuevoEstado.equalsIgnoreCase("Pendiente")
                && !nuevoEstado.equalsIgnoreCase("Aprobado")
                && !nuevoEstado.equalsIgnoreCase("Rechazado")
                && !nuevoEstado.equalsIgnoreCase("Pagado"))) {
            System.err.println("Error: Estado inválido.");
            return false;
        }

        comision.setEstado(nuevoEstado);
        comision.setComentarioAdmin(comentarioAdmin);
        return comisionDAO.actualizarComision(comision);
    }

    public boolean actualizarComentarioVendedor(int idComision, int idVendedor, String nuevoComentario) {
        Comision comision = comisionDAO.obtenerComisionPorId(idComision);
        if (comision == null) {
            System.err.println("Error: Comisión no encontrada.");
            return false;
        }
        if (comision.getIdUsuario() != idVendedor) {
            System.err.println("Error: El usuario no es el propietario de la comisión.");
            return false;
        }
        if (!"Pendiente".equalsIgnoreCase(comision.getEstado())) {
            System.err.println("Error: Solo se pueden editar comisiones pendientes.");
            return false;
        }
        if (nuevoComentario == null || nuevoComentario.trim().isEmpty()) {
            System.err.println("Error: Comentario no puede estar vacío.");
            return false;
        }

        comision.setComentario(nuevoComentario);
        return comisionDAO.actualizarComision(comision);
    }

    public boolean eliminarComision(int idComision, int idUsuarioQueElimina, String rolUsuarioQueElimina) {
        Comision comision = comisionDAO.obtenerComisionPorId(idComision);
        if (comision == null) {
            System.err.println("Error: Comisión no encontrada.");
            return false;
        }

        if ("Administrador".equalsIgnoreCase(rolUsuarioQueElimina)) {
            return comisionDAO.eliminarComision(idComision);
        } else if ("Vendedor".equalsIgnoreCase(rolUsuarioQueElimina)
                && comision.getIdUsuario() == idUsuarioQueElimina
                && "Pendiente".equalsIgnoreCase(comision.getEstado())
                && comision.isEsManual()) {
            return comisionDAO.eliminarComision(idComision);
        } else {
            System.err.println("Error: Permisos insuficientes o comisión no editable.");
            return false;
        }
    }

    public Comision obtenerComisionPorId(int idComision) {
        return comisionDAO.obtenerComisionPorId(idComision);
    }

    public List<Comision> obtenerTodasLasComisiones() {
        return comisionDAO.obtenerTodasLasComisiones();
    }

    public List<Comision> obtenerComisionesPorUsuario(int idUsuario) {
        return comisionDAO.obtenerComisionesPorUsuario(idUsuario);
    }

    public List<Comision> obtenerComisionesPorEstado(String estado) {
        return comisionDAO.obtenerComisionesPorEstado(estado);
    }

    public BigDecimal calcularComisionProducto(DetalleVentaProducto detalleProducto) {
        Producto producto = productoDAO.obtenerProductoPorId(detalleProducto.getIdProducto());
        if (producto == null) {
            System.err.println("Producto no encontrado: ID " + detalleProducto.getIdProducto());
            return BigDecimal.ZERO;
        }

        ComisionCategoriaProducto comisionConfig = comisionCategoriaProductoDAO.obtenerComisionPorCategoriaProductoId(producto.getIdCategoriaProducto());
        if (comisionConfig != null) {
            BigDecimal gananciaUnitaria = detalleProducto.getPrecioUnitarioFinal().subtract(producto.getCostoInterno());
            BigDecimal comisionUnit = gananciaUnitaria.multiply(BigDecimal.valueOf(comisionConfig.getPorcentajeComision()))
                    .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
            return comisionUnit.multiply(BigDecimal.valueOf(detalleProducto.getCantidad()));
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calcularComisionServicio(DetalleVentaServicio detalleServicio) {
        BigDecimal porcentajeFijo = new BigDecimal("0.05");
        return detalleServicio.getPrecioUnitarioFinal()
                .multiply(BigDecimal.valueOf(detalleServicio.getCantidad()))
                .multiply(porcentajeFijo)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public boolean generarComisionesAutomaticas(Venta venta, List<DetalleVentaProducto> detallesProductos, List<DetalleVentaServicio> detallesServicios) {
        boolean exito = true;
        int idUsuarioVendedor = venta.getIdUsuario();

        for (DetalleVentaProducto dp : detallesProductos) {
            BigDecimal montoComision = calcularComisionProducto(dp);
            if (montoComision.compareTo(BigDecimal.ZERO) > 0) {
                Comision comision = new Comision(idUsuarioVendedor, venta.getIdVenta(), null, montoComision, "Pendiente", "Comisión automática por producto", false, null);
                if (comisionDAO.insertarComision(comision) == -1) {
                    System.err.println("Fallo al insertar comisión de producto: " + dp.getIdProducto());
                    exito = false;
                }
            }
        }

        for (DetalleVentaServicio ds : detallesServicios) {
            BigDecimal montoComision = calcularComisionServicio(ds);
            if (montoComision.compareTo(BigDecimal.ZERO) > 0) {
                Comision comision = new Comision(idUsuarioVendedor, venta.getIdVenta(), ds.getIdServicio(), montoComision, "Pendiente", "Comisión automática por servicio", false, null);
                if (comisionDAO.insertarComision(comision) == -1) {
                    System.err.println("Fallo al insertar comisión de servicio: " + ds.getIdServicio());
                    exito = false;
                }
            }
        }

        return exito;
    }

    public boolean actualizarComisionCompleta(Comision comision) {
        return comisionDAO.actualizarComision(comision);
    }

    /**
     * Obtiene el nombre del primer producto de una venta (para el listado en el controlador).
     */
    public String obtenerNombreProductoPorVenta(Integer idVenta) {
        if (idVenta == null) return null;

        List<DetalleVentaProducto> detalles = detalleVentaProductoDAO.obtenerDetallesPorVenta(idVenta);
        if (detalles.isEmpty()) return null;

        DetalleVentaProducto detalle = detalles.get(0);
        Producto producto = productoDAO.obtenerProductoPorId(detalle.getIdProducto());
        return (producto != null) ? producto.getNombre() : null;
    }

    /**
     * Obtiene el nombre de un servicio por su ID (para el listado en el controlador).
     */
    public String obtenerNombreServicioPorId(Integer idServicio) {
        if (idServicio == null) return null;

        Servicio servicio = servicioDAO.obtenerServicioPorId(idServicio);
        return (servicio != null) ? servicio.getNombre() : null;
    }
}
