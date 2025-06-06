package com.cerrajeria.app.dao;

import com.cerrajeria.app.database.DatabaseManager;
import com.cerrajeria.app.models.CategoriaProducto;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO (Data Access Object) para interactuar con la tabla 'categoria_producto' en la base de datos.
 */
public class CategoriaProductoDAO {

    /**
     * Inserta una nueva categoría de producto en la base de datos.
     * @param categoria La categoría a insertar.
     * @return El ID de la categoría recién insertada, o -1 si hubo un error.
     */
    public int insertarCategoriaProducto(CategoriaProducto categoria) {
        String sql = "INSERT INTO categoria_producto (nombre, activo) VALUES (?, ?)";
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
                        categoria.setIdCategoriaProducto(idGenerado);
                        System.out.println("Categoría de producto insertada con ID: " + idGenerado);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar categoría de producto: " + e.getMessage());
        }
        return idGenerado;
    }

    /**
     * Obtiene una categoría de producto por su ID.
     * @param idCategoria El ID de la categoría a buscar.
     * @return El objeto CategoriaProducto si se encuentra, o null.
     */
    public CategoriaProducto obtenerCategoriaProductoPorId(int idCategoria) {
        String sql = "SELECT id_categoria_producto, nombre, fecha_creacion, fecha_actualizacion, activo FROM categoria_producto WHERE id_categoria_producto = ?";
        CategoriaProducto categoria = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCategoria);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    categoria = mapearResultSetACategoriaProducto(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener categoría de producto por ID: " + e.getMessage());
        }
        return categoria;
    }

    /**
     * Obtiene una categoría de producto por su nombre.
     * @param nombre El nombre de la categoría a buscar.
     * @return El objeto CategoriaProducto si se encuentra, o null.
     */
    public CategoriaProducto obtenerCategoriaProductoPorNombre(String nombre) {
        String sql = "SELECT id_categoria_producto, nombre, fecha_creacion, fecha_actualizacion, activo FROM categoria_producto WHERE nombre = ?";
        CategoriaProducto categoria = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    categoria = mapearResultSetACategoriaProducto(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener categoría de producto por nombre: " + e.getMessage());
        }
        return categoria;
    }

    /**
     * Actualiza los datos de una categoría de producto existente.
     * @param categoria La categoría con los datos actualizados.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarCategoriaProducto(CategoriaProducto categoria) {
        String sql = "UPDATE categoria_producto SET nombre = ?, activo = ?, fecha_actualizacion = GETDATE() WHERE id_categoria_producto = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoria.getNombre());
            pstmt.setBoolean(2, categoria.isActivo());
            pstmt.setInt(3, categoria.getIdCategoriaProducto());

            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Filas afectadas al actualizar categoría de producto: " + filasAfectadas);
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar categoría de producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Desactiva lógicamente una categoría de producto (establece 'activo' en 0/false).
     * @param idCategoria El ID de la categoría a desactivar.
     * @return true si la desactivación fue exitosa, false en caso contrario.
     */
    public boolean desactivarCategoriaProducto(int idCategoria) {
        String sql = "UPDATE categoria_producto SET activo = 0, fecha_actualizacion = GETDATE() WHERE id_categoria_producto = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCategoria);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Categoría de producto con ID " + idCategoria + " desactivada.");
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al desactivar categoría de producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Activa lógicamente una categoría de producto (establece 'activo' en 1/true).
     * @param idCategoria El ID de la categoría a activar.
     * @return true si la activación fue exitosa, false en caso contrario.
     */
    public boolean activarCategoriaProducto(int idCategoria) {
        String sql = "UPDATE categoria_producto SET activo = 1, fecha_actualizacion = GETDATE() WHERE id_categoria_producto = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCategoria);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Categoría de producto con ID " + idCategoria + " activada.");
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al activar categoría de producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene una lista de todas las categorías de producto (activas e inactivas).
     * @return Una lista de objetos CategoriaProducto.
     */
    public List<CategoriaProducto> obtenerTodasCategoriasProducto() {
        String sql = "SELECT id_categoria_producto, nombre, fecha_creacion, fecha_actualizacion, activo FROM categoria_producto";
        List<CategoriaProducto> categorias = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categorias.add(mapearResultSetACategoriaProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todas las categorías de producto: " + e.getMessage());
        }
        return categorias;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto CategoriaProducto.
     * @param rs El ResultSet actual.
     * @return Un objeto CategoriaProducto con los datos del ResultSet.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    private CategoriaProducto mapearResultSetACategoriaProducto(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_categoria_producto");
        String nombre = rs.getString("nombre");

        Timestamp tsCreacion = rs.getTimestamp("fecha_creacion");
        LocalDateTime fechaCreacion = (tsCreacion != null) ? tsCreacion.toLocalDateTime() : null;

        Timestamp tsActualizacion = rs.getTimestamp("fecha_actualizacion");
        LocalDateTime fechaActualizacion = (tsActualizacion != null) ? tsActualizacion.toLocalDateTime() : null;

        // 'activo' es BIT en DB, se mapea a boolean en Java
        boolean activo = rs.getBoolean("activo");

        return new CategoriaProducto(id, nombre, fechaCreacion, fechaActualizacion, activo);
    }
}