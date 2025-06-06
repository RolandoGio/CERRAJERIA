package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.models.Usuario;
import com.cerrajeria.app.services.UsuarioService;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controlador para la ventana modal de creación de nuevos usuarios.
 * Restringe la creación a solo el rol "Vendedor" para el Administrador.
 */
public class UserCreationModalController {

    @FXML private TextField userNameField;
    @FXML private TextField usernameLoginField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> userRoleCombo;
    @FXML private TextField userCodeField; // Campo de código, será auto-generado
    @FXML private Label formMessageLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private UsuarioService usuarioService;
    private boolean userCreated = false; // Bandera para indicar si un usuario fue creado
    private Runnable refreshCallback; // Callback para la vista principal

    public UserCreationModalController() {
        this.usuarioService = new UsuarioService();
    }

    /**
     * Inicializa el controlador. Se llama después de que los elementos FXML son inyectados.
     * Configura el ComboBox de roles para que solo muestre "Vendedor" y lo deshabilita.
     */
    @FXML
    private void initialize() {
        formMessageLabel.setVisible(false);
        // Restringir el ComboBox de rol a solo "Vendedor" y deshabilitarlo
        userRoleCombo.setItems(FXCollections.observableArrayList("Vendedor"));
        userRoleCombo.getSelectionModel().select("Vendedor");
        userRoleCombo.setDisable(true); // El administrador solo puede crear vendedores
        userCodeField.setEditable(false); // El código siempre es de solo lectura y se autogenera
    }

    /**
     * Permite a la vista principal (ProductsManagementController) establecer un callback
     * para que sea notificada cuando se cree un nuevo usuario.
     * @param callback Una función Runnable a ejecutar.
     */
    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    /**
     * @return true si se creó un usuario, false en caso contrario.
     */
    public boolean isUserCreated() {
        return userCreated;
    }

    /**
     * Genera un código de usuario para el rol "Vendedor" (VDXXXX).
     * @return El nuevo código de usuario.
     */
    private String generateUserCode() {
        String prefix = "VD";
        // Buscar el último código VD existente
        List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
        int maxNum = 0;
        Pattern pattern = Pattern.compile(prefix + "(\\d{4})");

        for (Usuario u : usuarios) {
            if (u.getCodigo() != null && u.getCodigo().startsWith(prefix)) {
                Matcher matcher = pattern.matcher(u.getCodigo());
                if (matcher.matches()) {
                    try {
                        int num = Integer.parseInt(matcher.group(1));
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException e) {
                        // Ignorar códigos mal formados
                    }
                }
            }
        }
        return String.format("%s%04d", prefix, maxNum + 1);
    }

    /**
     * Maneja la acción del botón "Guardar Usuario".
     * Intenta crear un nuevo usuario con el rol "Vendedor".
     * @param event El evento de acción.
     */
    @FXML
    private void handleSaveUser(ActionEvent event) {
        formMessageLabel.setVisible(false);

        String nombreCompleto = userNameField.getText();
        String nombreUsuarioLogin = usernameLoginField.getText();
        String contrasena = passwordField.getText();
        String rol = userRoleCombo.getSelectionModel().getSelectedItem(); // Siempre será "Vendedor"

        if (nombreCompleto.isEmpty() || nombreUsuarioLogin.isEmpty() || contrasena.isEmpty()) {
            formMessageLabel.setText("Nombre Completo, Nombre de Usuario y Contraseña son obligatorios.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            formMessageLabel.setVisible(true);
            return;
        }

        // Generar el código automáticamente para el nuevo vendedor
        String codigoGenerado = generateUserCode();
        userCodeField.setText(codigoGenerado); // Mostrar el código generado en el campo

        // Crear el objeto Usuario con el constructor de 4 parámetros
        Usuario nuevoUsuario = new Usuario(nombreCompleto, nombreUsuarioLogin, contrasena, rol);
        nuevoUsuario.setCodigo(codigoGenerado); // Asignar el código generado

        boolean success = usuarioService.registrarUsuario(nuevoUsuario);
        if (success) {
            formMessageLabel.setText("Usuario 'Vendedor' creado exitosamente.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            formMessageLabel.setVisible(true);
            userCreated = true; // Establecer la bandera
            // Opcional: limpiar campos después de un éxito si el usuario quisiera crear otro
            // handleClearForm();

            // Notificar a la vista principal para que recargue sus datos
            if (refreshCallback != null) {
                refreshCallback.run();
            }

            // Cerrar la ventana después de un pequeño retraso para que el usuario vea el mensaje
            new Thread(() -> {
                try {
                    Thread.sleep(1500); // Esperar 1.5 segundos
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                javafx.application.Platform.runLater(() -> closeWindow());
            }).start();

        } else {
            formMessageLabel.setText("Error al crear usuario. El nombre de usuario o código podrían ya existir.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            formMessageLabel.setVisible(true);
        }
    }

    /**
     * Maneja la acción del botón "Cancelar" o un intento de cerrar la ventana.
     * Cierra la ventana modal.
     * @param event El evento de acción.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    /**
     * Cierra la ventana modal.
     */
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /* // Este método ya no es necesario ya que el rol y código son fijos/autogenerados
    private void handleClearForm() {
        userNameField.clear();
        usernameLoginField.clear();
        passwordField.clear();
        userCodeField.clear();
        formMessageLabel.setVisible(false);
        formMessageLabel.setText("");
    }
    */
}