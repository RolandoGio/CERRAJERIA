package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.models.CategoriaProducto;
import com.cerrajeria.app.models.Producto;
import com.cerrajeria.app.services.CategoriaProductoService;
import com.cerrajeria.app.services.ProductoService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para la vista de productos de un vendedor.
 * Muestra un listado de productos en modo solo lectura, con buscador y filtro por categoría.
 */
public class VendedorProductsController {

    @FXML
    private TableView<Producto> productsTable;
    @FXML
    private TableColumn<Producto, String> colName;
    @FXML
    private TableColumn<Producto, String> colCategory;
    @FXML
    private TableColumn<Producto, java.math.BigDecimal> colPrice;
    @FXML
    private TableColumn<Producto, Integer> colStock;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private Label statusLabel;

    private final ProductoService productoService;
    private final CategoriaProductoService categoriaProductoService;
    private final ObservableList<Producto> productList;
    private final ObservableList<String> categoryList;

    public VendedorProductsController() {
        this.productoService = new ProductoService();
        this.categoriaProductoService = new CategoriaProductoService();
        this.productList = FXCollections.observableArrayList();
        this.categoryList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        // Configurar columnas
        colName.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCategory.setCellValueFactory(cellData -> {
            int idCat = cellData.getValue().getIdCategoriaProducto();
            CategoriaProducto cat = categoriaProductoService.obtenerCategoriaProductoPorId(idCat);
            return new ReadOnlyStringWrapper(cat != null ? cat.getNombre() : "Desconocida");
        });

        productsTable.setItems(productList);

        // Inicializar ComboBox de categorías
        loadCategories();

        // Cargar todos los productos por defecto
        loadProducts();

        statusLabel.setVisible(false);
    }

    private void loadProducts() {
        List<Producto> productos = productoService.obtenerProductosActivos();
        productList.setAll(productos);
    }

    private void loadCategories() {
        categoryList.clear();
        categoryList.add("Todas"); // Opción para ver todos los productos

        List<CategoriaProducto> categorias = categoriaProductoService.obtenerTodasCategoriasProducto();

        categorias.forEach(cat -> categoryList.add(cat.getNombre()));

        categoryComboBox.setItems(categoryList);
        categoryComboBox.getSelectionModel().selectFirst(); // Seleccionar "Todas" por defecto
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchField.getText().trim().toLowerCase();
        String selectedCategory = categoryComboBox.getValue();

        List<Producto> productosFiltrados = productoService.obtenerProductosActivos().stream()
                .filter(p -> p.getNombre().toLowerCase().contains(searchText)
                        || String.valueOf(p.getIdProducto()).contains(searchText))
                .filter(p -> {
                    if ("Todas".equals(selectedCategory)) {
                        return true;
                    }
                    CategoriaProducto cat = categoriaProductoService.obtenerCategoriaProductoPorId(p.getIdCategoriaProducto());
                    return cat != null && selectedCategory.equalsIgnoreCase(cat.getNombre());
                })
                .collect(Collectors.toList());

        productList.setAll(productosFiltrados);

        if (productosFiltrados.isEmpty()) {
            statusLabel.setText("No se encontraron productos que coincidan.");
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setVisible(true);
        } else {
            statusLabel.setVisible(false);
        }
    }
}
