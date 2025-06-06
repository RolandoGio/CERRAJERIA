package com.cerrajeria.app;

import com.cerrajeria.app.models.Usuario;

/**
 * Clase singleton para manejar la sesión del usuario actual.
 */
public class SessionManager {
    private static Usuario usuarioActual;

    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void clear() {
        usuarioActual = null;
    }
}
