package com.cerrajeria.app.dao;

import com.cerrajeria.app.database.DatabaseManager;
import com.cerrajeria.app.models.ComisionCategoriaProducto;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO (Data Access Object) para interactuar con la tabla 'comision_categoria_producto' en la base de datos.
 */
public class ComisionCategoriaProductoDAO {

    /**
     * Inserta una nueva configuración de comisión por categoría de producto.
     * @param comisionConfig El objeto ComisionCategoriaProducto a insertar.
     * @return El ID de la configuración recién insertada, o -1 si hubo un error.
     */
    public int insertarComisionCategoriaProducto(ComisionCategoriaProducto comisionConfig) {
        String sql = "INSERT INTO comision_categoria_producto (id_categoria_producto, porcentaje_comision) VALUES (?, ?)";
        int idGenerado = -1;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, comisionConfig.getIdCategoriaProducto());
            pstmt.setInt(2, comisionConfig.getPorcentajeComision());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        comisionConfig.setIdComisionCategoriaProducto(idGenerado);
                        System.out.println("Configuración de comisión por categoría insertada con ID: " + idGenerado);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar configuración de comisión por categoría: " + e.getMessage());
        }
        return idGenerado;
    }

    /**
     * Obtiene una configuración de comisión por categoría de producto por su ID.
     * @param idComisionCategoriaProducto El ID de la configuración a buscar.
     * @return El objeto ComisionCategoriaProducto si se encuentra, o null.
     */
    public ComisionCategoriaProducto obtenerComisionCategoriaProductoPorId(int idComisionCategoriaProducto) {
        String sql = "SELECT id_com_cat_pro, id_categoria_producto, porcentaje_comision, fecha_creacion, fecha_actualizacion FROM comision_categoria_producto WHERE id_com_cat_pro = ?";
        ComisionCategoriaProducto comisionConfig = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idComisionCategoriaProducto);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    comisionConfig = mapearResultSetAComisionCategoriaProducto(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener configuración de comisión por ID: " + e.getMessage());
        }
        return comisionConfig;
    }

    /**
     * Obtiene una configuración de comisión por el ID de la categoría de producto.
     * @param idCategoriaProducto El ID de la categoría de producto.
     * @return El objeto ComisionCategoriaProducto si se encuentra, o null.
     */
    public ComisionCategoriaProducto obtenerComisionPorCategoriaProductoId(int idCategoriaProducto) {
        String sql = "SELECT id_com_cat_pro, id_categoria_producto, porcentaje_comision, fecha_creacion, fecha_actualizacion FROM comision_categoria_producto WHERE id_categoria_producto = ?";
        ComisionCategoriaProducto comisionConfig = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCategoriaProducto);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    comisionConfig = mapearResultSetAComisionCategoriaProducto(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener configuración de comisión por categoría de producto: " + e.getMessage());
        }
        return comisionConfig;
    }

    /**
     * Actualiza los datos de una configuración de comisión existente.
     * @param comisionConfig La configuración con los datos actualizados.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarComisionCategoriaProducto(ComisionCategoriaProducto comisionConfig) {
        String sql = "UPDATE comision_categoria_producto SET id_categoria_producto = ?, porcentaje_comision = ?, fecha_actualizacion = GETDATE() WHERE id_com_cat_pro = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, comisionConfig.getIdCategoriaProducto());
            pstmt.setInt(2, comisionConfig.getPorcentajeComision());
            pstmt.setInt(3, comisionConfig.getIdComisionCategoriaProducto());

            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Filas afectadas al actualizar configuración de comisión: " + filasAfectadas);
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar configuración de comisión por categoría: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina una configuración de comisión por categoría de producto.
     * @param idComisionCategoriaProducto El ID de la configuración a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public boolean eliminarComisionCategoriaProducto(int idComisionCategoriaProducto) {
        String sql = "DELETE FROM comision_categoria_producto WHERE id_com_cat_pro = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idComisionCategoriaProducto);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Configuración de comisión con ID " + idComisionCategoriaProducto + " eliminada.");
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar configuración de comisión por categoría: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene una lista de todas las configuraciones de comisión por categoría de producto.
     * @return Una lista de objetos ComisionCategoriaProducto.
     */
    public List<ComisionCategoriaProducto> obtenerTodasLasComisionesCategoriaProducto() {
        String sql = "SELECT id_com_cat_pro, id_categoria_producto, porcentaje_comision, fecha_creacion, fecha_actualizacion FROM comision_categoria_producto ORDER BY id_categoria_producto";
        List<ComisionCategoriaProducto> comisionesConfig = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                comisionesConfig.add(mapearResultSetAComisionCategoriaProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todas las configuraciones de comisión por categoría: " + e.getMessage());
        }
        return comisionesConfig;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto ComisionCategoriaProducto.
     * @param rs El ResultSet actual.
     * @return Un objeto ComisionCategoriaProducto con los datos del ResultSet.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    private ComisionCategoriaProducto mapearResultSetAComisionCategoriaProducto(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_com_cat_pro");
        int idCategoriaProducto = rs.getInt("id_categoria_producto");
        int porcentajeComision = rs.getInt("porcentaje_comision");

        Timestamp tsCreacion = rs.getTimestamp("fecha_creacion");
        LocalDateTime fechaCreacion = (tsCreacion != null) ? tsCreacion.toLocalDateTime() : null;

        Timestamp tsActualizacion = rs.getTimestamp("fecha_actualizacion");
        LocalDateTime fechaActualizacion = (tsActualizacion != null) ? tsActualizacion.toLocalDateTime() : null;

        return new ComisionCategoriaProducto(id, idCategoriaProducto, porcentajeComision,
                fechaCreacion, fechaActualizacion);
    }
}