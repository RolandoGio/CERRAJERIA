package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.models.Comision;
import com.cerrajeria.app.models.Usuario;
import com.cerrajeria.app.services.ComisionService;
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
import javafx.util.StringConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

public class CommissionsManagementController {

    @FXML private TableView<Comision> commissionsTable;
    @FXML private TableColumn<Comision, Integer> colId;
    @FXML private TableColumn<Comision, String> colUser;
    @FXML private TableColumn<Comision, String> colVenta;   // Ahora String para mostrar nombre
    @FXML private TableColumn<Comision, String> colServicio; // Ahora String para mostrar nombre
    @FXML private TableColumn<Comision, BigDecimal> colMonto;
    @FXML private TableColumn<Comision, String> colEstado;
    @FXML private TableColumn<Comision, String> colComentario;
    @FXML private TableColumn<Comision, String> colComentarioAdmin;
    @FXML private TableColumn<Comision, String> colManual;

    @FXML private ComboBox<Usuario> userCombo;
    @FXML private TextField montoField;
    @FXML private TextField ventaIdField;
    @FXML private TextField servicioIdField;
    @FXML private TextArea comentarioArea;
    @FXML private TextArea comentarioAdminArea;
    @FXML private ComboBox<String> estadoCombo;

    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Label formMessageLabel;

    private ComisionService comisionService;
    private UsuarioService usuarioService;
    private ObservableList<Comision> commissionList;
    private ObservableList<Usuario> userList;

