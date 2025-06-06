package com.cerrajeria.app.dao;

import com.cerrajeria.app.database.DatabaseManager;
import com.cerrajeria.app.models.MovimientoStock;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO (Data Access Object) para interactuar con la tabla 'movimiento_stock' en la base de datos.
 */
public class MovimientoStockDAO {

    /**
     * Inserta un nuevo movimiento de stock en la base de datos.
     * @param movimiento El objeto MovimientoStock a insertar.
     * @return El ID del movimiento recién insertado, o -1 si hubo un error.
     */
    public int insertarMovimientoStock(MovimientoStock movimiento) {
        String sql = "INSERT INTO movimiento_stock (id_producto, tipo, cantidad, motivo) VALUES (?, ?, ?, ?)";
        int idGenerado = -1;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, movimiento.getIdProducto());
            pstmt.setString(2, movimiento.getTipo());
            pstmt.setInt(3, movimiento.getCantidad());
            pstmt.setString(4, movimiento.getMotivo());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        movimiento.setIdMovimientoStock(idGenerado); // Actualiza el objeto
                        System.out.println("Movimiento de stock insertado con ID: " + idGenerado);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar movimiento de stock: " + e.getMessage());
        }
        return idGenerado;
    }

    /**
     * Obtiene un movimiento de stock por su ID.
     * @param idMovimiento El ID del movimiento a buscar.
     * @return El objeto MovimientoStock si se encuentra, o null.
     */
    public MovimientoStock obtenerMovimientoStockPorId(int idMovimiento) {
        String sql = "SELECT id_movimiento_stock, id_producto, tipo, cantidad, motivo, fecha_creacion, fecha_actualizacion FROM movimiento_stock WHERE id_movimiento_stock = ?";
        MovimientoStock movimiento = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idMovimiento);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    movimiento = mapearResultSetAMovimientoStock(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener movimiento de stock por ID: " + e.getMessage());
        }
        return movimiento;
    }

    /**
     * Obtiene una lista de movimientos de stock para un producto específico.
     * @param idProducto El ID del producto.
     * @return Una lista de objetos MovimientoStock.
     */
    public List<MovimientoStock> obtenerMovimientosPorProductoId(int idProducto) {
        String sql = "SELECT id_movimiento_stock, id_producto, tipo, cantidad, motivo, fecha_creacion, fecha_actualizacion FROM movimiento_stock WHERE id_producto = ? ORDER BY fecha_creacion DESC";
        List<MovimientoStock> movimientos = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idProducto);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapearResultSetAMovimientoStock(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener movimientos por ID de producto: " + e.getMessage());
        }
        return movimientos;
    }

    /**
     * Obtiene una lista de todos los movimientos de stock.
     * @return Una lista de objetos MovimientoStock.
     */
    public List<MovimientoStock> obtenerTodosLosMovimientosStock() {
        String sql = "SELECT id_movimiento_stock, id_producto, tipo, cantidad, motivo, fecha_creacion, fecha_actualizacion FROM movimiento_stock ORDER BY fecha_creacion DESC";
        List<MovimientoStock> movimientos = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                movimientos.add(mapearResultSetAMovimientoStock(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los movimientos de stock: " + e.getMessage());
        }
        return movimientos;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto MovimientoStock.
     * @param rs El ResultSet actual.
     * @return Un objeto MovimientoStock con los datos del ResultSet.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    private MovimientoStock mapearResultSetAMovimientoStock(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_movimiento_stock");
        int idProducto = rs.getInt("id_producto");
        String tipo = rs.getString("tipo");
        int cantidad = rs.getInt("cantidad");
        String motivo = rs.getString("motivo");

        Timestamp tsCreacion = rs.getTimestamp("fecha_creacion");
        LocalDateTime fechaCreacion = (tsCreacion != null) ? tsCreacion.toLocalDateTime() : null;

        Timestamp tsActualizacion = rs.getTimestamp("fecha_actualizacion");
        LocalDateTime fechaActualizacion = (tsActualizacion != null) ? tsActualizacion.toLocalDateTime() : null;

        return new MovimientoStock(id, idProducto, tipo, cantidad, motivo, fechaCreacion, fechaActualizacion);
    }
}