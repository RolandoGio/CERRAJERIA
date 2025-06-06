package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.models.Usuario;
import com.cerrajeria.app.services.UsuarioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsersManagementController {

    @FXML private TextField userIdField;
    @FXML private TextField userNameField;
    @FXML private TextField usernameLoginField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> userRoleCombo;
    @FXML private TextField userCodeField;
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

    private UsuarioService usuarioService;
    private ObservableList<Usuario> userList;
    private Usuario currentUser;

    public UsersManagementController() {
        this.usuarioService = new UsuarioService();
        this.userList = FXCollections.observableArrayList();
    }

    public void setCurrentUser(Usuario currentUser) {
        this.currentUser = currentUser;
    }

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("activo"));

        usersTable.setItems(userList);
        loadUsers();
        loadRoles();

        usersTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showUserDetails(newValue));

        formMessageLabel.setVisible(false);
        setFormControlsEditable(false);
        setEditDeleteButtonsDisable(true);
        clearButton.setDisable(false);
    }

    private void loadUsers() {
        userList.clear();
        userList.addAll(usuarioService.obtenerTodosLosUsuarios());
        usersTable.refresh();
    }

    private void loadRoles() {
        userRoleCombo.setItems(FXCollections.observableArrayList("Administrador", "Vendedor"));
        userRoleCombo.getSelectionModel().selectFirst();
    }

    private void setFormControlsEditable(boolean editable) {
        userNameField.setEditable(editable);
        usernameLoginField.setEditable(editable);
        passwordField.setEditable(editable);
        userRoleCombo.setDisable(!editable);
        userCodeField.setEditable(false);
    }

    private void setEditDeleteButtonsDisable(boolean disable) {
        saveButton.setDisable(disable);
        deactivateButton.setDisable(disable);
        activateButton.setDisable(disable);
    }

    private void showUserDetails(Usuario usuario) {
        if (usuario != null) {
            userIdField.setText(String.valueOf(usuario.getIdUsuario()));
            userNameField.setText(usuario.getNombre());
            usernameLoginField.setText(usuario.getNombreUsuario());
            passwordField.clear();
            userRoleCombo.getSelectionModel().select(usuario.getRol());
            userCodeField.setText(usuario.getCodigo());
            formMessageLabel.setVisible(false);

            // Permitir edición completa para el admin en cualquier usuario
            setFormControlsEditable(true);
            userCodeField.setEditable(false);
            saveButton.setDisable(false);
            deactivateButton.setDisable(false);
            activateButton.setDisable(false);
        } else {
            handleClearForm(null);
        }
    }

    private String generateUserCode(String rol) {
        String prefix = "";
        if ("Administrador".equalsIgnoreCase(rol)) {
            prefix = "AD";
        } else if ("Vendedor".equalsIgnoreCase(rol)) {
            prefix = "VD";
        } else {
            return null;
        }

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

    @FXML
    private void handleSaveUser(ActionEvent event) {
        formMessageLabel.setVisible(false);

        String userIdText = userIdField.getText();
        boolean isNewUser = (userIdText == null || userIdText.isEmpty() || userIdText.equals("Automático"));

        String nombreCompleto = userNameField.getText();
        String nombreUsuarioLogin = usernameLoginField.getText();
        String contrasena = passwordField.getText();
        String rol = userRoleCombo.getSelectionModel().getSelectedItem();
        String codigo = userCodeField.getText();

        if (nombreCompleto.isEmpty() || nombreUsuarioLogin.isEmpty() || rol == null) {
            formMessageLabel.setText("Nombre Completo, Nombre de Usuario y Rol son obligatorios.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            formMessageLabel.setVisible(true);
            return;
        }

        if (!isNewUser) {
            try {
                int idUsuario = Integer.parseInt(userIdText);
                Usuario usuarioExistente = usuarioService.obtenerUsuarioPorId(idUsuario);

                if (usuarioExistente != null) {
                    usuarioExistente.setNombre(nombreCompleto);
                    usuarioExistente.setNombreUsuario(nombreUsuarioLogin);
                    usuarioExistente.setRol(rol);
                    usuarioExistente.setCodigo(codigo);

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
                    formMessageLabel.setText("Error: Usuario no encontrado.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                }
            } catch (NumberFormatException e) {
                formMessageLabel.setText("ID de usuario inválido.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
        } else {
            formMessageLabel.setText("Use el botón 'Crear Nuevo Usuario' para crear un nuevo registro.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.BLUE);
        }
        formMessageLabel.setVisible(true);
    }

    @FXML
    private void handleClearForm(ActionEvent event) {
        userIdField.clear();
        userNameField.clear();
        usernameLoginField.clear();
        passwordField.clear();
        userRoleCombo.getSelectionModel().selectFirst();
        userCodeField.clear();
        formMessageLabel.setVisible(false);
        usersTable.getSelectionModel().clearSelection();
        setFormControlsEditable(true);
        userCodeField.setEditable(false);
        setEditDeleteButtonsDisable(true);
        saveButton.setDisable(true);
    }

    @FXML
    private void handleDeactivateUser(ActionEvent event) {
        Usuario selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Desactivación");
            alert.setHeaderText("Desactivar Usuario: " + selectedUser.getNombre());
            alert.setContentText("¿Está seguro de que desea desactivar este usuario?");

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

    @FXML
    private void handleActivateUser(ActionEvent event) {
        Usuario selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Activación");
            alert.setHeaderText("Activar Usuario: " + selectedUser.getNombre());
            alert.setContentText("¿Está seguro de que desea activar este usuario?");

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

    @FXML
    private void refreshTable(ActionEvent event) {
        searchField.clear();
        loadUsers();
        formMessageLabel.setVisible(false);
        handleClearForm(null);
    }

    @FXML
    private void handleCreateNewUser(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/UserCreationModalView.fxml"));
            Parent parent = fxmlLoader.load();

            UserCreationModalController creationController = fxmlLoader.getController();
            creationController.setRefreshCallback(this::loadUsers);

            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setTitle("Crear Nuevo Usuario Vendedor");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (creationController.isUserCreated()) {
                formMessageLabel.setText("Usuario 'Vendedor' creado exitosamente.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                formMessageLabel.setVisible(true);
            } else {
                formMessageLabel.setText("Creación de usuario cancelada o fallida.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
                formMessageLabel.setVisible(true);
            }
            handleClearForm(null);

        } catch (IOException e) {
            System.err.println("Error al cargar la ventana de creación de usuario: " + e.getMessage());
            formMessageLabel.setText("Error al abrir la ventana de creación de usuario.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            formMessageLabel.setVisible(true);
        }
    }
}