    public CommissionsManagementController() {
        this.comisionService = new ComisionService();
        this.usuarioService = new UsuarioService();
        this.commissionList = FXCollections.observableArrayList();
        this.userList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idComision"));

        colUser.setCellValueFactory(cellData -> {
            Usuario usuario = usuarioService.obtenerUsuarioPorId(cellData.getValue().getIdUsuario());
            return new javafx.beans.property.SimpleStringProperty(usuario != null ? usuario.getNombre() : "Desconocido");
        });

        // Mostrar el nombre del producto asociado a la venta (o "N/A" si no hay)
        colVenta.setCellValueFactory(cellData -> {
            Integer idVenta = cellData.getValue().getIdVenta();
            String nombreProducto = (idVenta != null) ? comisionService.obtenerNombreProductoPorVenta(idVenta) : null;
            return new javafx.beans.property.SimpleStringProperty(nombreProducto != null ? nombreProducto : "N/A");
        });

        // Mostrar el nombre del servicio (o "N/A" si no hay)
        colServicio.setCellValueFactory(cellData -> {
            Integer idServicio = cellData.getValue().getIdServicio();
            String nombreServicio = (idServicio != null) ? comisionService.obtenerNombreServicioPorId(idServicio) : null;
            return new javafx.beans.property.SimpleStringProperty(nombreServicio != null ? nombreServicio : "N/A");
        });

        colMonto.setCellValueFactory(new PropertyValueFactory<>("montoComision"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colComentario.setCellValueFactory(new PropertyValueFactory<>("comentario"));
        colComentarioAdmin.setCellValueFactory(new PropertyValueFactory<>("comentarioAdmin"));
        colManual.setCellValueFactory(cellData -> {
            Boolean esManual = cellData.getValue().isEsManual();
            String valor = esManual != null && esManual ? "Sí" : "No";
            return new javafx.beans.property.SimpleStringProperty(valor);
        });

        commissionsTable.setItems(commissionList);
        loadCommissions();
        loadUsers();

        estadoCombo.setItems(FXCollections.observableArrayList("Pendiente", "Pagado"));
        estadoCombo.getSelectionModel().selectFirst();

        commissionsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> showCommissionDetails(newVal)
        );

        formMessageLabel.setVisible(false);
        setEditButtonsDisable(true);
    }

    private void loadCommissions() {
        commissionList.setAll(comisionService.obtenerTodasLasComisiones());
    }

    private void loadUsers() {
        userList.setAll(usuarioService.obtenerTodosLosUsuarios());
        userCombo.setItems(userList);
        userCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Usuario user) {
                return user != null ? user.getNombre() : "";
            }
            @Override
            public Usuario fromString(String s) { return null; }
        });
    }

    private void showCommissionDetails(Comision comision) {
        if (comision != null) {
            Usuario usuario = usuarioService.obtenerUsuarioPorId(comision.getIdUsuario());
            userCombo.getSelectionModel().select(usuario);

            montoField.setText(comision.getMontoComision().toPlainString());
            ventaIdField.setText(comision.getIdVenta() != null ? comision.getIdVenta().toString() : "");
            servicioIdField.setText(comision.getIdServicio() != null ? comision.getIdServicio().toString() : "");
            comentarioArea.setText(comision.getComentario());
            comentarioAdminArea.setText(comision.getComentarioAdmin());
            estadoCombo.getSelectionModel().select(comision.getEstado());

            setEditButtonsDisable(false);
        } else {
            clearForm();
        }
    }

    private void setEditButtonsDisable(boolean disable) {
        updateButton.setDisable(disable);
        deleteButton.setDisable(disable);
    }

    @FXML
    private void handleUpdateCommission(ActionEvent event) {
        Comision selected = commissionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Seleccione una comisión para actualizar.", true);
            return;
        }

        String nuevoEstado = estadoCombo.getSelectionModel().getSelectedItem();
        String comentarioAdmin = comentarioAdminArea.getText();
        String montoStr = montoField.getText();

        if (montoStr.isBlank()) {
            showMessage("El monto no puede estar vacío.", true);
            return;
        }

        BigDecimal nuevoMonto;
        try {
            nuevoMonto = new BigDecimal(montoStr);
        } catch (NumberFormatException ex) {
            showMessage("El monto debe ser numérico.", true);
            return;
        }

        selected.setMontoComision(nuevoMonto);
        selected.setEstado(nuevoEstado);
        selected.setComentarioAdmin(comentarioAdmin);

        boolean ok = comisionService.actualizarComisionCompleta(selected);

        if (ok) {
            showMessage("Comisión actualizada correctamente.", false);
            loadCommissions();
            clearForm();
        } else {
            showMessage("No se pudo actualizar la comisión.", true);
        }
    }

    @FXML
    private void handleDeleteCommission(ActionEvent event) {
        Comision selected = commissionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Seleccione una comisión para eliminar.", true);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("Eliminar Comisión ID " + selected.getIdComision());
        alert.setContentText("¿Está seguro de eliminar esta comisión?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean ok = comisionService.eliminarComision(
                    selected.getIdComision(), 0, "Administrador");
            if (ok) {
                showMessage("Comisión eliminada.", false);
                loadCommissions();
                clearForm();
            } else {
                showMessage("No se pudo eliminar la comisión.", true);
            }
        }
    }

    @FXML
    private void handleRefreshTable(ActionEvent event) {
        loadCommissions();
        clearForm();
        formMessageLabel.setVisible(false);
    }

    @FXML
    private void handleClearForm(ActionEvent event) {
        clearForm();
    }

    private void clearForm() {
        commissionsTable.getSelectionModel().clearSelection();
        userCombo.getSelectionModel().clearSelection();
        montoField.clear();
        ventaIdField.clear();
        servicioIdField.clear();
        comentarioArea.clear();
        comentarioAdminArea.clear();
        estadoCombo.getSelectionModel().selectFirst();
        setEditButtonsDisable(true);
    }

    private void showMessage(String msg, boolean isError) {
        formMessageLabel.setText(msg);
        formMessageLabel.setTextFill(isError ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.GREEN);
        formMessageLabel.setVisible(true);
    }

    @FXML
    private void handleCreateCommissionModal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CommissionCreationModalView.fxml"));
            Parent root = loader.load();

            CommissionCreationModalController modalController = loader.getController();
            modalController.setRefreshCallback(this::loadCommissions);

            Stage stage = new Stage();
            stage.setTitle("Crear Comisión Manual");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Error al abrir el modal: " + e.getMessage(), true);
        }
    }
}
