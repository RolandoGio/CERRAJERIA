package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.models.CategoriaServicio;
import com.cerrajeria.app.models.Servicio;
import com.cerrajeria.app.services.CategoriaServicioService;
import com.cerrajeria.app.services.ServicioService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para la vista de servicios de un vendedor.
 * Permite listar servicios disponibles y filtrarlos por categoría y nombre.
 */
public class VendedorServicesController {

    @FXML
    private TableView<Servicio> servicesTable;
    @FXML
    private TableColumn<Servicio, String> colName;
    @FXML
    private TableColumn<Servicio, String> colDescription;
    @FXML
    private TableColumn<Servicio, String> colCategory;
    @FXML
    private TableColumn<Servicio, java.math.BigDecimal> colPrice;

    @FXML
    private ComboBox<CategoriaServicio> categoryComboBox;
    @FXML
    private TextField searchField;

    private final ServicioService servicioService;
    private final CategoriaServicioService categoriaServicioService;
    private final ObservableList<Servicio> serviceList;

    public VendedorServicesController() {
        this.servicioService = new ServicioService();
        this.categoriaServicioService = new CategoriaServicioService();
        this.serviceList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        // Configurar columnas de la tabla
        colName.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colCategory.setCellValueFactory(cellData -> {
            int id = cellData.getValue().getIdCategoriaServicio();
            CategoriaServicio cat = categoriaServicioService.obtenerCategoriaServicioPorId(id);
            return new ReadOnlyStringWrapper(cat != null ? cat.getNombre() : "Desconocida");
        });

        // Configurar la tabla
        servicesTable.setItems(serviceList);

        // Configurar el ComboBox de categorías para mostrar solo el nombre
        categoryComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(CategoriaServicio item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Ver todo"); // Mostrar "Ver todo" cuando esté vacío
                } else {
                    setText(item.getNombre());
                }
            }
        });
        categoryComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(CategoriaServicio item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Ver todo"); // Mostrar "Ver todo" cuando esté vacío
                } else {
                    setText(item.getNombre());
                }
            }
        });

        // Cargar categorías activas
        List<CategoriaServicio> categorias = categoriaServicioService.obtenerCategoriasServicioActivas();
        categoryComboBox.getItems().add(null); // Para la opción "Ver todo"
        categoryComboBox.getItems().addAll(categorias);
        categoryComboBox.getSelectionModel().selectFirst(); // Seleccionar "Ver todo" por defecto

        // Listeners para filtrar servicios
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Cargar servicios
        loadServices();
    }

    /**
     * Carga los servicios activos.
     */
    private void loadServices() {
        List<Servicio> servicios = servicioService.obtenerServiciosActivos();
        serviceList.setAll(servicios);
    }

    /**
     * Aplica los filtros de categoría y nombre de búsqueda.
     */
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        CategoriaServicio selectedCategory = categoryComboBox.getValue();

        List<Servicio> filtered = servicioService.obtenerServiciosActivos().stream()
                .filter(s -> {
                    boolean matchesCategory = (selectedCategory == null) || (s.getIdCategoriaServicio() == selectedCategory.getIdCategoriaServicio());
                    boolean matchesName = s.getNombre().toLowerCase().contains(searchText);
                    return matchesCategory && matchesName;
                })
                .collect(Collectors.toList());

        serviceList.setAll(filtered);
    }
}
