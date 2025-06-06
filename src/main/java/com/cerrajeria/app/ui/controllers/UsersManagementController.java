package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.models.Usuario;
import com.cerrajeria.app.services.UsuarioService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Nueva importación
import javafx.scene.Parent;     // Nueva importación
import javafx.scene.Scene;      // Nueva importación
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;   // Nueva importación
import javafx.stage.Stage;      // Nueva importación

import java.io.IOException; // Nueva importación para manejar IOException
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controlador para la vista de Gestión de Usuarios.
 * Permite al Administrador crear, editar, visualizar, activar y desactivar usuarios.
 */
public class UsersManagementController {

    // --- Componentes de la UI (inyectados desde FXML) ---
    @FXML private TextField userIdField;
    @FXML private TextField userNameField; // Nombre completo
    @FXML private TextField usernameLoginField; // Nombre de usuario para login
    @FXML private PasswordField passwordField; // Contraseña
    @FXML private ComboBox<String> userRoleCombo; // Combo para el rol
    @FXML private TextField userCodeField; // Código de usuario (AD0001, VD0001)
    @FXML private Label formMessageLabel;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button deactivateButton;
    @FXML private Button activateButton;
    @FXML private TextField searchField;
    @FXML private TableView<Usuario> usersTable;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colName;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colRole;
    @FXML private TableColumn<Usuario, String> colCode;
    @FXML private TableColumn<Usuario, Boolean> colActive;

    // --- Servicio de negocio ---
    private UsuarioService usuarioService;
    private ObservableList<Usuario> userList; // Lista observable para la tabla

    // --- Constructor ---
    public UsersManagementController() {
        this.usuarioService = new UsuarioService();
        this.userList = FXCollections.observableArrayList();
    }

    /**
     * Método de inicialización del controlador. Se llama automáticamente después de cargar el FXML.
     * Configura la tabla, carga los datos iniciales y el ComboBox de roles.
     */
    @FXML
    private void initialize() {
        // Configurar las columnas de la tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("activo"));

        usersTable.setItems(userList); // Vincular la lista observable a la tabla

        // Cargar datos iniciales
        loadUsers();
        loadRoles(); // Cargar los roles en el ComboBox

        // Configurar listener para selección de tabla (para edición)
        usersTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showUserDetails(newValue));

