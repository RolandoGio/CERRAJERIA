package com.cerrajeria.app.services;

import com.cerrajeria.app.dao.*;
import com.cerrajeria.app.models.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.cerrajeria.app.database.DatabaseManager; // Para manejar transacciones

/**
 * Clase de servicio para la gestión de ventas.
 * Coordina la creación de ventas, detalles de productos/servicios y el registro de movimientos de stock.
 * La actualización del stock del producto y el cálculo del total de la venta
 * se delegan a los triggers de la base de datos para mantener la consistencia.
 */
public class VentaService {

    private VentaDAO ventaDAO;
    private DetalleVentaProductoDAO detalleVentaProductoDAO;
    private DetalleVentaServicioDAO detalleVentaServicioDAO;
    private ComisionService comisionService;
    private ProductoDAO productoDAO; // Necesario para obtener información del producto (ej. stock actual para validación)
    private ServicioDAO servicioDAO; // Necesario para verificar precios de referencia
    private MovimientoStockDAO movimientoStockDAO; // Para registrar salidas de stock

    // Constructor
    public VentaService() {
        this.ventaDAO = new VentaDAO();
        this.detalleVentaProductoDAO = new DetalleVentaProductoDAO();
        this.detalleVentaServicioDAO = new DetalleVentaServicioDAO();
        this.productoDAO = new ProductoDAO();
        this.servicioDAO = new ServicioDAO();
        this.movimientoStockDAO = new MovimientoStockDAO();
        this.comisionService = new ComisionService(); // Inicializar el servicio de comisiones
    }

