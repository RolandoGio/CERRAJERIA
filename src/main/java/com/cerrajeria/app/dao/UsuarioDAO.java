package com.cerrajeria.app.dao;

import com.cerrajeria.app.database.DatabaseManager;
import com.cerrajeria.app.models.Usuario;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO (Data Access Object) para interactuar con la tabla 'usuario' en la base de datos.
 * Contiene métodos CRUD (Crear, Leer, Actualizar, Eliminar).
 */
public class UsuarioDAO {

    /**
     * Inserta un nuevo usuario en la base de datos.
     * @param usuario El objeto Usuario a insertar.
     * @return El ID del usuario recién insertado, o -1 si hubo un error.
     */
    public int insertarUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre, usuario, contrasena, rol, activo) VALUES (?, ?, ?, ?, ?)";
        int idGenerado = -1;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getNombreUsuario());
            pstmt.setString(3, usuario.getContrasena());
            pstmt.setString(4, usuario.getRol());
            pstmt.setBoolean(5, usuario.isActivo());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1); // Obtiene el ID generado por IDENTITY
                        usuario.setIdUsuario(idGenerado); // Actualiza el objeto usuario con el ID
                        System.out.println("Usuario insertado con ID: " + idGenerado);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar usuario: " + e.getMessage());
            // Podrías verificar si es un error de UNIQUE por el nombre de usuario
            if (e.getMessage().contains("Cannot insert duplicate key row in object 'dbo.usuario' with unique index 'UQ__usuario__065D0F53B7552972'")) {
                System.err.println("El nombre de usuario '" + usuario.getNombreUsuario() + "' ya existe.");
            }
        }
        return idGenerado;
    }

    /**
     * Obtiene un usuario por su ID.
     * @param idUsuario El ID del usuario a buscar.
     * @return El objeto Usuario si se encuentra, o null si no existe.
     */
    public Usuario obtenerUsuarioPorId(int idUsuario) {
        String sql = "SELECT id_usuario, nombre, usuario, contrasena, rol, activo, fecha_creacion, fecha_actualizacion, codigo FROM usuario WHERE id_usuario = ?";
        Usuario usuario = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    usuario = mapearResultSetAUsuario(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario por ID: " + e.getMessage());
        }
        return usuario;
    }

    /**
     * Obtiene un usuario por su nombre de usuario (útil para el login).
     * @param nombreUsuario El nombre de usuario a buscar.
     * @return El objeto Usuario si se encuentra, o null si no existe.
     */
    public Usuario obtenerUsuarioPorNombreUsuario(String nombreUsuario) {
        String sql = "SELECT id_usuario, nombre, usuario, contrasena, rol, activo, fecha_creacion, fecha_actualizacion, codigo FROM usuario WHERE usuario = ?";
        Usuario usuario = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombreUsuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    usuario = mapearResultSetAUsuario(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario por nombre de usuario: " + e.getMessage());
        }
        return usuario;
    }

    /**
     * Obtiene un usuario por su código único.
     * @param codigo El código del usuario (ej. AD0001, VD0001) a buscar.
     * @return El objeto Usuario si se encuentra, o null si no existe.
     */
    public Usuario obtenerUsuarioPorCodigo(String codigo) {
        String sql = "SELECT id_usuario, nombre, usuario, contrasena, rol, activo, fecha_creacion, fecha_actualizacion, codigo FROM usuario WHERE codigo = ?";
        Usuario usuario = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, codigo);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    usuario = mapearResultSetAUsuario(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario por código: " + e.getMessage());
        }
        return usuario;
    }

    /**
     * Actualiza los datos de un usuario existente.
     * @param usuario El objeto Usuario con los datos actualizados (el ID debe estar establecido).
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarUsuario(Usuario usuario) {
        // No actualizamos fecha_creacion ni codigo directamente desde aquí normalmente
        String sql = "UPDATE usuario SET nombre = ?, usuario = ?, contrasena = ?, rol = ?, activo = ?, fecha_actualizacion = GETDATE() WHERE id_usuario = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getNombreUsuario());
            pstmt.setString(3, usuario.getContrasena());
            pstmt.setString(4, usuario.getRol());
            pstmt.setBoolean(5, usuario.isActivo());
            pstmt.setInt(6, usuario.getIdUsuario());

            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Filas afectadas al actualizar usuario: " + filasAfectadas);
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Desactiva lógicamente un usuario (establece 'activo' en 0/false).
     * @param idUsuario El ID del usuario a desactivar.
     * @return true si la desactivación fue exitosa, false en caso contrario.
     */
    public boolean desactivarUsuario(int idUsuario) {
        String sql = "UPDATE usuario SET activo = 0, fecha_actualizacion = GETDATE() WHERE id_usuario = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Usuario con ID " + idUsuario + " desactivado.");
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al desactivar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Activa lógicamente un usuario (establece 'activo' en 1/true).
     * @param idUsuario El ID del usuario a activar.
     * @return true si la activación fue exitosa, false en caso contrario.
     */
    public boolean activarUsuario(int idUsuario) {
        String sql = "UPDATE usuario SET activo = 1, fecha_actualizacion = GETDATE() WHERE id_usuario = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Usuario con ID " + idUsuario + " activado.");
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al activar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene una lista de todos los usuarios (activos e inactivos).
     * @return Una lista de objetos Usuario.
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        String sql = "SELECT id_usuario, nombre, usuario, contrasena, rol, activo, fecha_creacion, fecha_actualizacion, codigo FROM usuario";
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usuarios.add(mapearResultSetAUsuario(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los usuarios: " + e.getMessage());
        }
        return usuarios;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto Usuario.
     * @param rs El ResultSet actual.
     * @return Un objeto Usuario con los datos del ResultSet.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    private Usuario mapearResultSetAUsuario(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_usuario");
        String nombre = rs.getString("nombre");
        String nombreUsuario = rs.getString("usuario");
        String contrasena = rs.getString("contrasena");
        String rol = rs.getString("rol");
        boolean activo = rs.getBoolean("activo"); // getBoolean para BIT/BOOLEAN

        // Para LocalDateTime, obtenemos un Timestamp y luego lo convertimos
        Timestamp tsCreacion = rs.getTimestamp("fecha_creacion");
        LocalDateTime fechaCreacion = (tsCreacion != null) ? tsCreacion.toLocalDateTime() : null;

        Timestamp tsActualizacion = rs.getTimestamp("fecha_actualizacion");
        LocalDateTime fechaActualizacion = (tsActualizacion != null) ? tsActualizacion.toLocalDateTime() : null;

        String codigo = rs.getString("codigo"); // La columna 'codigo' puede ser nula inicialmente

        return new Usuario(id, nombre, nombreUsuario, contrasena, rol, activo, fechaCreacion, fechaActualizacion, codigo);
    }
}