        formMessageLabel.setVisible(false); // Ocultar mensaje al inicio
        setFormControlsEditable(false); // Deshabilitar campos de formulario al inicio (para un nuevo ingreso)
        setEditDeleteButtonsDisable(true); // Deshabilitar botones de edición/desactivación al inicio
        clearButton.setDisable(false); // El botón limpiar siempre debe estar habilitado
    }

    /**
     * Carga todos los usuarios de la base de datos y actualiza la tabla.
     */
    private void loadUsers() {
        userList.clear();
        userList.addAll(usuarioService.obtenerTodosLosUsuarios());
        usersTable.refresh(); // Asegura que la tabla se redibuje
    }

    /**
     * Carga los roles disponibles en el ComboBox.
     */
    private void loadRoles() {
        userRoleCombo.setItems(FXCollections.observableArrayList("Administrador", "Vendedor"));
        userRoleCombo.getSelectionModel().selectFirst(); // Seleccionar el primer rol por defecto
    }

    /**
     * Habilita/deshabilita los campos de entrada del formulario.
     * @param editable true para habilitar, false para deshabilitar.
     */
    private void setFormControlsEditable(boolean editable) {
        userNameField.setEditable(editable);
        usernameLoginField.setEditable(editable);
        passwordField.setEditable(editable);
        userRoleCombo.setDisable(!editable);
        userCodeField.setEditable(false); // Siempre de solo lectura en la UI (excepto para generación auto)
    }

    /**
     * Habilita o deshabilita los botones de guardar, desactivar y activar.
     * @param disable true para deshabilitar, false para habilitar.
     */
    private void setEditDeleteButtonsDisable(boolean disable) {
        saveButton.setDisable(disable);
        deactivateButton.setDisable(disable);
        activateButton.setDisable(disable);
    }

    /**
     * Muestra los detalles del usuario seleccionado en el formulario para edición.
     * Aplica restricciones si el usuario es un Administrador o un Vendedor (para el cambio de rol).
     * @param usuario El usuario seleccionado en la tabla.
     */
    private void showUserDetails(Usuario usuario) {
        if (usuario != null) {
            userIdField.setText(String.valueOf(usuario.getIdUsuario()));
            userNameField.setText(usuario.getNombre());
            usernameLoginField.setText(usuario.getNombreUsuario());
            passwordField.clear(); // Limpiar contraseña por seguridad al cargar
            userRoleCombo.getSelectionModel().select(usuario.getRol());
            userCodeField.setText(usuario.getCodigo());
            formMessageLabel.setVisible(false); // Ocultar mensaje al seleccionar

            // Habilitar todos los campos para edición por defecto
            setFormControlsEditable(true);
            userCodeField.setEditable(false); // El código siempre es de solo lectura una vez que existe

            // Habilitar/Deshabilitar botones para edición/desactivación
            setEditDeleteButtonsDisable(false);

            if ("Administrador".equalsIgnoreCase(usuario.getRol())) {
                // Si el usuario seleccionado es un Administrador:
                // Deshabilitar la edición de sus propios datos (excepto contraseña que puede ser opcional)
                userNameField.setEditable(false);
                usernameLoginField.setEditable(false);
                userRoleCombo.setDisable(true); // No se puede cambiar el rol de un admin
                userCodeField.setEditable(false); // El código siempre es de solo lectura

                saveButton.setDisable(true); // No se puede guardar cambios en un admin
                deactivateButton.setDisable(true); // No se puede desactivar un admin
                activateButton.setDisable(true); // No se puede activar un admin (si ya está inactivo y es admin)
                formMessageLabel.setText("No puedes modificar, activar o desactivar a otro Administrador.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
                formMessageLabel.setVisible(true);
            } else if ("Vendedor".equalsIgnoreCase(usuario.getRol())) {
                // Si el usuario seleccionado es un Vendedor:
                // Restringir el cambio de rol.
                userRoleCombo.setDisable(true); // No se puede cambiar el rol de un Vendedor
                formMessageLabel.setText("No puedes cambiar el rol de un Vendedor a Administrador.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
                formMessageLabel.setVisible(true);
            }


        } else {
            handleClearForm(null); // Limpiar el formulario si no hay selección
            setFormControlsEditable(true); // Habilitar campos para crear un nuevo usuario
            setEditDeleteButtonsDisable(true); // Deshabilita botones de edición/activación/desactivación
            saveButton.setDisable(false); // Habilita el botón de guardar para el nuevo registro
        }
    }

    /**
     * Genera un código de usuario basado en el rol (AD0001, VD0001, etc.).
     * Busca el último código existente para el rol y genera el siguiente.
     * @param rol El rol del usuario ("Administrador" o "Vendedor").
     * @return Un nuevo código de usuario.
     */
    private String generateUserCode(String rol) {
        String prefix = "";
        if ("Administrador".equalsIgnoreCase(rol)) {
            prefix = "AD";
        } else if ("Vendedor".equalsIgnoreCase(rol)) {
            prefix = "VD";
        } else {
            return null; // Rol no reconocido
        }

        // Buscar el último código para ese prefijo
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
     * Maneja la acción del botón "Guardar".
     * Puede crear un nuevo usuario o actualizar uno existente.
     * @param event El evento de acción.
     */
    @FXML
    private void handleSaveUser(ActionEvent event) {
        formMessageLabel.setVisible(false);

        String userIdText = userIdField.getText();
        boolean isNewUser = (userIdText == null || userIdText.isEmpty() || userIdText.equals("Automático"));

        String nombreCompleto = userNameField.getText();
        String nombreUsuarioLogin = usernameLoginField.getText();
        String contrasena = passwordField.getText(); // Solo se usará para nuevas creaciones o si se cambia
        String rol = userRoleCombo.getSelectionModel().getSelectedItem();
        String codigo = userCodeField.getText(); // Se lee, pero se gestiona si es nuevo o no

        // Validaciones básicas
        if (nombreCompleto.isEmpty() || nombreUsuarioLogin.isEmpty() || rol == null) {
            formMessageLabel.setText("Nombre Completo, Nombre de Usuario y Rol son obligatorios.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            formMessageLabel.setVisible(true);
            return;
        }

        // Si es un Administrador seleccionado, no permitir guardar
        if (!isNewUser) { // Solo aplica a usuarios existentes seleccionados
            Usuario selectedUser = usersTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null && "Administrador".equalsIgnoreCase(selectedUser.getRol())) {
                formMessageLabel.setText("No puedes guardar cambios para un usuario Administrador.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                formMessageLabel.setVisible(true);
                return;
            }
        }

        if (isNewUser) {
            // Lógica para crear un nuevo usuario (ahora se usará el modal para esto)
            formMessageLabel.setText("Use el botón 'Crear Nuevo Usuario' para crear un nuevo registro.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.BLUE);
            formMessageLabel.setVisible(true);
            return; // Salir, ya que la creación se hace por el modal
        } else {
            // Lógica para actualizar un usuario existente
            try {
                int idUsuario = Integer.parseInt(userIdText);
                Usuario usuarioExistente = usuarioService.obtenerUsuarioPorId(idUsuario);

                if (usuarioExistente != null) {
                    // **VALIDACIÓN: No permitir cambiar de Vendedor a Administrador**
                    if ("Vendedor".equalsIgnoreCase(usuarioExistente.getRol()) && "Administrador".equalsIgnoreCase(rol)) {
                        formMessageLabel.setText("No se puede cambiar el rol de Vendedor a Administrador.");
                        formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                        formMessageLabel.setVisible(true);
                        return;
                    }

                    usuarioExistente.setNombre(nombreCompleto);
                    usuarioExistente.setNombreUsuario(nombreUsuarioLogin);
                    usuarioExistente.setRol(rol); // Esto actualizará el rol si no es Admin
                    usuarioExistente.setCodigo(codigo); // Se actualizará con el valor del campo

                    if (!contrasena.isEmpty()) {
                        usuarioExistente.setContrasena(contrasena);
                    }

                    boolean success = usuarioService.actualizarUsuario(usuarioExistente);
                    if (success) {
                        formMessageLabel.setText("Usuario actualizado exitosamente.");
                        formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                        handleClearForm(null);
                        loadUsers();
                    } else {
                        formMessageLabel.setText("Error al actualizar usuario. Revise los datos.");
                        formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                    }
                } else {
                    formMessageLabel.setText("Error: Usuario con ID " + idUsuario + " no encontrado para actualizar.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                }
            } catch (NumberFormatException e) {
                formMessageLabel.setText("ID de usuario inválido para actualizar.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
        }
        formMessageLabel.setVisible(true);
    }

    /**
     * Maneja la acción del botón "Limpiar". Limpia el formulario y reinicia los controles.
     * @param event El evento de acción.
     */
    @FXML
    private void handleClearForm(ActionEvent event) {
        userIdField.clear();
        userNameField.clear();
        usernameLoginField.clear();
        passwordField.clear();
        userRoleCombo.getSelectionModel().selectFirst(); // Reseleccionar el primer rol
        userCodeField.clear();
        formMessageLabel.setVisible(false);
        formMessageLabel.setText("");
        usersTable.getSelectionModel().clearSelection();
        setFormControlsEditable(true); // Habilitar campos para crear un nuevo usuario
        userCodeField.setEditable(false); // El código se generará, no se edita manualmente
        setEditDeleteButtonsDisable(true); // Deshabilita botones de edición/activación/desactivación
        saveButton.setDisable(true); // Deshabilita el botón de guardar en el formulario principal
    }

    /**
     * Maneja la acción del botón "Desactivar". Desactiva lógicamente el usuario seleccionado.
     * @param event El evento de acción.
     */
    @FXML
    private void handleDeactivateUser(ActionEvent event) {
        Usuario selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // No permitir desactivar un Administrador
            if ("Administrador".equalsIgnoreCase(selectedUser.getRol())) {
                formMessageLabel.setText("No puedes desactivar un usuario Administrador.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                formMessageLabel.setVisible(true);
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Desactivación");
            alert.setHeaderText("Desactivar Usuario: " + selectedUser.getNombre());
            alert.setContentText("¿Está seguro de que desea desactivar este usuario? No podrá iniciar sesión.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = usuarioService.desactivarUsuario(selectedUser.getIdUsuario());
                if (success) {
                    formMessageLabel.setText("Usuario desactivado exitosamente.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                    loadUsers();
                    handleClearForm(null);
                } else {
                    formMessageLabel.setText("Error al desactivar usuario.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                }
            }
        } else {
            formMessageLabel.setText("Seleccione un usuario para desactivar.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
        }
        formMessageLabel.setVisible(true);
    }

    /**
     * Maneja la acción del botón "Activar". Activa lógicamente el usuario seleccionado.
     * @param event El evento de acción.
     */
    @FXML
    private void handleActivateUser(ActionEvent event) {
        Usuario selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // No permitir activar un Administrador
            if ("Administrador".equalsIgnoreCase(selectedUser.getRol())) {
                formMessageLabel.setText("No puedes activar un usuario Administrador.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                formMessageLabel.setVisible(true);
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Activación");
            alert.setHeaderText("Activar Usuario: " + selectedUser.getNombre());
            alert.setContentText("¿Está seguro de que desea activar este usuario? Podrá iniciar sesión.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = usuarioService.activarUsuario(selectedUser.getIdUsuario());
                if (success) {
                    formMessageLabel.setText("Usuario activado exitosamente.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                    loadUsers();
                    handleClearForm(null);
                } else {
                    formMessageLabel.setText("Error al activar usuario.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                }
            }
        } else {
            formMessageLabel.setText("Seleccione un usuario para activar.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
        }
        formMessageLabel.setVisible(true);
    }

    /**
     * Maneja la acción del botón "Buscar". Filtra los usuarios en la tabla.
     * @param event El evento de acción.
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadUsers();
            return;
        }

        ObservableList<Usuario> filteredList = FXCollections.observableArrayList();
        for (Usuario u : usuarioService.obtenerTodosLosUsuarios()) {
            if (String.valueOf(u.getIdUsuario()).contains(searchText) ||
                    u.getNombre().toLowerCase().contains(searchText) ||
                    u.getNombreUsuario().toLowerCase().contains(searchText) ||
                    (u.getCodigo() != null && u.getCodigo().toLowerCase().contains(searchText))) {
                filteredList.add(u);
            }
        }
        userList.setAll(filteredList);
    }

    /**
     * Maneja la acción del botón "Actualizar Tabla". Recarga todos los usuarios.
     * @param event El evento de acción.
     */
    @FXML
    private void refreshTable(ActionEvent event) {
        searchField.clear();
        loadUsers();
        formMessageLabel.setVisible(false);
        formMessageLabel.setText("");
        handleClearForm(null);
    }

    /**
     * Maneja la acción del botón "Crear Nuevo Usuario".
     * Abre una ventana modal para la creación de un nuevo usuario Vendedor.
     * @param event El evento de acción.
     */
    @FXML
    private void handleCreateNewUser(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/UserCreationModalView.fxml"));
            Parent parent = fxmlLoader.load();

            UserCreationModalController creationController = fxmlLoader.getController();
            // Establecer un callback para que la tabla principal se recargue cuando se cierre el modal
            creationController.setRefreshCallback(this::loadUsers);

            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setTitle("Crear Nuevo Usuario Vendedor");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Hace que la ventana sea modal y bloquee la principal
            stage.showAndWait(); // Espera a que la ventana se cierre

            // Después de que el modal se cierra, la tabla de usuarios se recargará a través del callback
            // si se creó un usuario.
            if(creationController.isUserCreated()) {
                formMessageLabel.setText("Usuario 'Vendedor' creado exitosamente.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                formMessageLabel.setVisible(true);
            } else {
                formMessageLabel.setText("Creación de usuario cancelada o fallida.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
                formMessageLabel.setVisible(true);
            }
            handleClearForm(null); // Limpiar el formulario principal

        } catch (IOException e) {
            System.err.println("Error al cargar la ventana de creación de usuario: " + e.getMessage());
            e.printStackTrace();
            formMessageLabel.setText("Error al abrir la ventana de creación de usuario.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            formMessageLabel.setVisible(true);
        }
    }
}