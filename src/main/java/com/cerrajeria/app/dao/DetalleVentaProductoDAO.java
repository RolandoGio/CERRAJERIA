package com.cerrajeria.app.dao;

import com.cerrajeria.app.database.DatabaseManager;
import com.cerrajeria.app.models.DetalleVentaProducto;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO (Data Access Object) para interactuar con la tabla 'detalle_venta_producto' en la base de datos.
 */
public class DetalleVentaProductoDAO {

    /**
     * Inserta un nuevo detalle de venta de producto en la base de datos.
     * @param detalle El objeto DetalleVentaProducto a insertar.
     * @return El ID del detalle recién insertado, o -1 si hubo un error.
     */
    public int insertarDetalleVentaProducto(DetalleVentaProducto detalle) {
        String sql = "INSERT INTO detalle_venta_producto (id_venta, id_producto, cantidad, precio_unitario_final, descripcion) VALUES (?, ?, ?, ?, ?)";
        int idGenerado = -1;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, detalle.getIdVenta());
            pstmt.setInt(2, detalle.getIdProducto());
            pstmt.setInt(3, detalle.getCantidad());
            pstmt.setBigDecimal(4, detalle.getPrecioUnitarioFinal());
            pstmt.setString(5, detalle.getDescripcion());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        detalle.setIdDetalleVentaProducto(idGenerado); // Actualiza el objeto
                        System.out.println("Detalle de venta de producto insertado con ID: " + idGenerado);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar detalle de venta de producto: " + e.getMessage());
        }
        return idGenerado;
    }

    /**
     * Obtiene un detalle de venta de producto por su ID.
     * @param idDetalle El ID del detalle a buscar.
     * @return El objeto DetalleVentaProducto si se encuentra, o null.
     */
    public DetalleVentaProducto obtenerDetalleVentaProductoPorId(int idDetalle) {
        String sql = "SELECT id_detalle_venta_producto, id_venta, id_producto, cantidad, " +
                "fecha_creacion, fecha_actualizacion, precio_unitario_final, descripcion FROM detalle_venta_producto WHERE id_detalle_venta_producto = ?";
        DetalleVentaProducto detalle = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idDetalle);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    detalle = mapearResultSetADetalleVentaProducto(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener detalle de venta de producto por ID: " + e.getMessage());
        }
        return detalle;
    }

    /**
     * Obtiene una lista de detalles de venta de producto para una venta específica.
     * @param idVenta El ID de la venta.
     * @return Una lista de objetos DetalleVentaProducto.
     */
    public List<DetalleVentaProducto> obtenerDetallesPorVenta(int idVenta) {
        String sql = "SELECT id_detalle_venta_producto, id_venta, id_producto, cantidad, " +
                "fecha_creacion, fecha_actualizacion, precio_unitario_final, descripcion FROM detalle_venta_producto WHERE id_venta = ? ORDER BY id_detalle_venta_producto";
        List<DetalleVentaProducto> detalles = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idVenta);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    detalles.add(mapearResultSetADetalleVentaProducto(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener detalles de venta de producto por venta: " + e.getMessage());
        }
        return detalles;
    }

    /**
     * Actualiza los datos de un detalle de venta de producto existente.
     * @param detalle El objeto DetalleVentaProducto con los datos actualizados (el ID debe estar establecido).
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarDetalleVentaProducto(DetalleVentaProducto detalle) {
        String sql = "UPDATE detalle_venta_producto SET id_venta = ?, id_producto = ?, cantidad = ?, precio_unitario_final = ?, descripcion = ?, fecha_actualizacion = GETDATE() WHERE id_detalle_venta_producto = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, detalle.getIdVenta());
            pstmt.setInt(2, detalle.getIdProducto());
            pstmt.setInt(3, detalle.getCantidad());
            pstmt.setBigDecimal(4, detalle.getPrecioUnitarioFinal());
            pstmt.setString(5, detalle.getDescripcion());
            pstmt.setInt(6, detalle.getIdDetalleVentaProducto());

            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Filas afectadas al actualizar detalle de venta de producto: " + filasAfectadas);
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar detalle de venta de producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un detalle de venta de producto.
     * @param idDetalle El ID del detalle a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public boolean eliminarDetalleVentaProducto(int idDetalle) {
        String sql = "DELETE FROM detalle_venta_producto WHERE id_detalle_venta_producto = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idDetalle);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Detalle de venta de producto con ID " + idDetalle + " eliminado.");
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar detalle de venta de producto: " + e.getMessage());
            return false;
        }
    }


    /**
     * Obtiene una lista de todos los detalles de venta de producto.
     * @return Una lista de objetos DetalleVentaProducto.
     */
    public List<DetalleVentaProducto> obtenerTodosLosDetallesVentaProducto() {
        String sql = "SELECT id_detalle_venta_producto, id_venta, id_producto, cantidad, " +
                "fecha_creacion, fecha_actualizacion, precio_unitario_final, descripcion FROM detalle_venta_producto ORDER BY id_venta, id_detalle_venta_producto";
        List<DetalleVentaProducto> detalles = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                detalles.add(mapearResultSetADetalleVentaProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los detalles de venta de producto: " + e.getMessage());
        }
        return detalles;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto DetalleVentaProducto.
     * @param rs El ResultSet actual.
     * @return Un objeto DetalleVentaProducto con los datos del ResultSet.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    private DetalleVentaProducto mapearResultSetADetalleVentaProducto(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_detalle_venta_producto");
        int idVenta = rs.getInt("id_venta");
        int idProducto = rs.getInt("id_producto");
        int cantidad = rs.getInt("cantidad");

        Timestamp tsCreacion = rs.getTimestamp("fecha_creacion");
        LocalDateTime fechaCreacion = (tsCreacion != null) ? tsCreacion.toLocalDateTime() : null;

        Timestamp tsActualizacion = rs.getTimestamp("fecha_actualizacion");
        LocalDateTime fechaActualizacion = (tsActualizacion != null) ? tsActualizacion.toLocalDateTime() : null;

        BigDecimal precioUnitarioFinal = rs.getBigDecimal("precio_unitario_final");
        String descripcion = rs.getString("descripcion"); // Puede ser nula

        return new DetalleVentaProducto(id, idVenta, idProducto, cantidad,
                fechaCreacion, fechaActualizacion, precioUnitarioFinal, descripcion);
    }
}