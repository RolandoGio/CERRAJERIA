package com.cerrajeria.app.dao;

import com.cerrajeria.app.database.DatabaseManager;
import com.cerrajeria.app.models.DetalleVentaServicio;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO (Data Access Object) para interactuar con la tabla 'detalle_venta_servicio' en la base de datos.
 */
public class DetalleVentaServicioDAO {

    /**
     * Inserta un nuevo detalle de venta de servicio en la base de datos.
     * @param detalle El objeto DetalleVentaServicio a insertar.
     * @return El ID del detalle recién insertado, o -1 si hubo un error.
     */
    public int insertarDetalleVentaServicio(DetalleVentaServicio detalle) {
        String sql = "INSERT INTO detalle_venta_servicio (id_venta, id_servicio, cantidad, precio_unitario_final, descripcion) VALUES (?, ?, ?, ?, ?)";
        int idGenerado = -1;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, detalle.getIdVenta());
            pstmt.setInt(2, detalle.getIdServicio());
            pstmt.setInt(3, detalle.getCantidad());
            pstmt.setBigDecimal(4, detalle.getPrecioUnitarioFinal());
            pstmt.setString(5, detalle.getDescripcion());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        detalle.setIdDetalleVentaServicio(idGenerado); // Actualiza el objeto
                        System.out.println("Detalle de venta de servicio insertado con ID: " + idGenerado);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar detalle de venta de servicio: " + e.getMessage());
        }
        return idGenerado;
    }

    /**
     * Obtiene un detalle de venta de servicio por su ID.
     * @param idDetalle El ID del detalle a buscar.
     * @return El objeto DetalleVentaServicio si se encuentra, o null.
     */
    public DetalleVentaServicio obtenerDetalleVentaServicioPorId(int idDetalle) {
        String sql = "SELECT id_detalle_venta_servicio, id_venta, id_servicio, cantidad, " +
                "fecha_creacion, fecha_actualizacion, precio_unitario_final, descripcion FROM detalle_venta_servicio WHERE id_detalle_venta_servicio = ?";
        DetalleVentaServicio detalle = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idDetalle);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    detalle = mapearResultSetADetalleVentaServicio(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener detalle de venta de servicio por ID: " + e.getMessage());
        }
        return detalle;
    }

    /**
     * Obtiene una lista de detalles de venta de servicio para una venta específica.
     * @param idVenta El ID de la venta.
     * @return Una lista de objetos DetalleVentaServicio.
     */
    public List<DetalleVentaServicio> obtenerDetallesPorVenta(int idVenta) {
        String sql = "SELECT id_detalle_venta_servicio, id_venta, id_servicio, cantidad, " +
                "fecha_creacion, fecha_actualizacion, precio_unitario_final, descripcion FROM detalle_venta_servicio WHERE id_venta = ? ORDER BY id_detalle_venta_servicio";
        List<DetalleVentaServicio> detalles = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idVenta);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    detalles.add(mapearResultSetADetalleVentaServicio(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener detalles de venta de servicio por venta: " + e.getMessage());
        }
        return detalles;
    }

    /**
     * Actualiza los datos de un detalle de venta de servicio existente.
     * @param detalle El objeto DetalleVentaServicio con los datos actualizados (el ID debe estar establecido).
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarDetalleVentaServicio(DetalleVentaServicio detalle) {
        String sql = "UPDATE detalle_venta_servicio SET id_venta = ?, id_servicio = ?, cantidad = ?, precio_unitario_final = ?, descripcion = ?, fecha_actualizacion = GETDATE() WHERE id_detalle_venta_servicio = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, detalle.getIdVenta());
            pstmt.setInt(2, detalle.getIdServicio());
            pstmt.setInt(3, detalle.getCantidad());
            pstmt.setBigDecimal(4, detalle.getPrecioUnitarioFinal());
            pstmt.setString(5, detalle.getDescripcion());
            pstmt.setInt(6, detalle.getIdDetalleVentaServicio());

            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Filas afectadas al actualizar detalle de venta de servicio: " + filasAfectadas);
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar detalle de venta de servicio: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un detalle de venta de servicio.
     * @param idDetalle El ID del detalle a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public boolean eliminarDetalleVentaServicio(int idDetalle) {
        String sql = "DELETE FROM detalle_venta_servicio WHERE id_detalle_venta_servicio = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idDetalle);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Detalle de venta de servicio con ID " + idDetalle + " eliminado.");
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar detalle de venta de servicio: " + e.getMessage());
            return false;
        }
    }


    /**
     * Obtiene una lista de todos los detalles de venta de servicio.
     * @return Una lista de objetos DetalleVentaServicio.
     */
    public List<DetalleVentaServicio> obtenerTodosLosDetallesVentaServicio() {
        String sql = "SELECT id_detalle_venta_servicio, id_venta, id_servicio, cantidad, " +
                "fecha_creacion, fecha_actualizacion, precio_unitario_final, descripcion FROM detalle_venta_servicio ORDER BY id_venta, id_detalle_venta_servicio";
        List<DetalleVentaServicio> detalles = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                detalles.add(mapearResultSetADetalleVentaServicio(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los detalles de venta de servicio: " + e.getMessage());
        }
        return detalles;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto DetalleVentaServicio.
     * @param rs El ResultSet actual.
     * @return Un objeto DetalleVentaServicio con los datos del ResultSet.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    private DetalleVentaServicio mapearResultSetADetalleVentaServicio(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_detalle_venta_servicio");
        int idVenta = rs.getInt("id_venta");
        int idServicio = rs.getInt("id_servicio");
        int cantidad = rs.getInt("cantidad");

        Timestamp tsCreacion = rs.getTimestamp("fecha_creacion");
        LocalDateTime fechaCreacion = (tsCreacion != null) ? tsCreacion.toLocalDateTime() : null;

        Timestamp tsActualizacion = rs.getTimestamp("fecha_actualizacion");
        LocalDateTime fechaActualizacion = (tsActualizacion != null) ? tsActualizacion.toLocalDateTime() : null;

        BigDecimal precioUnitarioFinal = rs.getBigDecimal("precio_unitario_final");
        String descripcion = rs.getString("descripcion");

        return new DetalleVentaServicio(id, idVenta, idServicio, cantidad,
                fechaCreacion, fechaActualizacion, precioUnitarioFinal, descripcion);
    }
}