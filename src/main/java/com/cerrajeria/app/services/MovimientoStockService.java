package com.cerrajeria.app.services;

import com.cerrajeria.app.dao.MovimientoStockDAO;
import com.cerrajeria.app.dao.ProductoDAO; // Para verificar el producto
import com.cerrajeria.app.models.MovimientoStock;
import com.cerrajeria.app.models.Producto;

import java.util.List;

/**
 * Clase de servicio para la gestión de movimientos de stock.
 * Permite registrar entradas y salidas de productos del inventario.
 */
public class MovimientoStockService {

    private MovimientoStockDAO movimientoStockDAO;
    private ProductoDAO productoDAO; // Necesario para validar si el producto existe

    // Constructor
    public MovimientoStockService() {
        this.movimientoStockDAO = new MovimientoStockDAO();
        this.productoDAO = new ProductoDAO();
    }

    /**
     * Registra una entrada de stock para un producto.
     * @param idProducto ID del producto afectado.
     * @param cantidad Cantidad que ingresa al stock.
     * @param comentario Comentario sobre el movimiento (ej. "Compra a proveedor", "Devolución").
     * @return true si la entrada fue registrada y el stock actualizado, false en caso contrario.
     */
    public boolean registrarEntradaStock(int idProducto, int cantidad, String comentario) {
        if (cantidad <= 0) {
            System.err.println("Error al registrar entrada de stock: La cantidad debe ser positiva.");
            return false;
        }
        Producto producto = productoDAO.obtenerProductoPorId(idProducto);
        if (producto == null) {
            System.err.println("Error al registrar entrada de stock: Producto con ID " + idProducto + " no encontrado.");
            return false;
        }

        // Actualizar stock del producto
        producto.setStock(producto.getStock() + cantidad);
        // Recalcular estado del stock
        if (producto.getStock() <= 0) { // Aunque es una entrada, siempre es bueno verificar
            producto.setEstado("Agotado");
        } else if (producto.getStock() <= producto.getStockMinimo()) {
            producto.setEstado("Bajo");
        } else {
            producto.setEstado("Disponible");
        }

        if (!productoDAO.actualizarProducto(producto)) {
            System.err.println("Fallo al actualizar stock del producto ID: " + idProducto + " durante la entrada.");
            return false;
        }

        // Registrar movimiento de stock
        MovimientoStock movimiento = new MovimientoStock(idProducto, "Entrada", cantidad, comentario);
        return movimientoStockDAO.insertarMovimientoStock(movimiento) != -1;
    }

    /**
     * Registra una salida de stock para un producto (no relacionada con venta).
     * @param idProducto ID del producto afectado.
     * @param cantidad Cantidad que sale del stock.
     * @param comentario Comentario sobre el movimiento (ej. "Merma", "Uso interno", "Error de inventario").
     * @return true si la salida fue registrada y el stock actualizado, false en caso contrario.
     */
    public boolean registrarSalidaStock(int idProducto, int cantidad, String comentario) {
        if (cantidad <= 0) {
            System.err.println("Error al registrar salida de stock: La cantidad debe ser positiva.");
            return false;
        }
        Producto producto = productoDAO.obtenerProductoPorId(idProducto);
        if (producto == null) {
            System.err.println("Error al registrar salida de stock: Producto con ID " + idProducto + " no encontrado.");
            return false;
        }
        if (producto.getStock() < cantidad) {
            System.err.println("Error al registrar salida de stock: Stock insuficiente para el producto " + producto.getNombre() + ". Stock actual: " + producto.getStock() + ", Cantidad a retirar: " + cantidad);
            return false;
        }

        // Actualizar stock del producto
        producto.setStock(producto.getStock() - cantidad);
        // Recalcular estado del stock
        if (producto.getStock() <= 0) {
            producto.setEstado("Agotado");
        } else if (producto.getStock() <= producto.getStockMinimo()) {
            producto.setEstado("Bajo");
        } else {
            producto.setEstado("Disponible");
        }

        if (!productoDAO.actualizarProducto(producto)) {
            System.err.println("Fallo al actualizar stock del producto ID: " + idProducto + " durante la salida.");
            return false;
        }

        // Registrar movimiento de stock
        MovimientoStock movimiento = new MovimientoStock(idProducto, "Salida", cantidad, comentario);
        return movimientoStockDAO.insertarMovimientoStock(movimiento) != -1;
    }

    /**
     * Obtiene un movimiento de stock por su ID.
     * @param idMovimiento ID del movimiento.
     * @return El objeto MovimientoStock o null.
     */
    public MovimientoStock obtenerMovimientoStockPorId(int idMovimiento) {
        return movimientoStockDAO.obtenerMovimientoStockPorId(idMovimiento);
    }

    /**
     * Obtiene todos los movimientos de stock.
     * @return Lista de objetos MovimientoStock.
     */
    public List<MovimientoStock> obtenerTodosLosMovimientosStock() {
        return movimientoStockDAO.obtenerTodosLosMovimientosStock();
    }

    /**
     * Obtiene movimientos de stock por tipo ('Entrada' o 'Salida').
     * @param tipo Tipo de movimiento a filtrar.
     * @return Lista de objetos MovimientoStock que coinciden con el tipo.
     */
    public List<MovimientoStock> obtenerMovimientosStockPorTipo(String tipo) {
        // En tu MovimientoStockDAO actual no hay un método para filtrar por tipo,
        // así que filtramos en memoria por ahora. Si el volumen es grande,
        // sería ideal agregar un método `obtenerMovimientosStockPorTipo` al DAO.
        return movimientoStockDAO.obtenerTodosLosMovimientosStock().stream()
                .filter(m -> m.getTipo().equalsIgnoreCase(tipo))
                .toList();
    }

    /**
     * Obtiene movimientos de stock para un producto específico.
     * @param idProducto ID del producto.
     * @return Lista de objetos MovimientoStock asociados al producto.
     */
    public List<MovimientoStock> obtenerMovimientosStockPorProducto(int idProducto) {
        // Corregido para usar el método de tu DAO
        return movimientoStockDAO.obtenerMovimientosPorProductoId(idProducto);
    }
}