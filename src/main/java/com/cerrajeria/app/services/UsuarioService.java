package com.cerrajeria.app.services;

import com.cerrajeria.app.dao.UsuarioDAO;
import com.cerrajeria.app.models.Usuario;

import java.util.List;

/**
 * Clase de servicio para la gestión de usuarios.
 * Contiene la lógica de negocio y utiliza UsuarioDAO para la interacción con la base de datos.
 */
public class UsuarioService {

    private UsuarioDAO usuarioDAO;

    // Constructor que inyecta el DAO (para facilitar pruebas y flexibilidad)
    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Autentica un usuario por su nombre de usuario.
     * En una aplicación real, la contraseña se hashearía y se compararía con un hash almacenado.
     * Por ahora, se realiza una comparación directa.
     * @param nombreUsuario El nombre de usuario.
     * @param contrasena La contraseña (sin hashear).
     * @return El objeto Usuario si la autenticación es exitosa, o null si falla.
     */
    public Usuario autenticarUsuario(String nombreUsuario, String contrasena) {
        Usuario usuario = usuarioDAO.obtenerUsuarioPorNombreUsuario(nombreUsuario);
        if (usuario != null && usuario.isActivo()) {
            // TODO: En una aplicación real, usar BCrypt.checkpw(contrasena, usuario.getContrasena())
            if (usuario.getContrasena().equals(contrasena)) { // Comparación directa para la prueba
                System.out.println("Autenticación exitosa para el usuario: " + nombreUsuario);
                return usuario;
            } else {
                System.out.println("Autenticación fallida: Contraseña incorrecta para el usuario: " + nombreUsuario);
            }
        } else if (usuario != null && !usuario.isActivo()){
            System.out.println("Autenticación fallida: El usuario " + nombreUsuario + " está inactivo.");
        } else {
            System.out.println("Autenticación fallida: Usuario '" + nombreUsuario + "' no encontrado.");
        }
        return null;
    }

    /**
     * Autentica un usuario utilizando su código único y contraseña.
     * @param codigo El código del usuario (ej. AD0001, VD0001).
     * @param contrasena La contraseña (sin hashear).
     * @return El objeto Usuario si la autenticación es exitosa y el usuario está activo, o null si falla.
     */
    public Usuario autenticarUsuarioPorCodigo(String codigo, String contrasena) {
        Usuario usuario = usuarioDAO.obtenerUsuarioPorCodigo(codigo); // Nuevo método en DAO
        if (usuario != null && usuario.isActivo()) {
            // TODO: En una aplicación real, usar BCrypt.checkpw(contrasena, usuario.getContrasena())
            if (usuario.getContrasena().equals(contrasena)) { // Comparación directa para la prueba
                System.out.println("Autenticación exitosa para el código: " + codigo);
                return usuario;
            } else {
                System.out.println("Autenticación fallida: Contraseña incorrecta para el código: " + codigo);
            }
        } else if (usuario != null && !usuario.isActivo()){
            System.out.println("Autenticación fallida: El usuario con código " + codigo + " está inactivo.");
        } else {
            System.out.println("Autenticación fallida: Usuario con código '" + codigo + "' no encontrado.");
        }
        return null;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * Realiza validaciones básicas.
     * @param usuario El objeto Usuario a registrar.
     * @return true si el registro es exitoso, false en caso contrario.
     */
    public boolean registrarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getNombreUsuario() == null || usuario.getNombreUsuario().trim().isEmpty() ||
                usuario.getContrasena() == null || usuario.getContrasena().trim().isEmpty() ||
                usuario.getRol() == null || usuario.getRol().trim().isEmpty()) {
            System.err.println("Error de registro: Datos de usuario incompletos.");
            return false;
        }

        // Verificar si el nombre de usuario ya existe
        if (usuarioDAO.obtenerUsuarioPorNombreUsuario(usuario.getNombreUsuario()) != null) {
            System.err.println("Error de registro: El nombre de usuario '" + usuario.getNombreUsuario() + "' ya está en uso.");
            return false;
        }

        // TODO: En una aplicación real, hashear la contraseña aquí antes de guardarla
        // String contrasenaHasheada = BCrypt.hashpw(usuario.getContrasena(), BCrypt.gensalt());
        // usuario.setContrasena(contrasenaHasheada);

        int id = usuarioDAO.insertarUsuario(usuario);
        return id != -1;
    }

    /**
     * Obtiene un usuario por su ID.
     * @param idUsuario ID del usuario.
     * @return El objeto Usuario o null.
     */
    public Usuario obtenerUsuarioPorId(int idUsuario) {
        return usuarioDAO.obtenerUsuarioPorId(idUsuario);
    }

    /**
     * Actualiza los datos de un usuario existente.
     * @param usuario El usuario con los datos actualizados.
     * @return true si la actualización es exitosa, false en caso contrario.
     */
    public boolean actualizarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getIdUsuario() <= 0) {
            System.err.println("Error de actualización: ID de usuario inválido.");
            return false;
        }
        // Podrías añadir más lógica de validación aquí antes de llamar al DAO
        return usuarioDAO.actualizarUsuario(usuario);
    }

    /**
     * Desactiva lógicamente un usuario.
     * @param idUsuario ID del usuario a desactivar.
     * @return true si la desactivación es exitosa, false en caso contrario.
     */
    public boolean desactivarUsuario(int idUsuario) {
        return usuarioDAO.desactivarUsuario(idUsuario);
    }

    /**
     * Activa lógicamente un usuario.
     * @param idUsuario ID del usuario a activar.
     * @return true si la activación es exitosa, false en caso contrario.
     */
    public boolean activarUsuario(int idUsuario) {
        return usuarioDAO.activarUsuario(idUsuario);
    }

    /**
     * Obtiene una lista de todos los usuarios.
     * @return Lista de objetos Usuario.
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioDAO.obtenerTodosLosUsuarios();
    }
}