package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.models.CategoriaProducto;
import com.cerrajeria.app.models.Producto;
import com.cerrajeria.app.services.CategoriaProductoService;
import com.cerrajeria.app.services.ProductoService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Nueva importación
import javafx.scene.Parent;     // Nueva importación
import javafx.scene.Scene;      // Nueva importación
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;   // Nueva importación
import javafx.stage.Stage;      // Nueva importación
import javafx.util.StringConverter;
import javafx.beans.property.ReadOnlyStringWrapper;

import java.io.IOException; // Necesario para el manejo de excepciones de FXML
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Controlador para la vista de Gestión de Productos.
 * Permite al Administrador crear, editar, visualizar, activar y desactivar productos.
 */
public class ProductsManagementController {

    // --- Componentes de la UI (inyectados desde FXML) ---
    @FXML private TextField productIdField;
    @FXML private TextField productNameField;
    @FXML private ComboBox<CategoriaProducto> productCategoryCombo; // ComboBox de Categorías
    @FXML private TextField productCostField;
    @FXML private TextField productPriceField;
    @FXML private TextField productStockField;
    @FXML private TextField productMinStockField;
    @FXML private Label formMessageLabel;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button deactivateButton;
    @FXML private Button activateButton;
    @FXML private TextField searchField;
    @FXML private TableView<Producto> productsTable;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colName;
    @FXML private TableColumn<Producto, String> colCategory; // Para mostrar el nombre de la categoría
    @FXML private TableColumn<Producto, BigDecimal> colPrice;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, Integer> colMinStock;
    @FXML private TableColumn<Producto, BigDecimal> colCost;
    @FXML private TableColumn<Producto, String> colStatus;
    @FXML private TableColumn<Producto, Boolean> colActive;

    // --- Servicios de negocio ---
    private ProductoService productoService;
    private CategoriaProductoService categoriaProductoService;
    private ObservableList<Producto> productList; // Lista observable para la tabla
    private ObservableList<CategoriaProducto> categoryList; // Lista observable para el ComboBox

    // --- Constructor ---
    public ProductsManagementController() {
        this.productoService = new ProductoService();
        this.categoriaProductoService = new CategoriaProductoService();
        this.productList = FXCollections.observableArrayList();
        this.categoryList = FXCollections.observableArrayList();
    }

    /**
     * Método de inicialización del controlador. Se llama automáticamente después de cargar el FXML.
     * Configura la tabla, carga los datos iniciales y el ComboBox de categorías.
     */
    @FXML
    private void initialize() {
        // Configurar las columnas de la tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colMinStock.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("costoInterno"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("activo"));

        // Columna para el nombre de la categoría
        colCategory.setCellValueFactory(cellData -> {
            int idCategoria = cellData.getValue().getIdCategoriaProducto();
            CategoriaProducto categoria = categoriaProductoService.obtenerCategoriaProductoPorId(idCategoria);
            return new ReadOnlyStringWrapper(categoria != null ? categoria.getNombre() : "Desconocida");
        });

        productsTable.setItems(productList);

        loadProducts();
        loadCategories(); // Carga las categorías al inicio

        productsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showProductDetails(newValue));

        formMessageLabel.setVisible(false);
        setEditDeleteButtonsDisable(true);