    /**
     * Registra una nueva venta, incluyendo sus productos y servicios.
     * Esta operación es transaccional: si alguna parte falla, se revertirá toda la venta.
     * La actualización del stock del producto y el cálculo del total de la venta
     * se delegan a los triggers de la base de datos.
     *
     * @param idUsuario ID del usuario (vendedor) que realiza la venta.
     * @param productos Lista de DetalleVentaProducto que forman parte de la venta.
     * @param servicios Lista de DetalleVentaServicio que forman parte de la venta.
     * @return El objeto Venta creado si la operación es exitosa (con el ID asignado), o null si falla.
     */
    public Venta registrarVenta(int idUsuario, List<DetalleVentaProducto> productos, List<DetalleVentaServicio> servicios) {
        Connection conn = null; // La conexión se gestionará aquí para la transacción
        Venta nuevaVenta = null;
        try {
            conn = DatabaseManager.getConnection();
            if (conn == null) {
                throw new SQLException("No se pudo establecer la conexión a la base de datos.");
            }
            conn.setAutoCommit(false); // Iniciar la transacción

            // 1. Insertar la cabecera de la venta
            // El total_venta se actualizará por triggers después de insertar los detalles.
            Venta ventaHeader = new Venta(idUsuario);
            int idVenta = ventaDAO.insertarVenta(ventaHeader);
            if (idVenta == -1) {
                throw new SQLException("Fallo al insertar la cabecera de la venta.");
            }
            ventaHeader.setIdVenta(idVenta);
            nuevaVenta = ventaHeader; // Asignar la venta creada a la variable de retorno

            // 2. Procesar detalles de productos
            for (DetalleVentaProducto detalleProducto : productos) {
                detalleProducto.setIdVenta(idVenta); // Asignar el ID de la venta

                // Validar stock antes de insertar el detalle (previene errores en la base de datos)
                Producto productoAfectado = productoDAO.obtenerProductoPorId(detalleProducto.getIdProducto());
                if (productoAfectado == null) {
                    throw new SQLException("Producto con ID " + detalleProducto.getIdProducto() + " no encontrado.");
                }
                if (productoAfectado.getStock() < detalleProducto.getCantidad()) {
                    throw new SQLException("Stock insuficiente para el producto: " + productoAfectado.getNombre() + ". Stock actual: " + productoAfectado.getStock() + ", Cantidad requerida: " + detalleProducto.getCantidad());
                }

                // Insertar el detalle del producto. El trigger tr_restar_stock_venta se encargará del stock.
                int idDetalleProd = detalleVentaProductoDAO.insertarDetalleVentaProducto(detalleProducto);
                if (idDetalleProd == -1) {
                    throw new SQLException("Fallo al insertar detalle de producto para Venta ID: " + idVenta);
                }

                // Registrar movimiento de stock (Salida) - Esto no lo hace el trigger
                MovimientoStock salidaStock = new MovimientoStock(
                        detalleProducto.getIdProducto(),
                        "Salida",
                        detalleProducto.getCantidad(),
                        "Venta ID " + idVenta + " - Producto: " + productoAfectado.getNombre()
                );
                if (movimientoStockDAO.insertarMovimientoStock(salidaStock) == -1) {
                    throw new SQLException("Fallo al registrar movimiento de stock para producto ID: " + detalleProducto.getIdProducto());
                }
            }

            // 3. Procesar detalles de servicios
            for (DetalleVentaServicio detalleServicio : servicios) {
                detalleServicio.setIdVenta(idVenta);
                // Insertar el detalle del servicio
                int idDetalleServ = detalleVentaServicioDAO.insertarDetalleVentaServicio(detalleServicio);
                if (idDetalleServ == -1) {
                    throw new SQLException("Fallo al insertar detalle de servicio para Venta ID: " + idVenta);
                }
            }

            // 4. Los triggers de la base de datos (trg_actualizar_total_venta_producto y trg_actualizar_total_venta_servicio)
            // se encargarán de recalcular y actualizar el campo total_venta en la tabla 'venta'.
            // No es necesario hacerlo explícitamente aquí en el servicio.

            // 5. Generar comisiones automáticas después de registrar los detalles
            // Aquí podríamos necesitar obtener el total de la venta de la BD si el servicio de comisiones lo requiere
            // antes del commit, pero es mejor que los triggers hayan terminado de actualizar.
            // Para simplificar, asumimos que el servicio de comisiones puede obtener el total o calcularlo internamente.
            boolean comisionesGeneradas = comisionService.generarComisionesAutomaticas(nuevaVenta, productos, servicios);
            if (!comisionesGeneradas) {
                // Si la generación de comisiones falla, se puede decidir si revertir la venta o solo loggear el error
                System.err.println("Advertencia: No se pudieron generar todas las comisiones automáticas para la venta ID: " + idVenta);
                // Si esto fuera crítico, se podría lanzar una excepción y revertir la venta.
            }

            conn.commit(); // Confirmar la transacción
            System.out.println("Venta completa registrada con éxito para ID: " + idVenta);

            // Opcional: Obtener la venta nuevamente de la BD para tener el total_venta actualizado por triggers
            // Esto es si 'nuevaVenta' necesita reflejar el total_venta calculado por la BD en este momento.
            // nuevaVenta = ventaDAO.obtenerVentaPorId(idVenta);

            return nuevaVenta; // Retornar el objeto Venta creado (con el ID)
        } catch (SQLException e) {
            System.err.println("Error en la transacción de venta: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // Revertir la transacción en caso de error
                    System.err.println("Transacción de venta revertida.");
                } catch (SQLException rollbackEx) {
                    System.err.println("Error al revertir la transacción: " + rollbackEx.getMessage());
                }
            }
            return null; // Retornar null si la venta falló
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaurar el modo autoCommit
                    conn.close(); // Cerrar la conexión
                } catch (SQLException closeEx) {
                    System.err.println("Error al cerrar la conexión: " + closeEx.getMessage());
                }
            }
        }
    }

    /**
     * Obtiene una venta por su ID.
     * @param idVenta ID de la venta.
     * @return El objeto Venta o null.
     */
    public Venta obtenerVentaPorId(int idVenta) {
        return ventaDAO.obtenerVentaPorId(idVenta);
    }

    /**
     * Obtiene una lista de todas las ventas.
     * @return Lista de objetos Venta.
     */
    public List<Venta> obtenerTodasLasVentas() {
        return ventaDAO.obtenerTodasLasVentas();
    }

    /**
     * Obtiene todos los detalles de productos para una venta específica.
     * @param idVenta ID de la venta.
     * @return Lista de DetalleVentaProducto.
     */
    public List<DetalleVentaProducto> obtenerDetallesProductoPorVenta(int idVenta) {
        return detalleVentaProductoDAO.obtenerDetallesPorVenta(idVenta);
    }

    /**
     * Obtiene todos los detalles de servicios para una venta específica.
     * @param idVenta ID de la venta.
     * @return Lista de DetalleVentaServicio.
     */
    public List<DetalleVentaServicio> obtenerDetallesServicioPorVenta(int idVenta) {
        return detalleVentaServicioDAO.obtenerDetallesPorVenta(idVenta);
    }

    /**
     * Asocia una venta existente a un vendedor específico (cambiando id_usuario).
     * @param idVenta El ID de la venta a modificar.
     * @param nuevoIdUsuario El ID del usuario al que se reasigna la venta.
     * @return true si la reasignación es exitosa, false en caso contrario.
     */
    public boolean reasignarVenta(int idVenta, int nuevoIdUsuario) {
        // En el VentaDAO actual, no existe un método específico para actualizar solo el id_usuario.
        // Para implementar esta funcionalidad, necesitarías añadir un método como:
        // public boolean actualizarUsuarioVenta(int idVenta, int nuevoIdUsuario) { ... }
        // en la clase VentaDAO que actualice el id_usuario y la fecha_actualizacion de la venta.

        // Por ahora, solo se devuelve false porque no hay un método de DAO que lo soporte directamente.
        System.err.println("La reasignación de venta requiere un método 'actualizarUsuarioVenta' en VentaDAO.");
        return false;
    }

    // Método auxiliar para buscar un producto por nombre para las pruebas/UI
    public Producto obtenerProductoPorNombre(String nombre) {
        return productoDAO.obtenerProductosPorNombre(nombre).stream().findFirst().orElse(null);
    }

    // Método auxiliar para buscar un servicio por nombre para las pruebas/UI
    public Servicio obtenerServicioPorNombre(String nombre) {
        return servicioDAO.obtenerServiciosPorNombre(nombre).stream().findFirst().orElse(null);
    }
}
