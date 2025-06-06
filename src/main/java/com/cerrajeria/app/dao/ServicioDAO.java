package com.cerrajeria.app.dao;

import com.cerrajeria.app.database.DatabaseManager;
import com.cerrajeria.app.models.Servicio;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO (Data Access Object) para interactuar con la tabla 'servicio' en la base de datos.
 */
public class ServicioDAO {

    /**
     * Inserta un nuevo servicio en la base de datos.
     * @param servicio El objeto Servicio a insertar.
     * @return El ID del servicio recién insertado, o -1 si hubo un error.
     */
    public int insertarServicio(Servicio servicio) {
        String sql = "INSERT INTO servicio (nombre, descripcion, precio, id_categoria_servicio, activo) VALUES (?, ?, ?, ?, ?)";
        int idGenerado = -1;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, servicio.getNombre());
            pstmt.setString(2, servicio.getDescripcion());
            pstmt.setBigDecimal(3, servicio.getPrecio());
            pstmt.setInt(4, servicio.getIdCategoriaServicio());
            pstmt.setBoolean(5, servicio.isActivo());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        servicio.setIdServicio(idGenerado); // Actualiza el objeto
                        System.out.println("Servicio insertado con ID: " + idGenerado);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar servicio: " + e.getMessage());
        }
        return idGenerado;
    }

    /**
     * Obtiene un servicio por su ID.
     * @param idServicio El ID del servicio a buscar.
     * @return El objeto Servicio si se encuentra, o null.
     */
    public Servicio obtenerServicioPorId(int idServicio) {
        String sql = "SELECT id_servicio, nombre, descripcion, precio, id_categoria_servicio, " +
                "fecha_creacion, fecha_actualizacion, activo FROM servicio WHERE id_servicio = ?";
        Servicio servicio = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idServicio);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    servicio = mapearResultSetAServicio(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener servicio por ID: " + e.getMessage());
        }
        return servicio;
    }

    /**
     * Obtiene una lista de servicios por nombre (útil para búsquedas).
     * @param nombre El nombre del servicio o parte de él.
     * @return Una lista de objetos Servicio.
     */
    public List<Servicio> obtenerServiciosPorNombre(String nombre) {
        String sql = "SELECT id_servicio, nombre, descripcion, precio, id_categoria_servicio, " +
                "fecha_creacion, fecha_actualizacion, activo FROM servicio WHERE nombre LIKE ? ORDER BY nombre";
        List<Servicio> servicios = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + nombre + "%"); // Búsqueda parcial

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    servicios.add(mapearResultSetAServicio(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener servicios por nombre: " + e.getMessage());
        }
        return servicios;
    }

    /**
     * Obtiene una lista de servicios por ID de categoría de servicio.
     * @param idCategoriaServicio El ID de la categoría de servicio.
     * @return Una lista de objetos Servicio.
     */
    public List<Servicio> obtenerServiciosPorCategoria(int idCategoriaServicio) {
        String sql = "SELECT id_servicio, nombre, descripcion, precio, id_categoria_servicio, " +
                "fecha_creacion, fecha_actualizacion, activo FROM servicio WHERE id_categoria_servicio = ? ORDER BY nombre";
        List<Servicio> servicios = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCategoriaServicio);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    servicios.add(mapearResultSetAServicio(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener servicios por categoría: " + e.getMessage());
        }
        return servicios;
    }

    /**
     * Actualiza los datos de un servicio existente.
     * @param servicio El objeto Servicio con los datos actualizados (el ID debe estar establecido).
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarServicio(Servicio servicio) {
        String sql = "UPDATE servicio SET nombre = ?, descripcion = ?, precio = ?, id_categoria_servicio = ?, " +
                "activo = ?, fecha_actualizacion = GETDATE() WHERE id_servicio = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, servicio.getNombre());
            pstmt.setString(2, servicio.getDescripcion());
            pstmt.setBigDecimal(3, servicio.getPrecio());
            pstmt.setInt(4, servicio.getIdCategoriaServicio());
            pstmt.setBoolean(5, servicio.isActivo());
            pstmt.setInt(6, servicio.getIdServicio());

            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Filas afectadas al actualizar servicio: " + filasAfectadas);
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar servicio: " + e.getMessage());
            return false;
        }
    }

    /**
     * Desactiva lógicamente un servicio (establece 'activo' en 0/false).
     * @param idServicio El ID del servicio a desactivar.
     * @return true si la desactivación fue exitosa, false en caso contrario.
     */
    public boolean desactivarServicio(int idServicio) {
        String sql = "UPDATE servicio SET activo = 0, fecha_actualizacion = GETDATE() WHERE id_servicio = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idServicio);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Servicio con ID " + idServicio + " desactivado.");
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al desactivar servicio: " + e.getMessage());
            return false;
        }
    }

    /**
     * Activa lógicamente un servicio (establece 'activo' en 1/true).
     * @param idServicio El ID del servicio a activar.
     * @return true si la activación fue exitosa, false en caso contrario.
     */
    public boolean activarServicio(int idServicio) {
        String sql = "UPDATE servicio SET activo = 1, fecha_actualizacion = GETDATE() WHERE id_servicio = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idServicio);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Servicio con ID " + idServicio + " activado.");
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al activar servicio: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene una lista de todos los servicios (activos e inactivos).
     * @return Una lista de objetos Servicio.
     */
    public List<Servicio> obtenerTodosLosServicios() {
        String sql = "SELECT id_servicio, nombre, descripcion, precio, id_categoria_servicio, " +
                "fecha_creacion, fecha_actualizacion, activo FROM servicio";
        List<Servicio> servicios = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                servicios.add(mapearResultSetAServicio(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los servicios: " + e.getMessage());
        }
        return servicios;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto Servicio.
     * @param rs El ResultSet actual.
     * @return Un objeto Servicio con los datos del ResultSet.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    private Servicio mapearResultSetAServicio(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_servicio");
        String nombre = rs.getString("nombre");
        String descripcion = rs.getString("descripcion");
        BigDecimal precio = rs.getBigDecimal("precio");
        int idCategoriaServicio = rs.getInt("id_categoria_servicio"); // Obtener ID de categoría

        Timestamp tsCreacion = rs.getTimestamp("fecha_creacion");
        LocalDateTime fechaCreacion = (tsCreacion != null) ? tsCreacion.toLocalDateTime() : null;

        Timestamp tsActualizacion = rs.getTimestamp("fecha_actualizacion");
        LocalDateTime fechaActualizacion = (tsActualizacion != null) ? tsActualizacion.toLocalDateTime() : null;

        boolean activo = rs.getBoolean("activo");

        return new Servicio(id, nombre, descripcion, precio, idCategoriaServicio,
                fechaCreacion, fechaActualizacion, activo);
    }
}