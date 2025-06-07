package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.SessionManager;
import com.cerrajeria.app.models.Comision;
import com.cerrajeria.app.models.Usuario;
import com.cerrajeria.app.models.Venta;
import com.cerrajeria.app.services.ReporteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador para la vista de reportes del vendedor.
 * Muestra el historial de ventas y comisiones del vendedor logueado.
 */
public class VendedorReportsController {

    @FXML private TableView<Venta> salesTable;
    @FXML private TableColumn<Venta, Integer> colSaleId;
    @FXML private TableColumn<Venta, LocalDateTime> colSaleDate;
    @FXML private TableColumn<Venta, BigDecimal> colSaleTotal;

    @FXML private TableView<Comision> commissionsTable;
    @FXML private TableColumn<Comision, Integer> colComId;
    @FXML private TableColumn<Comision, BigDecimal> colComAmount;
    @FXML private TableColumn<Comision, String> colComState;
    @FXML private TableColumn<Comision, LocalDateTime> colComDate;

    private final ReporteService reporteService = new ReporteService();
    private int idVendedor;

    @FXML
    private void initialize() {
        Usuario u = SessionManager.getUsuarioActual();
        idVendedor = (u != null) ? u.getIdUsuario() : -1;

        colSaleId.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        colSaleDate.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        colSaleTotal.setCellValueFactory(new PropertyValueFactory<>("totalVenta"));

        colComId.setCellValueFactory(new PropertyValueFactory<>("idComision"));
        colComAmount.setCellValueFactory(new PropertyValueFactory<>("montoComision"));
        colComState.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colComDate.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));

        loadData();
    }

    private void loadData() {
        if (idVendedor == -1) {
            salesTable.setItems(FXCollections.emptyObservableList());
            commissionsTable.setItems(FXCollections.emptyObservableList());
            return;
        }
        Map<String, List<?>> data = reporteService.obtenerHistorialVendedor(idVendedor);
        List<Venta> ventas = (List<Venta>) data.getOrDefault("ventas", List.of());
        List<Comision> comisiones = (List<Comision>) data.getOrDefault("comisiones", List.of());

        ObservableList<Venta> ventasObs = FXCollections.observableArrayList(ventas);
        ObservableList<Comision> comObs = FXCollections.observableArrayList(comisiones);

        salesTable.setItems(ventasObs);
        commissionsTable.setItems(comObs);
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadData();
    }
}