        // Validadores de entrada
        productStockField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) { productStockField.setText(oldVal); }
        });
        productMinStockField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) { productMinStockField.setText(oldVal); }
        });
        productCostField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) { productCostField.setText(oldVal); }
        });
        productPriceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) { productPriceField.setText(oldVal); }
        });
    }

    /**
     * Carga todos los productos de la base de datos y actualiza la tabla.
     */
    private void loadProducts() {
        productList.clear();
        productList.addAll(productoService.obtenerTodosLosProductos());
        productsTable.refresh();
    }

    /**
     * Carga todas las categorías de producto activas para el ComboBox.
     * Este método se llamará también cuando la ventana de categorías se cierre
     * para asegurar que el ComboBox de productos esté actualizado.
     */
    private void loadCategories() {
        categoryList.clear();
        categoryList.addAll(categoriaProductoService.obtenerCategoriasProductoActivas());
        productCategoryCombo.setItems(categoryList);
        productCategoryCombo.setConverter(new StringConverter<CategoriaProducto>() {
            @Override
            public String toString(CategoriaProducto categoria) {
                return categoria != null ? categoria.getNombre() : "";
            }
            @Override
            public CategoriaProducto fromString(String string) { return null; }
        });
    }

    /**
     * Muestra los detalles del producto seleccionado en el formulario para edición.
     * @param producto El producto seleccionado en la tabla.
     */
    private void showProductDetails(Producto producto) {
        if (producto != null) {
            productIdField.setText(String.valueOf(producto.getIdProducto()));
            productNameField.setText(producto.getNombre());
            CategoriaProducto categoriaSeleccionada = categoriaProductoService.obtenerCategoriaProductoPorId(producto.getIdCategoriaProducto());
            productCategoryCombo.getSelectionModel().select(categoriaSeleccionada);
            productCostField.setText(String.valueOf(producto.getCostoInterno()));
            productPriceField.setText(String.valueOf(producto.getPrecio()));
            productStockField.setText(String.valueOf(producto.getStock()));
            productMinStockField.setText(String.valueOf(producto.getStockMinimo()));
            formMessageLabel.setVisible(false);
            setEditDeleteButtonsDisable(false);
        } else {
            handleClearForm(null);
        }
    }

    /**
     * Habilita o deshabilita los botones de desactivar/activar/guardar según la selección de la tabla.
     * @param disable true para deshabilitar, false para habilitar.
     */
    private void setEditDeleteButtonsDisable(boolean disable) {
        saveButton.setDisable(false);
        deactivateButton.setDisable(disable);
        activateButton.setDisable(disable);
    }

    /**
     * Maneja la acción del botón "Guardar".
     * @param event El evento de acción.
     */
    @FXML
    private void handleSaveProduct(ActionEvent event) {
        formMessageLabel.setVisible(false);

        String nombre = productNameField.getText();
        CategoriaProducto categoria = productCategoryCombo.getSelectionModel().getSelectedItem();
        String costoStr = productCostField.getText();
        String precioStr = productPriceField.getText();
        String stockStr = productStockField.getText();
        String minStockStr = productMinStockField.getText();

        if (nombre.isEmpty() || categoria == null || costoStr.isEmpty() || precioStr.isEmpty() ||
                stockStr.isEmpty() || minStockStr.isEmpty()) {
            formMessageLabel.setText("Todos los campos son obligatorios.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            formMessageLabel.setVisible(true);
            return;
        }

        BigDecimal costoInterno;
        BigDecimal precio;
        int stock;
        int stockMinimo;

        try {
            costoInterno = new BigDecimal(costoStr);
            precio = new BigDecimal(precioStr);
            stock = Integer.parseInt(stockStr);
            stockMinimo = Integer.parseInt(minStockStr);

            if (costoInterno.compareTo(BigDecimal.ZERO) < 0 || precio.compareTo(BigDecimal.ZERO) < 0 || stock < 0 || stockMinimo < 0) {
                throw new NumberFormatException("Valores numéricos deben ser positivos.");
            }
        } catch (NumberFormatException e) {
            formMessageLabel.setText("Asegúrate de que Costo, Precio, Stock y Stock Mínimo sean números válidos y positivos.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            formMessageLabel.setVisible(true);
            return;
        }

        String productIdText = productIdField.getText();
        if (productIdText == null || productIdText.isEmpty() || productIdText.equals("Automático")) {
            boolean success = productoService.crearProducto(nombre, categoria.getIdCategoriaProducto(),
                    costoInterno, precio, stock, stockMinimo);
            if (success) {
                formMessageLabel.setText("Producto creado exitosamente.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                handleClearForm(null);
                loadProducts();
            } else {
                formMessageLabel.setText("Error al crear producto. Revise los datos.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
        } else {
            try {
                int idProducto = Integer.parseInt(productIdText);
                Producto productoExistente = productoService.obtenerProductoPorId(idProducto);

                if (productoExistente != null) {
                    productoExistente.setNombre(nombre);
                    productoExistente.setIdCategoriaProducto(categoria.getIdCategoriaProducto());
                    productoExistente.setCostoInterno(costoInterno);
                    productoExistente.setPrecio(precio);
                    productoExistente.setStock(stock);
                    productoExistente.setStockMinimo(stockMinimo);

                    boolean success = productoService.actualizarProducto(productoExistente);
                    if (success) {
                        formMessageLabel.setText("Producto actualizado exitosamente.");
                        formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                        handleClearForm(null);
                        loadProducts();
                    } else {
                        formMessageLabel.setText("Error al actualizar producto. Revise los datos.");
                        formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                    }
                } else {
                    formMessageLabel.setText("Error: Producto con ID " + idProducto + " no encontrado para actualizar.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                }
            } catch (NumberFormatException e) {
                formMessageLabel.setText("ID de producto inválido para actualizar.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
        }
        formMessageLabel.setVisible(true);
    }

    /**
     * Maneja la acción del botón "Limpiar". Limpia el formulario.
     * @param event El evento de acción.
     */
    @FXML
    private void handleClearForm(ActionEvent event) {
        productIdField.clear();
        productNameField.clear();
        productCategoryCombo.getSelectionModel().clearSelection();
        productCostField.clear();
        productPriceField.clear();
        productStockField.clear();
        productMinStockField.clear();
        formMessageLabel.setVisible(false);
        formMessageLabel.setText("");
        productsTable.getSelectionModel().clearSelection();
        setEditDeleteButtonsDisable(true);
    }

    /**
     * Maneja la acción del botón "Desactivar". Desactiva lógicamente el producto seleccionado.
     * @param event El evento de acción.
     */
    @FXML
    private void handleDeactivateProduct(ActionEvent event) {
        Producto selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Desactivación");
            alert.setHeaderText("Desactivar Producto: " + selectedProduct.getNombre());
            alert.setContentText("¿Está seguro de que desea desactivar este producto? No estará disponible para ventas.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = productoService.desactivarProducto(selectedProduct.getIdProducto());
                if (success) {
                    formMessageLabel.setText("Producto desactivado exitosamente.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                    loadProducts();
                    handleClearForm(null);
                } else {
                    formMessageLabel.setText("Error al desactivar producto. " +
                            "Asegúrese de que no sea el producto 'Otros' o que no haya referencias activas.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                }
            }
        } else {
            formMessageLabel.setText("Seleccione un producto para desactivar.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
        }
        formMessageLabel.setVisible(true);
    }

    /**
     * Maneja la acción del botón "Activar". Activa lógicamente el producto seleccionado.
     * @param event El evento de acción.
     */
    @FXML
    private void handleActivateProduct(ActionEvent event) {
        Producto selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Activación");
            alert.setHeaderText("Activar Producto: " + selectedProduct.getNombre());
            alert.setContentText("¿Está seguro de que desea activar este producto? Estará disponible para ventas.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = productoService.activarProducto(selectedProduct.getIdProducto());
                if (success) {
                    formMessageLabel.setText("Producto activado exitosamente.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                    loadProducts();
                    handleClearForm(null);
                } else {
                    formMessageLabel.setText("Error al activar producto.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                }
            }
        } else {
            formMessageLabel.setText("Seleccione un producto para activar.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
        }
        formMessageLabel.setVisible(true);
    }

    /**
     * Maneja la acción del botón "Buscar". Filtra los productos en la tabla.
     * @param event El evento de acción.
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadProducts();
            return;
        }

        ObservableList<Producto> filteredList = FXCollections.observableArrayList();
        for (Producto p : productoService.obtenerTodosLosProductos()) {
            if (String.valueOf(p.getIdProducto()).contains(searchText) ||
                    p.getNombre().toLowerCase().contains(searchText)) {
                filteredList.add(p);
            }
        }
        productList.setAll(filteredList);
    }

    /**
     * Maneja la acción del botón "Actualizar Tabla". Recarga todos los productos.
     * @param event El evento de acción.
     */
    @FXML
    private void refreshTable(ActionEvent event) {
        searchField.clear();
        loadProducts();
        formMessageLabel.setVisible(false);
        formMessageLabel.setText("");
        handleClearForm(null);
    }

    /**
     * Maneja la acción del botón "Gestionar Categorías".
     * Abre una nueva ventana modal para la gestión de categorías de producto.
     * @param event El evento de acción.
     */
    @FXML
    private void handleManageCategories(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/CategoryManagementView.fxml"));
            Parent parent = fxmlLoader.load();

            // Obtener el controlador de la ventana de categorías
            CategoryManagementController categoryController = fxmlLoader.getController();
            // Establecer un callback para que, al cerrar la ventana de categorías,
            // se recarguen las categorías en el ComboBox de productos.
            categoryController.setOnCategoriesUpdatedCallback(this::loadCategories);

            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setTitle("Gestionar Categorías de Producto");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Hace que la ventana sea modal
            stage.showAndWait(); // Espera a que la ventana se cierre

            // Cuando la ventana de categorías se cierra, aseguramos que el ComboBox se actualice
            loadCategories();

        } catch (IOException e) {
            System.err.println("Error al cargar la ventana de gestión de categorías: " + e.getMessage());
            e.printStackTrace();
            formMessageLabel.setText("Error al abrir la gestión de categorías.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            formMessageLabel.setVisible(true);
        }
    }
}