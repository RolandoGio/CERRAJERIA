package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.MainApplication;
import com.cerrajeria.app.SessionManager;
import com.cerrajeria.app.models.Usuario;
import com.cerrajeria.app.services.UsuarioService;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import java.io.IOException;

/**
 * Controlador para la pantalla de Login de la aplicación.
 * Maneja la lógica de autenticación del usuario, valida credenciales
 * y redirige al dashboard apropiado según el rol.
 */
public class LoginController {

    @FXML
    private TextField codigoField;

    @FXML
    private PasswordField contrasenaField;

    @FXML
    private Label mensajeError;

    private final UsuarioService usuarioService;

    /**
     * Constructor del controlador.
     */
    public LoginController() {
        this.usuarioService = new UsuarioService();
    }

    /**
     * Método que se llama automáticamente después de que los campos FXML
     * han sido inyectados.
     */
    @FXML
    private void initialize() {
        mensajeError.setVisible(false);
    }

    /**
     * Maneja la acción del botón "Iniciar Sesión".
     * @param event El evento de acción.
     */
    @FXML
    private void handleLoginButton(ActionEvent event) {
        String codigo = codigoField.getText();
        String contrasena = contrasenaField.getText();

        mensajeError.setVisible(false);
        mensajeError.setText("");

        if (codigo.isEmpty() || contrasena.isEmpty()) {
            mensajeError.setText("Por favor, ingrese código y contraseña.");
            mensajeError.setVisible(true);
            return;
        }

        Usuario usuarioAutenticado = usuarioService.autenticarUsuarioPorCodigo(codigo, contrasena);

        if (usuarioAutenticado != null) {
            System.out.println("Login exitoso! Usuario: " + usuarioAutenticado.getNombreUsuario() + ", Rol: " + usuarioAutenticado.getRol());
            // Guardar el usuario en la sesión
            SessionManager.setUsuarioActual(usuarioAutenticado);

            try {
                if ("Administrador".equalsIgnoreCase(usuarioAutenticado.getRol())) {
                    MainApplication.showAdminDashboard();
                } else if ("Vendedor".equalsIgnoreCase(usuarioAutenticado.getRol())) {
                    MainApplication.showVendedorDashboard();
                } else {
                    mensajeError.setText("Rol de usuario no reconocido. Contacte al administrador.");
                    mensajeError.setVisible(true);
                }
            } catch (IOException e) {
                System.err.println("Error al cargar la siguiente pantalla: " + e.getMessage());
                mensajeError.setText("Error interno de la aplicación. Contacte soporte.");
                mensajeError.setVisible(true);
                e.printStackTrace();
            }
        } else {
            mensajeError.setText("Código o contraseña incorrectos.");
            mensajeError.setVisible(true);
        }
    }
}
