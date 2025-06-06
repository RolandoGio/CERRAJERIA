package com.cerrajeria.app.dao;

import com.cerrajeria.app.database.DatabaseManager;
import com.cerrajeria.app.models.Venta;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO (Data Access Object) para interactuar con la tabla 'venta' en la base de datos.
 */
public class VentaDAO {

    /**
     * Inserta una nueva venta en la base de datos.
     * @param venta El objeto Venta a insertar.
     * @return El ID de la venta recién insertada, o -1 si hubo un error.
     */
    public int insertarVenta(Venta venta) {
        // total_venta tiene un DEFAULT en la DB, pero lo podemos pasar si ya lo tenemos calculado.
        // Para la inserción inicial, solo necesitamos id_usuario.
        String sql = "INSERT INTO venta (id_usuario) VALUES (?)";
        int idGenerado = -1;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, venta.getIdUsuario());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        venta.setIdVenta(idGenerado); // Actualiza el objeto
                        System.out.println("Venta insertada con ID: " + idGenerado);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar venta: " + e.getMessage());
        }
        return idGenerado;
    }

    /**
     * Obtiene una venta por su ID.
     * @param idVenta El ID de la venta a buscar.
     * @return El objeto Venta si se encuentra, o null.
     */
    public Venta obtenerVentaPorId(int idVenta) {
        String sql = "SELECT id_venta, id_usuario, fecha_creacion, fecha_actualizacion, total_venta FROM venta WHERE id_venta = ?";
        Venta venta = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idVenta);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    venta = mapearResultSetAVenta(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener venta por ID: " + e.getMessage());
        }
        return venta;
    }

    /**
     * Obtiene una lista de ventas realizadas por un usuario específico.
     * @param idUsuario El ID del usuario (vendedor).
     * @return Una lista de objetos Venta.
     */
    public List<Venta> obtenerVentasPorUsuario(int idUsuario) {
        String sql = "SELECT id_venta, id_usuario, fecha_creacion, fecha_actualizacion, total_venta FROM venta WHERE id_usuario = ? ORDER BY fecha_creacion DESC";
        List<Venta> ventas = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ventas.add(mapearResultSetAVenta(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas por usuario: " + e.getMessage());
        }
        return ventas;
    }

    /**
     * Actualiza el total de una venta. Este método es útil después de insertar/actualizar detalles
     * y que el trigger de la DB recalcule el total. Simplemente lo refresca en el objeto Venta.
     * @param idVenta El ID de la venta a actualizar.
     * @param nuevoTotal El nuevo total a establecer.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarTotalVenta(int idVenta, BigDecimal nuevoTotal) {
        String sql = "UPDATE venta SET total_venta = ?, fecha_actualizacion = GETDATE() WHERE id_venta = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, nuevoTotal);
            pstmt.setInt(2, idVenta);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Total de venta actualizado para Venta ID " + idVenta + " a " + nuevoTotal);
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar total de venta: " + e.getMessage());
            return false;
        }
    }


    /**
     * Obtiene una lista de todas las ventas.
     * @return Una lista de objetos Venta.
     */
    public List<Venta> obtenerTodasLasVentas() {
        String sql = "SELECT id_venta, id_usuario, fecha_creacion, fecha_actualizacion, total_venta FROM venta ORDER BY fecha_creacion DESC";
        List<Venta> ventas = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ventas.add(mapearResultSetAVenta(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todas las ventas: " + e.getMessage());
        }
        return ventas;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto Venta.
     * @param rs El ResultSet actual.
     * @return Un objeto Venta con los datos del ResultSet.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    private Venta mapearResultSetAVenta(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_venta");
        int idUsuario = rs.getInt("id_usuario");

        Timestamp tsCreacion = rs.getTimestamp("fecha_creacion");
        LocalDateTime fechaCreacion = (tsCreacion != null) ? tsCreacion.toLocalDateTime() : null;

        Timestamp tsActualizacion = rs.getTimestamp("fecha_actualizacion");
        LocalDateTime fechaActualizacion = (tsActualizacion != null) ? tsActualizacion.toLocalDateTime() : null;

        BigDecimal totalVenta = rs.getBigDecimal("total_venta");

        return new Venta(id, idUsuario, fechaCreacion, fechaActualizacion, totalVenta);
    }
}