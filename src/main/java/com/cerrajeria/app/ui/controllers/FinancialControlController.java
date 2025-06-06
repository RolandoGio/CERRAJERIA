package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.models.ControlFinanciero;
import com.cerrajeria.app.services.ControlFinancieroService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class FinancialControlController {

    @FXML private TextField recordIdField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField descriptionField;
    @FXML private TextField amountField;
    @FXML private Label formMessageLabel;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button deleteButton;

    @FXML private TableView<ControlFinanciero> recordsTable;
    @FXML private TableColumn<ControlFinanciero, Integer> colId;
    @FXML private TableColumn<ControlFinanciero, String> colType;
    @FXML private TableColumn<ControlFinanciero, String> colDescription;
    @FXML private TableColumn<ControlFinanciero, BigDecimal> colAmount;
    @FXML private TableColumn<ControlFinanciero, BigDecimal> colCost;
    @FXML private TableColumn<ControlFinanciero, String> colDate;

    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpenseLabel;
    @FXML private Label balanceLabel;

    @FXML private DatePicker filterDatePicker;
    @FXML private ComboBox<String> filterTypeCombo;

    private final ControlFinancieroService service;
    private final ObservableList<ControlFinanciero> recordList;

    public FinancialControlController() {
        this.service = new ControlFinancieroService();
        this.recordList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList("Ingreso", "Egreso"));

        colId.setCellValueFactory(new PropertyValueFactory<>("idControlFinanciero"));
        colType.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("costo"));
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getFechaCreacion() != null
                        ? cellData.getValue().getFechaCreacion().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        : ""
        ));

        filterTypeCombo.setItems(FXCollections.observableArrayList("Día", "Semana", "Mes", "Trimestre", "Año"));

        recordsTable.setItems(recordList);
        recordsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> showRecordDetails(newVal));

        loadRecords();
        updateTotals();

        deleteButton.setDisable(true);
        formMessageLabel.setVisible(false);
    }

    private void loadRecords() {
        recordList.clear();
        recordList.addAll(service.obtenerTodosLosRegistrosFinancieros());
    }

    private void showRecordDetails(ControlFinanciero registro) {
        if (registro != null) {
            recordIdField.setText(String.valueOf(registro.getIdControlFinanciero()));
            typeCombo.getSelectionModel().select(registro.getTipo());
            descriptionField.setText(registro.getDescripcion());
            amountField.setText(registro.getMonto().toString());
            deleteButton.setDisable(false);
        } else {
            handleClearForm(null);
        }
    }

    @FXML
    private void handleSaveRecord(ActionEvent event) {
        formMessageLabel.setVisible(false);

        String tipo = typeCombo.getSelectionModel().getSelectedItem();
        String descripcion = descriptionField.getText().trim();
        String montoText = amountField.getText().trim();

        if (tipo == null || descripcion.isEmpty() || montoText.isEmpty()) {
            formMessageLabel.setText("Tipo, descripción y monto son obligatorios.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            formMessageLabel.setVisible(true);
            return;
        }

        BigDecimal monto;
        try {
            monto = new BigDecimal(montoText);
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            formMessageLabel.setText("Monto inválido. Debe ser positivo.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            formMessageLabel.setVisible(true);
            return;
        }

        // Asignamos un costo básico (ejemplo): 0 para servicios y monto para productos.
        BigDecimal costo = tipo.equalsIgnoreCase("Ingreso") ? BigDecimal.ZERO : monto;

        String idText = recordIdField.getText();
        boolean success;
        if (idText == null || idText.isEmpty()) {
            // Lógica de inserción: la toma el trigger
            success = tipo.equalsIgnoreCase("Ingreso")
                    ? service.registrarIngreso(descripcion, monto)
                    : service.registrarEgreso(descripcion, monto);
            if (success) {
                formMessageLabel.setText("Registro creado exitosamente.");
            }
        } else {
            try {
                int id = Integer.parseInt(idText);
                ControlFinanciero reg = new ControlFinanciero(id, tipo, descripcion, monto, costo, null, null);
                success = service.actualizarRegistroFinanciero(reg);
                if (success) {
                    formMessageLabel.setText("Registro actualizado exitosamente.");
                }
            } catch (NumberFormatException e) {
                formMessageLabel.setText("ID inválido para actualizar.");
                success = false;
            }
        }

        if (success) {
            formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            loadRecords();
            updateTotals();
            handleClearForm(null);
        } else {
            formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
        formMessageLabel.setVisible(true);
    }

    @FXML
    private void handleDeleteRecord(ActionEvent event) {
        ControlFinanciero selected = recordsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("Eliminar registro seleccionado");
        alert.setContentText("¿Está seguro de eliminar este registro?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = service.eliminarRegistroFinanciero(selected.getIdControlFinanciero());
            if (success) {
                recordList.remove(selected);
                updateTotals();
                formMessageLabel.setText("Registro eliminado.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                handleClearForm(null);
            } else {
                formMessageLabel.setText("Error al eliminar registro.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
            formMessageLabel.setVisible(true);
        }
    }

    @FXML
    private void handleClearForm(ActionEvent event) {
        recordIdField.clear();
        typeCombo.getSelectionModel().clearSelection();
        descriptionField.clear();
        amountField.clear();
        recordsTable.getSelectionModel().clearSelection();
        deleteButton.setDisable(true);
        formMessageLabel.setVisible(false);
    }

    private void updateTotals() {
        totalIncomeLabel.setText(String.format("Total Ingresos: $%.2f", service.calcularTotalIngresos()));
        totalExpenseLabel.setText(String.format("Total Egresos: $%.2f", service.calcularTotalEgresos()));
        balanceLabel.setText(String.format("Balance: $%.2f", service.calcularBalanceTotal()));
    }

    @FXML
    private void handleFilter() {
        LocalDate date = filterDatePicker.getValue();
        String tipoFiltro = filterTypeCombo.getSelectionModel().getSelectedItem();

        if (date == null || tipoFiltro == null) {
            formMessageLabel.setText("Seleccione una fecha y un tipo de filtro.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            formMessageLabel.setVisible(true);
            return;
        }

        List<ControlFinanciero> filtrados = service.obtenerRegistrosPorPeriodo(date, tipoFiltro);
        recordList.setAll(filtrados);
        updateTotals();
    }

    @FXML
    private void handleClearFilter() {
        loadRecords();
        updateTotals();
        filterDatePicker.setValue(null);
        filterTypeCombo.getSelectionModel().clearSelection();
        formMessageLabel.setVisible(false);
    }
}
