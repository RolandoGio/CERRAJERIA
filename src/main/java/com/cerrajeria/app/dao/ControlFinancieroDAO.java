package com.cerrajeria.app.dao;

import com.cerrajeria.app.database.DatabaseManager;
import com.cerrajeria.app.models.ControlFinanciero;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO (Data Access Object) para interactuar con la tabla 'control_financiero' en la base de datos.
 */
public class ControlFinancieroDAO {

    /**
     * Inserta un nuevo registro de control financiero.
     * @param registro El objeto ControlFinanciero a insertar.
     * @return El ID del registro recién insertado, o -1 si hubo un error.
     */
    public int insertarControlFinanciero(ControlFinanciero registro) {
        String sql = "INSERT INTO control_financiero (tipo, descripcion, monto) VALUES (?, ?, ?)";
        int idGenerado = -1;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, registro.getTipo());
            pstmt.setString(2, registro.getDescripcion());
            pstmt.setBigDecimal(3, registro.getMonto());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        registro.setIdControlFinanciero(idGenerado); // Actualiza el objeto
                        System.out.println("Registro financiero insertado con ID: " + idGenerado);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar registro financiero: " + e.getMessage());
        }
        return idGenerado;
    }

    /**
     * Obtiene un registro de control financiero por su ID.
     * @param idRegistro El ID del registro a buscar.
     * @return El objeto ControlFinanciero si se encuentra, o null.
     */
    public ControlFinanciero obtenerControlFinancieroPorId(int idRegistro) {
        String sql = "SELECT id_control_financiero, tipo, descripcion, monto, fecha_creacion, fecha_actualizacion FROM control_financiero WHERE id_control_financiero = ?";
        ControlFinanciero registro = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idRegistro);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    registro = mapearResultSetAControlFinanciero(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener registro financiero por ID: " + e.getMessage());
        }
        return registro;
    }

    /**
     * Obtiene una lista de registros de control financiero por tipo ('Ingreso' o 'Egreso').
     * @param tipo El tipo de registro a buscar.
     * @return Una lista de objetos ControlFinanciero.
     */
    public List<ControlFinanciero> obtenerControlFinancieroPorTipo(String tipo) {
        String sql = "SELECT id_control_financiero, tipo, descripcion, monto, fecha_creacion, fecha_actualizacion FROM control_financiero WHERE tipo = ? ORDER BY fecha_creacion DESC";
        List<ControlFinanciero> registros = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tipo);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    registros.add(mapearResultSetAControlFinanciero(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener registros financieros por tipo: " + e.getMessage());
        }
        return registros;
    }

    /**
     * Obtiene una lista de todos los registros de control financiero.
     * @return Una lista de objetos ControlFinanciero.
     */
    public List<ControlFinanciero> obtenerTodosLosRegistrosFinancieros() {
        String sql = "SELECT id_control_financiero, tipo, descripcion, monto, fecha_creacion, fecha_actualizacion FROM control_financiero ORDER BY fecha_creacion DESC";
        List<ControlFinanciero> registros = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                registros.add(mapearResultSetAControlFinanciero(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los registros financieros: " + e.getMessage());
        }
        return registros;
    }

    /**
     * Actualiza los datos de un registro de control financiero existente.
     * @param registro El objeto ControlFinanciero con los datos actualizados.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarControlFinanciero(ControlFinanciero registro) {
        String sql = "UPDATE control_financiero SET tipo = ?, descripcion = ?, monto = ?, fecha_actualizacion = GETDATE() WHERE id_control_financiero = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, registro.getTipo());
            pstmt.setString(2, registro.getDescripcion());
            pstmt.setBigDecimal(3, registro.getMonto());
            pstmt.setInt(4, registro.getIdControlFinanciero());

            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Filas afectadas al actualizar registro financiero: " + filasAfectadas);
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar registro financiero: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un registro de control financiero.
     * @param idRegistro El ID del registro a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public boolean eliminarControlFinanciero(int idRegistro) {
        String sql = "DELETE FROM control_financiero WHERE id_control_financiero = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idRegistro);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Registro financiero con ID " + idRegistro + " eliminado.");
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar registro financiero: " + e.getMessage());
            return false;
        }
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto ControlFinanciero.
     * @param rs El ResultSet actual.
     * @return Un objeto ControlFinanciero con los datos del ResultSet.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    private ControlFinanciero mapearResultSetAControlFinanciero(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_control_financiero");
        String tipo = rs.getString("tipo");
        String descripcion = rs.getString("descripcion");
        BigDecimal monto = rs.getBigDecimal("monto");

        Timestamp tsCreacion = rs.getTimestamp("fecha_creacion");
        LocalDateTime fechaCreacion = (tsCreacion != null) ? tsCreacion.toLocalDateTime() : null;

        Timestamp tsActualizacion = rs.getTimestamp("fecha_actualizacion");
        LocalDateTime fechaActualizacion = (tsActualizacion != null) ? tsActualizacion.toLocalDateTime() : null;

        return new ControlFinanciero(id, tipo, descripcion, monto, fechaCreacion, fechaActualizacion);
    }
}