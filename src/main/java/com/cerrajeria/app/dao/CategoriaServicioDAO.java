package com.cerrajeria.app.dao;

import com.cerrajeria.app.database.DatabaseManager;
import com.cerrajeria.app.models.CategoriaServicio;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO (Data Access Object) para interactuar con la tabla 'categoria_servicio' en la base de datos.
 */
public class CategoriaServicioDAO {

    /**
     * Inserta una nueva categoría de servicio en la base de datos.
     * @param categoria La categoría a insertar.
     * @return El ID de la categoría recién insertada, o -1 si hubo un error.
     */
    public int insertarCategoriaServicio(CategoriaServicio categoria) {
        String sql = "INSERT INTO categoria_servicio (nombre, activo) VALUES (?, ?)";
        int idGenerado = -1;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, categoria.getNombre());
            pstmt.setBoolean(2, categoria.isActivo());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        categoria.setIdCategoriaServicio(idGenerado);
                        System.out.println("Categoría de servicio insertada con ID: " + idGenerado);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar categoría de servicio: " + e.getMessage());
        }
        return idGenerado;
    }

    /**
     * Obtiene una categoría de servicio por su ID.
     * @param idCategoria El ID de la categoría a buscar.
     * @return El objeto CategoriaServicio si se encuentra, o null.
     */
    public CategoriaServicio obtenerCategoriaServicioPorId(int idCategoria) {
        String sql = "SELECT id_categoria_servicio, nombre, fecha_creacion, fecha_actualizacion, activo FROM categoria_servicio WHERE id_categoria_servicio = ?";
        CategoriaServicio categoria = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCategoria);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    categoria = mapearResultSetACategoriaServicio(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener categoría de servicio por ID: " + e.getMessage());
        }
        return categoria;
    }

    /**
     * Obtiene una categoría de servicio por su nombre.
     * @param nombre El nombre de la categoría a buscar.
     * @return El objeto CategoriaServicio si se encuentra, o null.
     */
    public CategoriaServicio obtenerCategoriaServicioPorNombre(String nombre) {
        String sql = "SELECT id_categoria_servicio, nombre, fecha_creacion, fecha_actualizacion, activo FROM categoria_servicio WHERE nombre = ?";
        CategoriaServicio categoria = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    categoria = mapearResultSetACategoriaServicio(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener categoría de servicio por nombre: " + e.getMessage());
        }
        return categoria;
    }

    /**
     * Actualiza los datos de una categoría de servicio existente.
     * @param categoria La categoría con los datos actualizados.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarCategoriaServicio(CategoriaServicio categoria) {
        String sql = "UPDATE categoria_servicio SET nombre = ?, activo = ?, fecha_actualizacion = GETDATE() WHERE id_categoria_servicio = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoria.getNombre());
            pstmt.setBoolean(2, categoria.isActivo());
            pstmt.setInt(3, categoria.getIdCategoriaServicio());

            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Filas afectadas al actualizar categoría de servicio: " + filasAfectadas);
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar categoría de servicio: " + e.getMessage());
            return false;
        }
    }

    /**
     * Desactiva lógicamente una categoría de servicio (establece 'activo' en 0/false).
     * @param idCategoria El ID de la categoría a desactivar.
     * @return true si la desactivación fue exitosa, false en caso contrario.
     */
    public boolean desactivarCategoriaServicio(int idCategoria) {
        String sql = "UPDATE categoria_servicio SET activo = 0, fecha_actualizacion = GETDATE() WHERE id_categoria_servicio = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCategoria);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Categoría de servicio con ID " + idCategoria + " desactivada.");
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al desactivar categoría de servicio: " + e.getMessage());
            return false;
        }
    }

    /**
     * Activa lógicamente una categoría de servicio (establece 'activo' en 1/true).
     * @param idCategoria El ID de la categoría a activar.
     * @return true si la activación fue exitosa, false en caso contrario.
     */
    public boolean activarCategoriaServicio(int idCategoria) {
        String sql = "UPDATE categoria_servicio SET activo = 1, fecha_actualizacion = GETDATE() WHERE id_categoria_servicio = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCategoria);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Categoría de servicio con ID " + idCategoria + " activada.");
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al activar categoría de servicio: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene una lista de todas las categorías de servicio (activas e inactivas).
     * @return Una lista de objetos CategoriaServicio.
     */
    public List<CategoriaServicio> obtenerTodasCategoriasServicio() {
        String sql = "SELECT id_categoria_servicio, nombre, fecha_creacion, fecha_actualizacion, activo FROM categoria_servicio";
        List<CategoriaServicio> categorias = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categorias.add(mapearResultSetACategoriaServicio(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todas las categorías de servicio: " + e.getMessage());
        }
        return categorias;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto CategoriaServicio.
     * @param rs El ResultSet actual.
     * @return Un objeto CategoriaServicio con los datos del ResultSet.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    private CategoriaServicio mapearResultSetACategoriaServicio(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_categoria_servicio");
        String nombre = rs.getString("nombre");

        Timestamp tsCreacion = rs.getTimestamp("fecha_creacion");
        LocalDateTime fechaCreacion = (tsCreacion != null) ? tsCreacion.toLocalDateTime() : null;

        Timestamp tsActualizacion = rs.getTimestamp("fecha_actualizacion");
        LocalDateTime fechaActualizacion = (tsActualizacion != null) ? tsActualizacion.toLocalDateTime() : null;

        boolean activo = rs.getBoolean("activo");

        return new CategoriaServicio(id, nombre, fechaCreacion, fechaActualizacion, activo);
    }
}