package com.cerrajeria.app.dao;

import com.cerrajeria.app.database.DatabaseManager;
import com.cerrajeria.app.models.Comision;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO (Data Access Object) para interactuar con la tabla 'comision' en la base de datos.
 */
public class ComisionDAO {

    /**
     * Inserta una nueva comisión en la base de datos.
     * @param comision El objeto Comision a insertar.
     * @return El ID de la comisión recién insertada, o -1 si hubo un error.
     */
    public int insertarComision(Comision comision) {
        String sql = "INSERT INTO comision (id_usuario, id_venta, id_servicio, monto_comision, estado, comentario, es_manual, comentario_admin) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int idGenerado = -1;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, comision.getIdUsuario());
            // setNull si el ID es nulo, setInt si tiene valor
            if (comision.getIdVenta() != null) {
                pstmt.setInt(2, comision.getIdVenta());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            if (comision.getIdServicio() != null) {
                pstmt.setInt(3, comision.getIdServicio());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setBigDecimal(4, comision.getMontoComision());
            pstmt.setString(5, comision.getEstado());
            pstmt.setString(6, comision.getComentario());
            pstmt.setBoolean(7, comision.isEsManual());
            pstmt.setString(8, comision.getComentarioAdmin());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        comision.setIdComision(idGenerado); // Actualiza el objeto
                        System.out.println("Comisión insertada con ID: " + idGenerado);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar comisión: " + e.getMessage());
        }
        return idGenerado;
    }

    /**
     * Obtiene una comisión por su ID.
     * @param idComision El ID de la comisión a buscar.
     * @return El objeto Comision si se encuentra, o null.
     */
    public Comision obtenerComisionPorId(int idComision) {
        String sql = "SELECT id_comision, id_usuario, id_venta, id_servicio, monto_comision, estado, " +
                "comentario, es_manual, comentario_admin, fecha_creacion, fecha_actualizacion FROM comision WHERE id_comision = ?";
        Comision comision = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idComision);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    comision = mapearResultSetAComision(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener comisión por ID: " + e.getMessage());
        }
        return comision;
    }

    /**
     * Obtiene una lista de comisiones para un usuario específico.
     * @param idUsuario El ID del usuario.
     * @return Una lista de objetos Comision.
     */
    public List<Comision> obtenerComisionesPorUsuario(int idUsuario) {
        String sql = "SELECT id_comision, id_usuario, id_venta, id_servicio, monto_comision, estado, " +
                "comentario, es_manual, comentario_admin, fecha_creacion, fecha_actualizacion FROM comision WHERE id_usuario = ? ORDER BY fecha_creacion DESC";
        List<Comision> comisiones = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    comisiones.add(mapearResultSetAComision(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener comisiones por usuario: " + e.getMessage());
        }
        return comisiones;
    }

    /**
     * Obtiene una lista de comisiones por estado (ej. 'Pendiente', 'Pagado').
     * @param estado El estado de la comisión.
     * @return Una lista de objetos Comision.
     */
    public List<Comision> obtenerComisionesPorEstado(String estado) {
        String sql = "SELECT id_comision, id_usuario, id_venta, id_servicio, monto_comision, estado, " +
                "comentario, es_manual, comentario_admin, fecha_creacion, fecha_actualizacion FROM comision WHERE estado = ? ORDER BY fecha_creacion DESC";
        List<Comision> comisiones = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, estado);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    comisiones.add(mapearResultSetAComision(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener comisiones por estado: " + e.getMessage());
        }
        return comisiones;
    }

    /**
     * Actualiza el estado y/o el comentario del administrador de una comisión.
     * @param comision El objeto Comision con los datos actualizados.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarComision(Comision comision) {
        String sql = "UPDATE comision SET id_usuario = ?, id_venta = ?, id_servicio = ?, monto_comision = ?, " +
                "estado = ?, comentario = ?, es_manual = ?, comentario_admin = ?, fecha_actualizacion = GETDATE() WHERE id_comision = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, comision.getIdUsuario());
            if (comision.getIdVenta() != null) {
                pstmt.setInt(2, comision.getIdVenta());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            if (comision.getIdServicio() != null) {
                pstmt.setInt(3, comision.getIdServicio());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setBigDecimal(4, comision.getMontoComision());
            pstmt.setString(5, comision.getEstado());
            pstmt.setString(6, comision.getComentario());
            pstmt.setBoolean(7, comision.isEsManual());
            pstmt.setString(8, comision.getComentarioAdmin());
            pstmt.setInt(9, comision.getIdComision());

            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Filas afectadas al actualizar comisión: " + filasAfectadas);
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar comisión: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina una comisión.
     * @param idComision El ID de la comisión a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public boolean eliminarComision(int idComision) {
        String sql = "DELETE FROM comision WHERE id_comision = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idComision);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Comisión con ID " + idComision + " eliminada.");
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar comisión: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene una lista de todas las comisiones.
     * @return Una lista de objetos Comision.
     */
    public List<Comision> obtenerTodasLasComisiones() {
        String sql = "SELECT id_comision, id_usuario, id_venta, id_servicio, monto_comision, estado, " +
                "comentario, es_manual, comentario_admin, fecha_creacion, fecha_actualizacion FROM comision ORDER BY fecha_creacion DESC";
        List<Comision> comisiones = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                comisiones.add(mapearResultSetAComision(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todas las comisiones: " + e.getMessage());
        }
        return comisiones;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto Comision.
     * @param rs El ResultSet actual.
     * @return Un objeto Comision con los datos del ResultSet.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    private Comision mapearResultSetAComision(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_comision");
        int idUsuario = rs.getInt("id_usuario");

        // Para columnas NULLABLE (id_venta, id_servicio), usar rs.getObject y castear a Integer
        Integer idVenta = (Integer) rs.getObject("id_venta");
        Integer idServicio = (Integer) rs.getObject("id_servicio");

        BigDecimal montoComision = rs.getBigDecimal("monto_comision");
        String estado = rs.getString("estado");
        String comentario = rs.getString("comentario");
        boolean esManual = rs.getBoolean("es_manual");
        String comentarioAdmin = rs.getString("comentario_admin"); // Puede ser nulo

        Timestamp tsCreacion = rs.getTimestamp("fecha_creacion");
        LocalDateTime fechaCreacion = (tsCreacion != null) ? tsCreacion.toLocalDateTime() : null;

        Timestamp tsActualizacion = rs.getTimestamp("fecha_actualizacion");
        LocalDateTime fechaActualizacion = (tsActualizacion != null) ? tsActualizacion.toLocalDateTime() : null;

        return new Comision(id, idUsuario, idVenta, idServicio, montoComision, estado,
                comentario, esManual, comentarioAdmin, fechaCreacion, fechaActualizacion);
    }
}