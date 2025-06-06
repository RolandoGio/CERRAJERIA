package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.models.Usuario;
import com.cerrajeria.app.services.ComisionService;
import com.cerrajeria.app.services.UsuarioService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para el modal de creación de comisiones manuales.
 * Permite al Administrador registrar una nueva comisión manual y asignarla a un vendedor,
 * incluyendo el comentario del administrador.
 */
public class CommissionCreationModalController {

    @FXML private ComboBox<Usuario> userCombo;
    @FXML private TextField montoField;
    @FXML private TextField ventaIdField;
    @FXML private TextArea comentarioArea;
    @FXML private TextArea comentarioAdminArea;
    @FXML private Label formMessageLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final ComisionService comisionService = new ComisionService();
    private final UsuarioService usuarioService = new UsuarioService();
    private final ObservableList<Usuario> vendedoresList = FXCollections.observableArrayList();
    private Runnable refreshCallback;

    @FXML
    private void initialize() {
        formMessageLabel.setVisible(false);
        loadVendedores();
    }

    private void loadVendedores() {
        List<Usuario> vendedores = usuarioService.obtenerTodosLosUsuarios()
                .stream()
                .filter(u -> "Vendedor".equalsIgnoreCase(u.getRol()) && u.isActivo())
                .collect(Collectors.toList());

        vendedoresList.setAll(vendedores);
        userCombo.setItems(vendedoresList);
        userCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Usuario user) {
                return user != null ? user.getNombre() : "";
            }
            @Override
            public Usuario fromString(String s) { return null; }
        });
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    @FXML
    private void handleSave(ActionEvent event) {
        formMessageLabel.setVisible(false);

        Usuario user = userCombo.getSelectionModel().getSelectedItem();
        String montoStr = montoField.getText();
        String ventaStr = ventaIdField.getText();
        String comentario = comentarioArea.getText();
        String comentarioAdmin = comentarioAdminArea.getText();

        if (user == null || montoStr.isBlank() || comentario.isBlank()) {
            showMessage("Usuario, Monto y Comentario son obligatorios.", true);
            return;
        }

        try {
            BigDecimal monto = new BigDecimal(montoStr);
            Integer idVenta = ventaStr.isBlank() ? null : Integer.parseInt(ventaStr);

            // Al registrar una comisión manual, el comentario del AD no se mezcla en el campo de comentario principal.
            // Se almacena únicamente el comentario general.
            boolean ok = comisionService.registrarComisionManual(
                    user.getIdUsuario(), monto, comentario, idVenta, null);

            if (ok) {
                showMessage("Comisión registrada.", false);

                if (refreshCallback != null) {
                    refreshCallback.run();
                }

                closeWindow();
            } else {
                showMessage("Error al registrar comisión.", true);
            }
        } catch (NumberFormatException ex) {
            showMessage("Monto e ID Venta deben ser numéricos.", true);
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showMessage(String msg, boolean isError) {
        formMessageLabel.setText(msg);
        formMessageLabel.setTextFill(isError ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.GREEN);
        formMessageLabel.setVisible(true);
    }
}
