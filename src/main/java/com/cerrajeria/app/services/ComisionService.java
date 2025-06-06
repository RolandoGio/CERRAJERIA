package com.cerrajeria.app.services;

import com.cerrajeria.app.dao.ComisionDAO;
import com.cerrajeria.app.dao.*;
import com.cerrajeria.app.models.*;
import com.cerrajeria.app.dao.ComisionCategoriaProductoDAO;
import com.cerrajeria.app.dao.UsuarioDAO;
import com.cerrajeria.app.models.Comision;
import com.cerrajeria.app.models.ComisionCategoriaProducto;
import com.cerrajeria.app.models.Usuario;
import com.cerrajeria.app.models.DetalleVentaProducto; // Podría usarse en un método de cálculo de comisión
import com.cerrajeria.app.models.DetalleVentaServicio; // Podría usarse en un método de cálculo de comisión

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional; // Para manejar retornos que podrían ser nulos

/**
 * Clase de servicio para la gestión de comisiones.
 * Contiene la lógica de negocio para crear, actualizar y visualizar comisiones.
 */
public class ComisionService {

    private ComisionDAO comisionDAO;
    private ComisionCategoriaProductoDAO comisionCategoriaProductoDAO;
    private UsuarioDAO usuarioDAO;

    // Constructor
    public ComisionService() {
        this.comisionDAO = new ComisionDAO();
        this.comisionCategoriaProductoDAO = new ComisionCategoriaProductoDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Registra una comisión manual para un usuario.
     * @param idUsuario ID del usuario al que se asigna la comisión.
     * @param montoComision Monto de la comisión.
     * @param comentario Comentario explicativo de la comisión.
     * @param idVenta Asociar a una venta (opcional, null si no aplica).
     * @param idServicio Asociar a un servicio específico (opcional, null si no aplica).
     * @return true si la comisión fue registrada exitosamente, false en caso contrario.
     */
    public boolean registrarComisionManual(int idUsuario, BigDecimal montoComision, String comentario, Integer idVenta, Integer idServicio) {
        // Validaciones básicas
        if (montoComision == null || montoComision.compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("Error al registrar comisión: El monto debe ser mayor a cero.");
            return false;
        }
        if (comentario == null || comentario.trim().isEmpty()) {
            System.err.println("Error al registrar comisión: El comentario es obligatorio para comisiones manuales.");
            return false;
        }

        // Verificar si el usuario existe y está activo
        Usuario usuario = usuarioDAO.obtenerUsuarioPorId(idUsuario);
        if (usuario == null || !usuario.isActivo()) {
            System.err.println("Error al registrar comisión: El usuario con ID " + idUsuario + " no existe o está inactivo.");
            return false;
        }

        Comision nuevaComision = new Comision(idUsuario, idVenta, idServicio, montoComision, "Pendiente", comentario, true, null);
        int id = comisionDAO.insertarComision(nuevaComision);
        return id != -1;
    }

    /**
     * Actualiza el estado y/o el comentario del administrador de una comisión.
     * Este método podría ser usado por un Administrador para revisar y aprobar/rechazar.
     * @param idComision ID de la comisión a actualizar.
     * @param nuevoEstado El nuevo estado de la comisión (ej. "Aprobada", "Rechazada", "Pagado").
     * @param comentarioAdmin Comentario adicional del administrador (puede ser null).
     * @return true si la actualización es exitosa, false en caso contrario.
     */
    public boolean actualizarEstadoYComentarioAdminComision(int idComision, String nuevoEstado, String comentarioAdmin) {
        Comision comision = comisionDAO.obtenerComisionPorId(idComision);
        if (comision == null) {
            System.err.println("Error al actualizar comisión: Comisión con ID " + idComision + " no encontrada.");
            return false;
        }
        // Validar el nuevo estado (ej. "Pendiente", "Aprobado", "Rechazado", "Pagado")
        if (nuevoEstado == null || (!nuevoEstado.equalsIgnoreCase("Pendiente") &&
                !nuevoEstado.equalsIgnoreCase("Aprobado") &&
                !nuevoEstado.equalsIgnoreCase("Rechazado") &&
                !nuevoEstado.equalsIgnoreCase("Pagado"))) {
            System.err.println("Error al actualizar comisión: Estado inválido proporcionado.");
            return false;
        }

        comision.setEstado(nuevoEstado);
        comision.setComentarioAdmin(comentarioAdmin);
        return comisionDAO.actualizarComision(comision);
    }

    /**
     * Permite a un vendedor actualizar su propio comentario en una comisión pendiente.
     * @param idComision ID de la comisión.
     * @param idVendedor ID del vendedor que intenta actualizar.
     * @param nuevoComentario Nuevo comentario del vendedor.
     * @return true si la actualización es exitosa, false en caso contrario.
     */
    public boolean actualizarComentarioVendedor(int idComision, int idVendedor, String nuevoComentario) {
        Comision comision = comisionDAO.obtenerComisionPorId(idComision);
        if (comision == null) {
            System.err.println("Error al actualizar comentario: Comisión con ID " + idComision + " no encontrada.");
            return false;
        }
        if (comision.getIdUsuario() != idVendedor) {
            System.err.println("Error al actualizar comentario: El usuario " + idVendedor + " no es el propietario de esta comisión.");
            return false;
        }
        if (!"Pendiente".equalsIgnoreCase(comision.getEstado())) {
            System.err.println("Error al actualizar comentario: Solo se pueden editar comisiones en estado 'Pendiente'.");
            return false;
        }
        if (nuevoComentario == null || nuevoComentario.trim().isEmpty()) {
            System.err.println("Error al actualizar comentario: El comentario no puede estar vacío.");
            return false;
        }

        comision.setComentario(nuevoComentario);
        return comisionDAO.actualizarComision(comision);
    }

    /**
     * Elimina una comisión.
     * @param idComision ID de la comisión a eliminar.
     * @param idUsuarioQueElimina ID del usuario que intenta eliminar (para validación de permisos).
     * @param rolUsuarioQueElimina Rol del usuario que intenta eliminar (ej. "Administrador").
     * @return true si la eliminación es exitosa, false en caso contrario.
     */
    public boolean eliminarComision(int idComision, int idUsuarioQueElimina, String rolUsuarioQueElimina) {
        Comision comision = comisionDAO.obtenerComisionPorId(idComision);
        if (comision == null) {
            System.err.println("Error al eliminar comisión: Comisión con ID " + idComision + " no encontrada.");
            return false;
        }

        // Un administrador puede eliminar cualquier comisión
        if ("Administrador".equalsIgnoreCase(rolUsuarioQueElimina)) {
            return comisionDAO.eliminarComision(idComision);
        }
        // Un vendedor solo puede eliminar sus propias comisiones si están pendientes y no son automáticas
        else if ("Vendedor".equalsIgnoreCase(rolUsuarioQueElimina) && comision.getIdUsuario() == idUsuarioQueElimina &&
                "Pendiente".equalsIgnoreCase(comision.getEstado()) && comision.isEsManual()) {
            return comisionDAO.eliminarComision(idComision);
        } else {
            System.err.println("Error al eliminar comisión: Permisos insuficientes o comisión no editable.");
            return false;
        }
    }


    /**
     * Obtiene una comisión por su ID.
     * @param idComision ID de la comisión.
     * @return El objeto Comision o null.
     */
    public Comision obtenerComisionPorId(int idComision) {
        return comisionDAO.obtenerComisionPorId(idComision);
    }

    /**
     * Obtiene una lista de todas las comisiones.
     * @return Lista de objetos Comision.
     */
    public List<Comision> obtenerTodasLasComisiones() {
        return comisionDAO.obtenerTodasLasComisiones();
    }

    /**
     * Obtiene una lista de comisiones para un usuario específico.
     * @param idUsuario ID del usuario.
     * @return Lista de objetos Comision.
     */
    public List<Comision> obtenerComisionesPorUsuario(int idUsuario) {
        return comisionDAO.obtenerComisionesPorUsuario(idUsuario);
    }

    /**
     * Obtiene una lista de comisiones por estado (ej. 'Pendiente', 'Pagado').
     * @param estado El estado de la comisión.
     * @return Lista de objetos Comision.
     */
    public List<Comision> obtenerComisionesPorEstado(String estado) {
        return comisionDAO.obtenerComisionesPorEstado(estado);
    }

    /**
     * Método para calcular comisión por un detalle de producto.
     * Este método es interno y podría ser llamado por otro servicio (ej. VentaService)
     * cuando se genera automáticamente una comisión a partir de una venta.
     * @param detalleProducto El detalle del producto vendido.
     * @return El monto de comisión calculado, o BigDecimal.ZERO si no aplica.
     */
    public BigDecimal calcularComisionProducto(DetalleVentaProducto detalleProducto) {
        // Obtener la categoría del producto
        Producto producto = new ProductoDAO().obtenerProductoPorId(detalleProducto.getIdProducto());
        if (producto == null) {
            System.err.println("Producto no encontrado para calcular comisión: ID " + detalleProducto.getIdProducto());
            return BigDecimal.ZERO;
        }

        // Obtener la configuración de comisión para esa categoría
        ComisionCategoriaProducto comisionConfig = comisionCategoriaProductoDAO.obtenerComisionPorCategoriaProductoId(producto.getIdCategoriaProducto());
        if (comisionConfig != null) {
            // (Precio Unitario Final - Costo Interno) * Cantidad * Porcentaje de Comisión
            BigDecimal gananciaUnitaria = detalleProducto.getPrecioUnitarioFinal().subtract(producto.getCostoInterno());
            BigDecimal comisionUnit = gananciaUnitaria.multiply(BigDecimal.valueOf(comisionConfig.getPorcentajeComision())).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
            return comisionUnit.multiply(BigDecimal.valueOf(detalleProducto.getCantidad()));
        }
        return BigDecimal.ZERO; // No hay configuración de comisión para esta categoría
    }

    /**
     * Método para calcular comisión por un detalle de servicio.
     * Se asume una comisión fija sobre el precio del servicio o un porcentaje predefinido.
     * Para simplificar, asumiremos que por ahora no hay una tabla `comision_categoria_servicio`
     * y que los servicios tienen una comisión fija o un porcentaje global que se definiría en código.
     * Si se requiere una tabla similar a `comision_categoria_producto`, habría que crearla.
     * @param detalleServicio El detalle del servicio vendido.
     * @return El monto de comisión calculado.
     */
    public BigDecimal calcularComisionServicio(DetalleVentaServicio detalleServicio) {
        // Ejemplo simple: 5% de comisión sobre el precio final del servicio
        // TODO: Podrías implementar una tabla de comisiones por categoría de servicio si es necesario.
        BigDecimal porcentajeFijoServicio = new BigDecimal("0.05"); // 5%
        return detalleServicio.getPrecioUnitarioFinal()
                .multiply(BigDecimal.valueOf(detalleServicio.getCantidad()))
                .multiply(porcentajeFijoServicio)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Genera comisiones automáticamente para los detalles de una venta.
     * @param venta La venta por la que se generarán comisiones.
     * @param detallesProductos Los detalles de productos de la venta.
     * @param detallesServicios Los detalles de servicios de la venta.
     * @return true si las comisiones se generaron exitosamente, false en caso contrario.
     */
    public boolean generarComisionesAutomaticas(Venta venta, List<DetalleVentaProducto> detallesProductos, List<DetalleVentaServicio> detallesServicios) {
        boolean exito = true;
        int idUsuarioVendedor = venta.getIdUsuario(); // El vendedor de la venta es quien recibe la comisión automática

        // Comisiones por productos
        for (DetalleVentaProducto dp : detallesProductos) {
            BigDecimal montoComision = calcularComisionProducto(dp);
            if (montoComision.compareTo(BigDecimal.ZERO) > 0) {
                Comision comisionProd = new Comision(idUsuarioVendedor, venta.getIdVenta(), null, montoComision, "Pendiente", "Comisión automática por venta de producto", false, null);
                if (comisionDAO.insertarComision(comisionProd) == -1) {
                    System.err.println("Fallo al insertar comisión automática por producto " + dp.getIdProducto() + " de Venta ID " + venta.getIdVenta());
                    exito = false;
                }
            }
        }

        // Comisiones por servicios
        for (DetalleVentaServicio ds : detallesServicios) {
            BigDecimal montoComision = calcularComisionServicio(ds);
            if (montoComision.compareTo(BigDecimal.ZERO) > 0) {
                Comision comisionServ = new Comision(idUsuarioVendedor, venta.getIdVenta(), ds.getIdServicio(), montoComision, "Pendiente", "Comisión automática por venta de servicio", false, null);
                if (comisionDAO.insertarComision(comisionServ) == -1) {
                    System.err.println("Fallo al insertar comisión automática por servicio " + ds.getIdServicio() + " de Venta ID " + venta.getIdVenta());
                    exito = false;
                }
            }
        }
        return exito;
    }

    // --- Métodos de visualización (lectura pura) ---
    public Optional<Comision> getComisionById(int id) {
        return Optional.ofNullable(comisionDAO.obtenerComisionPorId(id));
    }

    public List<Comision> getAllComisiones() {
        return comisionDAO.obtenerTodasLasComisiones();
    }

    public List<Comision> getComisionesByUsuario(int idUsuario) {
        return comisionDAO.obtenerComisionesPorUsuario(idUsuario);
    }

    public List<Comision> getComisionesByEstado(String estado) {
        return comisionDAO.obtenerComisionesPorEstado(estado);
    }
}