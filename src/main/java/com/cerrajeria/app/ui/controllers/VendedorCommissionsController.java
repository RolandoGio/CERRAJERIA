package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.SessionManager;
import com.cerrajeria.app.models.Comision;
import com.cerrajeria.app.models.Usuario;
import com.cerrajeria.app.services.ComisionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Controlador para la vista de comisiones de un vendedor.
 * Permite visualizar las comisiones, ver el comentario del administrador
 * y actualizar el comentario del vendedor si la comisión está pendiente.
 */
public class VendedorCommissionsController {

    @FXML private TableView<Comision> commissionsTable;
    @FXML private TableColumn<Comision, Integer> colId;
    @FXML private TableColumn<Comision, String> colVenta;
    @FXML private TableColumn<Comision, String> colServicio;
    @FXML private TableColumn<Comision, java.math.BigDecimal> colMonto;
    @FXML private TableColumn<Comision, String> colEstado;
    @FXML private TableColumn<Comision, String> colComentario;
    @FXML private TableColumn<Comision, String> colComentarioAdmin;

    @FXML private TextArea comentarioField;
    @FXML private TextArea comentarioAdminField;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Label messageLabel;

    private final ComisionService comisionService = new ComisionService();
    private final ObservableList<Comision> commissionList = FXCollections.observableArrayList();
    private int idVendedor;

    @FXML
    private void initialize() {
        Usuario u = SessionManager.getUsuarioActual();
        idVendedor = (u != null) ? u.getIdUsuario() : -1;

        // Configuración de columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("idComision"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("montoComision"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colComentario.setCellValueFactory(new PropertyValueFactory<>("comentario"));
        colComentarioAdmin.setCellValueFactory(new PropertyValueFactory<>("comentarioAdmin"));
        colVenta.setCellValueFactory(c -> new SimpleStringProperty(
                comisionService.obtenerNombreProductoPorVenta(c.getValue().getIdVenta())
        ));
        colServicio.setCellValueFactory(c -> new SimpleStringProperty(
                comisionService.obtenerNombreServicioPorId(c.getValue().getIdServicio())
        ));

        commissionsTable.setItems(commissionList);
        commissionsTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> showDetails(newVal));

        // Inicializar el campo del comentario del administrador como solo lectura
        comentarioAdminField.setEditable(false);

        loadCommissions();
        disableForm(true);
        messageLabel.setVisible(false);
    }

    /**
     * Carga las comisiones del vendedor.
     */
    private void loadCommissions() {
        if (idVendedor != -1) {
            List<Comision> comisiones = comisionService.obtenerComisionesPorUsuario(idVendedor);
            commissionList.setAll(comisiones);
        }
    }

    /**
     * Muestra el detalle de la comisión seleccionada,
     * habilitando la edición solo si la comisión está en estado "Pendiente".
     */
    private void showDetails(Comision comision) {
        if (comision != null) {
            comentarioField.setText(comision.getComentario());
            comentarioAdminField.setText(comision.getComentarioAdmin());
            boolean editable = "Pendiente".equalsIgnoreCase(comision.getEstado());
            comentarioField.setDisable(!editable);
            updateButton.setDisable(!editable);
            // Botón de eliminar solo activo si es Pendiente y es Manual
            deleteButton.setDisable(!editable || !comision.isEsManual());
        } else {
            comentarioField.clear();
            comentarioAdminField.clear();
            disableForm(true);
        }
    }

    /**
     * Desactiva o activa los campos de edición.
     */
    private void disableForm(boolean disable) {
        comentarioField.setDisable(disable);
        updateButton.setDisable(disable);
        deleteButton.setDisable(disable);
    }

    /**
     * Permite al vendedor actualizar su propio comentario.
     */
    @FXML
    private void handleActualizarComentario(ActionEvent event) {
        Comision selected = commissionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        boolean ok = comisionService.actualizarComentarioVendedor(
                selected.getIdComision(), idVendedor, comentarioField.getText());
        if (ok) {
            showMessage("Comentario actualizado", false);
            loadCommissions();
        } else {
            showMessage("No se pudo actualizar", true);
        }
    }

    /**
     * Desactiva la comisión en lugar de borrarla físicamente.
     */
    @FXML
    private void handleEliminarComision(ActionEvent event) {
        Comision selected = commissionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Marcar la comisión como desactivada (borrado lógico)
        selected.setEstado("Desactivada");
        boolean ok = comisionService.actualizarComisionCompleta(selected);
        if (ok) {
            showMessage("Comisión desactivada", false);
            loadCommissions();
        } else {
            showMessage("No se pudo desactivar", true);
        }
    }

    /**
     * Refresca la lista de comisiones.
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        loadCommissions();
    }

    /**
     * Muestra un mensaje de éxito o error.
     */
    private void showMessage(String msg, boolean error) {
        messageLabel.setText(msg);
        messageLabel.setTextFill(error ? Color.RED : Color.GREEN);
        messageLabel.setVisible(true);
    }
}
