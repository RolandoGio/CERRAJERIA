package com.cerrajeria.app.dao;

import com.cerrajeria.app.database.DatabaseManager;
import com.cerrajeria.app.models.ControlFinanciero;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para interactuar con la tabla 'control_financiero' en la base de datos.
 */
public class ControlFinancieroDAO {

    public int insertarControlFinanciero(ControlFinanciero registro) {
        String sql = "INSERT INTO control_financiero (tipo, descripcion, monto, costo) VALUES (?, ?, ?, ?)";
        int idGenerado = -1;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, registro.getTipo());
            pstmt.setString(2, registro.getDescripcion());
            pstmt.setBigDecimal(3, registro.getMonto());
            pstmt.setBigDecimal(4, registro.getCosto() != null ? registro.getCosto() : BigDecimal.ZERO);

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        registro.setIdControlFinanciero(idGenerado);
                        System.out.println("Registro financiero insertado con ID: " + idGenerado);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar registro financiero: " + e.getMessage());
        }
        return idGenerado;
    }

    public ControlFinanciero obtenerControlFinancieroPorId(int idRegistro) {
        String sql = "SELECT * FROM control_financiero WHERE id_control_financiero = ?";
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

    public List<ControlFinanciero> obtenerControlFinancieroPorTipo(String tipo) {
        String sql = "SELECT * FROM control_financiero WHERE tipo = ? ORDER BY fecha_creacion DESC";
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

    public List<ControlFinanciero> obtenerTodosLosRegistrosFinancieros() {
        String sql = "SELECT * FROM control_financiero ORDER BY fecha_creacion DESC";
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

    public boolean actualizarControlFinanciero(ControlFinanciero registro) {
        String sql = "UPDATE control_financiero SET tipo = ?, descripcion = ?, monto = ?, costo = ?, fecha_actualizacion = GETDATE() WHERE id_control_financiero = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, registro.getTipo());
            pstmt.setString(2, registro.getDescripcion());
            pstmt.setBigDecimal(3, registro.getMonto());
            pstmt.setBigDecimal(4, registro.getCosto() != null ? registro.getCosto() : BigDecimal.ZERO);
            pstmt.setInt(5, registro.getIdControlFinanciero());

            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Filas afectadas al actualizar: " + filasAfectadas);
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarControlFinanciero(int idRegistro) {
        String sql = "DELETE FROM control_financiero WHERE id_control_financiero = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idRegistro);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Registro eliminado: ID " + idRegistro);
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar: " + e.getMessage());
            return false;
        }
    }

    private ControlFinanciero mapearResultSetAControlFinanciero(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_control_financiero");
        String tipo = rs.getString("tipo");
        String descripcion = rs.getString("descripcion");
        BigDecimal monto = rs.getBigDecimal("monto");
        BigDecimal costo = rs.getBigDecimal("costo");

        Timestamp tsCreacion = rs.getTimestamp("fecha_creacion");
        LocalDateTime fechaCreacion = (tsCreacion != null) ? tsCreacion.toLocalDateTime() : null;

        Timestamp tsActualizacion = rs.getTimestamp("fecha_actualizacion");
        LocalDateTime fechaActualizacion = (tsActualizacion != null) ? tsActualizacion.toLocalDateTime() : null;

        return new ControlFinanciero(id, tipo, descripcion, monto, costo, fechaCreacion, fechaActualizacion);
    }

    public List<ControlFinanciero> obtenerRegistrosPorPeriodo(LocalDate date, String tipoFiltro) {
        List<ControlFinanciero> lista = new ArrayList<>();
        String sql = "SELECT * FROM control_financiero WHERE ";

        switch (tipoFiltro) {
            case "Día":
                sql += "CONVERT(DATE, fecha_creacion) = ?";
                break;
            case "Semana":
                sql += "DATEPART(YEAR, fecha_creacion) = YEAR(?) AND DATEPART(WEEK, fecha_creacion) = DATEPART(WEEK, ?)";
                break;
            case "Mes":
                sql += "YEAR(fecha_creacion) = YEAR(?) AND MONTH(fecha_creacion) = MONTH(?)";
                break;
            case "Trimestre":
                sql += "YEAR(fecha_creacion) = YEAR(?) AND DATEPART(QUARTER, fecha_creacion) = DATEPART(QUARTER, ?)";
                break;
            case "Año":
                sql += "YEAR(fecha_creacion) = YEAR(?)";
                break;
            default:
                throw new IllegalArgumentException("Tipo de filtro inválido: " + tipoFiltro);
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(date));
            if (!tipoFiltro.equals("Año")) {
                pstmt.setDate(2, Date.valueOf(date));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSetAControlFinanciero(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al filtrar registros: " + e.getMessage());
        }

        return lista;
    }
}
