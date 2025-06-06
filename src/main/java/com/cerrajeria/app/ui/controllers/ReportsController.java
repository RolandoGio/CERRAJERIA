package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.models.Comision;
import com.cerrajeria.app.models.Usuario;
import com.cerrajeria.app.models.Venta;
import com.cerrajeria.app.services.ReporteService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para la vista de reportes globales.
 */
public class ReportsController {

    // --- Ventas ---
    @FXML private TableView<Venta> salesTable;
    @FXML private TableColumn<Venta, Integer> colSaleId;
    @FXML private TableColumn<Venta, String> colSaleUser;
    @FXML private TableColumn<Venta, LocalDateTime> colSaleDate;
    @FXML private TableColumn<Venta, BigDecimal> colSaleTotal;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<Usuario> userFilterCombo;
    @FXML private Label salesTotalLabel;

    // --- Comisiones ---
    @FXML private TableView<Comision> commissionsTable;
    @FXML private TableColumn<Comision, Integer> colComId;
    @FXML private TableColumn<Comision, String> colComUser;
    @FXML private TableColumn<Comision, BigDecimal> colComAmount;
    @FXML private TableColumn<Comision, String> colComState;
    @FXML private TableColumn<Comision, LocalDateTime> colComDate;
    @FXML private ComboBox<Usuario> commissionUserCombo;
    @FXML private ComboBox<String> commissionStateCombo;
    @FXML private Label commissionsTotalLabel;

    // --- Estadísticas ---
    @FXML private TableView<Map.Entry<String, Integer>> topProductsTable;
    @FXML private TableColumn<Map.Entry<String, Integer>, String> colProdName;
    @FXML private TableColumn<Map.Entry<String, Integer>, Integer> colProdCount;
    @FXML private TableView<Map.Entry<String, Integer>> topServicesTable;
    @FXML private TableColumn<Map.Entry<String, Integer>, String> colServName;
    @FXML private TableColumn<Map.Entry<String, Integer>, Integer> colServCount;
    @FXML private Label incomeLabel;
    @FXML private Label expensesLabel;
    @FXML private Label balanceLabel;

    private final ReporteService reporteService;
    private ObservableList<Usuario> usuarios;

    public ReportsController() {
        this.reporteService = new ReporteService();
    }

    @FXML
    private void initialize() {
        // Configurar ComboBoxes
        usuarios = FXCollections.observableArrayList(reporteService.obtenerTodosLosUsuarios());
        userFilterCombo.setItems(usuarios);
        commissionUserCombo.setItems(usuarios);

        // Mostrar solo nombres en ComboBoxes
        userFilterCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Usuario usuario, boolean empty) {
                super.updateItem(usuario, empty);
                setText((usuario == null || empty) ? "" : usuario.getNombre());
            }
        });
        userFilterCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Usuario usuario, boolean empty) {
                super.updateItem(usuario, empty);
                setText((usuario == null || empty) ? "" : usuario.getNombre());
            }
        });
        commissionUserCombo.setCellFactory(userFilterCombo.getCellFactory());
        commissionUserCombo.setButtonCell(userFilterCombo.getButtonCell());

        commissionStateCombo.setItems(FXCollections.observableArrayList("Pendiente", "Aprobado", "Rechazado", "Pagado"));

        // Configuración de columnas de ventas
        colSaleId.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        colSaleUser.setCellValueFactory(cellData -> new SimpleObjectProperty<>(
                resolveUserName(cellData.getValue().getIdUsuario())));
        colSaleDate.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        colSaleTotal.setCellValueFactory(new PropertyValueFactory<>("totalVenta"));

        // Configuración de columnas de comisiones
        colComId.setCellValueFactory(new PropertyValueFactory<>("idComision"));
        colComUser.setCellValueFactory(cellData -> new SimpleObjectProperty<>(
                resolveUserName(cellData.getValue().getIdUsuario())));
        colComAmount.setCellValueFactory(new PropertyValueFactory<>("montoComision"));
        colComState.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colComDate.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));

        // Columnas para estadísticas
        colProdName.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getKey()));
        colProdCount.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getValue()));
        colServName.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getKey()));
        colServCount.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getValue()));

        // Inicializar datos
        refreshSales();
        refreshCommissions();
        refreshStats();
    }

    // --- Métodos para refrescar datos ---
    @FXML private void handleRefreshSales() { refreshSales(); }
    @FXML private void handleClearSalesFilters() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        userFilterCombo.getSelectionModel().clearSelection();
        refreshSales();
    }

    private void refreshSales() {
        LocalDate desdeFecha = fromDatePicker.getValue();
        LocalDate hastaFecha = toDatePicker.getValue();
        LocalDateTime desde = (desdeFecha != null) ? desdeFecha.atStartOfDay() : null;
        LocalDateTime hasta = (hastaFecha != null) ? hastaFecha.atTime(23, 59, 59) : null;

        Integer idUsuario = Optional.ofNullable(userFilterCombo.getValue())
                .map(Usuario::getIdUsuario)
                .orElse(null);

        ObservableList<Venta> ventas = FXCollections.observableArrayList(
                reporteService.obtenerTodasLasVentasFiltradas(desde, hasta, idUsuario));
        salesTable.setItems(ventas);

        BigDecimal total = ventas.stream()
                .map(Venta::getTotalVenta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        salesTotalLabel.setText("Total: " + total);
    }

    @FXML private void handleRefreshCommissions() { refreshCommissions(); }
    @FXML private void handleClearCommissionsFilters() {
        commissionUserCombo.getSelectionModel().clearSelection();
        commissionStateCombo.getSelectionModel().clearSelection();
        refreshCommissions();
    }

    private void refreshCommissions() {
        Integer idUsuario = Optional.ofNullable(commissionUserCombo.getValue())
                .map(Usuario::getIdUsuario)
                .orElse(null);
        String estado = commissionStateCombo.getValue();

        ObservableList<Comision> comisiones = FXCollections.observableArrayList(
                reporteService.obtenerTodasLasComisionesFiltradas(idUsuario, estado));
        commissionsTable.setItems(comisiones);

        BigDecimal total = comisiones.stream()
                .map(Comision::getMontoComision)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        commissionsTotalLabel.setText("Total: " + total);
    }

    @FXML private void handleRefreshStats() { refreshStats(); }
    private void refreshStats() {
        ObservableList<Map.Entry<String, Integer>> productos = FXCollections.observableArrayList(
                reporteService.obtenerProductosMasVendidos(5).entrySet());
        ObservableList<Map.Entry<String, Integer>> servicios = FXCollections.observableArrayList(
                reporteService.obtenerServiciosMasVendidos(5).entrySet());
        topProductsTable.setItems(productos);
        topServicesTable.setItems(servicios);

        Map<String, BigDecimal> resumen = reporteService.obtenerResumenFinanciero();
        incomeLabel.setText("Ingresos: " + resumen.get("totalIngresos"));
        expensesLabel.setText("Egresos: " + resumen.get("totalEgresos"));
        balanceLabel.setText("Balance: " + resumen.get("balanceTotal"));
    }

    // --- Ayudante para obtener el nombre del usuario ---
    private String resolveUserName(int idUsuario) {
        return usuarios.stream()
                .filter(u -> u.getIdUsuario() == idUsuario)
                .map(Usuario::getNombre)
                .findFirst()
                .orElse("Desconocido");
    }
}
