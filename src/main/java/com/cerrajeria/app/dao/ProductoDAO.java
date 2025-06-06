package com.cerrajeria.app.dao;

import com.cerrajeria.app.database.DatabaseManager;
import com.cerrajeria.app.models.Producto;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO (Data Access Object) para interactuar con la tabla 'producto' en la base de datos.
 */
public class ProductoDAO {

    /**
     * Inserta un nuevo producto en la base de datos.
     * El estado del producto se asignará automáticamente por el trigger de la DB si es necesario,
     * o se manejará lógicamente después de la inserción inicial.
     * @param producto El objeto Producto a insertar.
     * @return El ID del producto recién insertado, o -1 si hubo un error.
     */
    public int insertarProducto(Producto producto) {
        // La columna 'estado' se puede omitir si el trigger de la DB la calcula al insertar,
        // pero la incluimos para control explícito si la DB no lo hace de inmediato.
        String sql = "INSERT INTO producto (nombre, id_categoria_producto, precio, stock, stock_minimo, estado, costo_interno, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int idGenerado = -1;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, producto.getNombre());
            pstmt.setInt(2, producto.getIdCategoriaProducto());
            pstmt.setBigDecimal(3, producto.getPrecio());
            pstmt.setInt(4, producto.getStock());
            pstmt.setInt(5, producto.getStockMinimo());
            pstmt.setString(6, producto.getEstado() != null ? producto.getEstado() : "Disponible"); // Default si no se setea
            pstmt.setBigDecimal(7, producto.getCostoInterno());
            pstmt.setBoolean(8, producto.isActivo());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        producto.setIdProducto(idGenerado);
                        System.out.println("Producto insertado con ID: " + idGenerado);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar producto: " + e.getMessage());
        }
        return idGenerado;
    }

    /**
     * Obtiene un producto por su ID.
     * @param idProducto El ID del producto a buscar.
     * @return El objeto Producto si se encuentra, o null.
     */
    public Producto obtenerProductoPorId(int idProducto) {
        String sql = "SELECT id_producto, nombre, id_categoria_producto, precio, stock, stock_minimo, estado, " +
                "fecha_creacion, fecha_actualizacion, costo_interno, activo FROM producto WHERE id_producto = ?";
        Producto producto = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idProducto);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    producto = mapearResultSetAProducto(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener producto por ID: " + e.getMessage());
        }
        return producto;
    }

    /**
     * Obtiene una lista de productos por nombre (útil para búsquedas).
     * @param nombre El nombre del producto o parte de él.
     * @return Una lista de objetos Producto.
     */
    public List<Producto> obtenerProductosPorNombre(String nombre) {
        String sql = "SELECT id_producto, nombre, id_categoria_producto, precio, stock, stock_minimo, estado, " +
                "fecha_creacion, fecha_actualizacion, costo_interno, activo FROM producto WHERE nombre LIKE ? ORDER BY nombre";
        List<Producto> productos = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + nombre + "%"); // Búsqueda parcial

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapearResultSetAProducto(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos por nombre: " + e.getMessage());
        }
        return productos;
    }


    /**
     * Actualiza los datos de un producto existente.
     * @param producto El objeto Producto con los datos actualizados (el ID debe estar establecido).
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarProducto(Producto producto) {
        String sql = "UPDATE producto SET nombre = ?, id_categoria_producto = ?, precio = ?, stock = ?, " +
                "stock_minimo = ?, estado = ?, fecha_actualizacion = GETDATE(), costo_interno = ?, activo = ? " +
                "WHERE id_producto = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, producto.getNombre());
            pstmt.setInt(2, producto.getIdCategoriaProducto());
            pstmt.setBigDecimal(3, producto.getPrecio());
            pstmt.setInt(4, producto.getStock());
            pstmt.setInt(5, producto.getStockMinimo());
            pstmt.setString(6, producto.getEstado());
            pstmt.setBigDecimal(7, producto.getCostoInterno());
            pstmt.setBoolean(8, producto.isActivo());
            pstmt.setInt(9, producto.getIdProducto());

            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Filas afectadas al actualizar producto: " + filasAfectadas);
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Desactiva lógicamente un producto (establece 'activo' en 0/false).
     * @param idProducto El ID del producto a desactivar.
     * @return true si la desactivación fue exitosa, false en caso contrario.
     */
    public boolean desactivarProducto(int idProducto) {
        String sql = "UPDATE producto SET activo = 0, fecha_actualizacion = GETDATE() WHERE id_producto = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idProducto);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Producto con ID " + idProducto + " desactivado.");
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al desactivar producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Activa lógicamente un producto (establece 'activo' en 1/true).
     * @param idProducto El ID del producto a activar.
     * @return true si la activación fue exitosa, false en caso contrario.
     */
    public boolean activarProducto(int idProducto) {
        String sql = "UPDATE producto SET activo = 1, fecha_actualizacion = GETDATE() WHERE id_producto = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idProducto);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Producto con ID " + idProducto + " activado.");
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al activar producto: " + e.getMessage());
            return false;
        }
    }


    /**
     * Obtiene una lista de todos los productos (activos e inactivos).
     * @return Una lista de objetos Producto.
     */
    public List<Producto> obtenerTodosLosProductos() {
        String sql = "SELECT id_producto, nombre, id_categoria_producto, precio, stock, stock_minimo, estado, " +
                "fecha_creacion, fecha_actualizacion, costo_interno, activo FROM producto";
        List<Producto> productos = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                productos.add(mapearResultSetAProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los productos: " + e.getMessage());
        }
        return productos;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto Producto.
     * @param rs El ResultSet actual.
     * @return Un objeto Producto con los datos del ResultSet.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    private Producto mapearResultSetAProducto(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_producto");
        String nombre = rs.getString("nombre");
        int idCategoria = rs.getInt("id_categoria_producto");
        BigDecimal precio = rs.getBigDecimal("precio");
        int stock = rs.getInt("stock");
        int stockMinimo = rs.getInt("stock_minimo");
        String estado = rs.getString("estado");

        Timestamp tsCreacion = rs.getTimestamp("fecha_creacion");
        LocalDateTime fechaCreacion = (tsCreacion != null) ? tsCreacion.toLocalDateTime() : null;

        Timestamp tsActualizacion = rs.getTimestamp("fecha_actualizacion");
        LocalDateTime fechaActualizacion = (tsActualizacion != null) ? tsActualizacion.toLocalDateTime() : null;

        BigDecimal costoInterno = rs.getBigDecimal("costo_interno");
        boolean activo = rs.getBoolean("activo");

        return new Producto(id, nombre, idCategoria, precio, stock, stockMinimo, estado,
                fechaCreacion, fechaActualizacion, costoInterno, activo);
    }
}