package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.models.CategoriaProducto;
import com.cerrajeria.app.services.CategoriaProductoService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage; // Para cerrar la ventana modal

import java.util.Optional;

/**
 * Controlador para la ventana de Gestión de Categorías de Producto.
 * Permite crear, editar, activar y desactivar categorías.
 */
public class CategoryManagementController {

    // --- Componentes de la UI (inyectados desde FXML) ---
    @FXML private TextField categoryIdField;
    @FXML private TextField categoryNameField;
    @FXML private Label formMessageLabel;
    @FXML private Button saveCategoryButton;
    @FXML private Button clearCategoryFormButton;
    @FXML private Button deactivateCategoryButton;
    @FXML private Button activateCategoryButton;
    @FXML private Button closeButton;
    @FXML private TableView<CategoriaProducto> categoriesTable;
    @FXML private TableColumn<CategoriaProducto, Integer> colCatId;
    @FXML private TableColumn<CategoriaProducto, String> colCatName;
    @FXML private TableColumn<CategoriaProducto, Boolean> colCatActive;

    // --- Servicio de negocio ---
    private CategoriaProductoService categoriaProductoService;
    private ObservableList<CategoriaProducto> categoryList; // Lista observable para la tabla

    // Un callback para notificar a la vista de productos cuando se actualizan las categorías
    private Runnable onCategoriesUpdatedCallback;

    // --- Constructor ---
    public CategoryManagementController() {
        this.categoriaProductoService = new CategoriaProductoService();
        this.categoryList = FXCollections.observableArrayList();
    }

    /**
     * Método de inicialización del controlador. Se llama automáticamente después de cargar el FXML.
     * Configura la tabla y carga los datos iniciales.
     */
    @FXML
    private void initialize() {
        // Configurar las columnas de la tabla
        colCatId.setCellValueFactory(new PropertyValueFactory<>("idCategoriaProducto"));
        colCatName.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCatActive.setCellValueFactory(new PropertyValueFactory<>("activo"));

        categoriesTable.setItems(categoryList); // Vincular la lista observable a la tabla

        // Cargar datos iniciales
        loadCategories();

        // Configurar listener para selección de tabla (para edición)
        categoriesTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showCategoryDetails(newValue));

        formMessageLabel.setVisible(false); // Ocultar mensaje al inicio
        setEditDeleteButtonsDisable(true); // Deshabilitar botones de edición/desactivación al inicio
    }

    /**
     * Establece un callback que se ejecutará cuando las categorías se actualicen,
     * para que la vista de productos pueda recargar su ComboBox.
     * @param callback La función a ejecutar.
     */
    public void setOnCategoriesUpdatedCallback(Runnable callback) {
        this.onCategoriesUpdatedCallback = callback;
    }

    /**
     * Carga todas las categorías de producto de la base de datos y actualiza la tabla.
     */
    private void loadCategories() {
        categoryList.clear();
        categoryList.addAll(categoriaProductoService.obtenerTodasCategoriasProducto());
        categoriesTable.refresh();
    }

    /**
     * Muestra los detalles de la categoría seleccionada en el formulario para edición.
     * @param categoria La categoría seleccionada en la tabla.
     */
    private void showCategoryDetails(CategoriaProducto categoria) {
        if (categoria != null) {
            categoryIdField.setText(String.valueOf(categoria.getIdCategoriaProducto()));
            categoryNameField.setText(categoria.getNombre());
            formMessageLabel.setVisible(false);
            setEditDeleteButtonsDisable(false); // Habilitar botones de edición/desactivación
        } else {
            handleClearForm(null); // Limpiar el formulario si no hay selección
        }
    }

    /**
     * Habilita o deshabilita los botones de desactivar/activar/guardar según la selección de la tabla.
     * @param disable true para deshabilitar, false para habilitar.
     */
    private void setEditDeleteButtonsDisable(boolean disable) {
        saveCategoryButton.setDisable(false); // Siempre se puede guardar (crear o actualizar)
        deactivateCategoryButton.setDisable(disable);
        activateCategoryButton.setDisable(disable);
    }

    /**
     * Maneja la acción del botón "Guardar".
     * Puede crear una nueva categoría o actualizar una existente.
     * @param event El evento de acción.
     */
    @FXML
    private void handleSaveCategory(ActionEvent event) {
        formMessageLabel.setVisible(false);
        String nombre = categoryNameField.getText();

        if (nombre.isEmpty()) {
            formMessageLabel.setText("El nombre de la categoría es obligatorio.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            formMessageLabel.setVisible(true);
            return;
        }

        String categoryIdText = categoryIdField.getText();
        if (categoryIdText == null || categoryIdText.isEmpty() || categoryIdText.equals("Automático")) {
            // Es una nueva categoría
            boolean success = categoriaProductoService.crearCategoriaProducto(nombre);
            if (success) {
                formMessageLabel.setText("Categoría creada exitosamente.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            } else {
                formMessageLabel.setText("Error al crear categoría. Revise si el nombre ya existe.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
        } else {
            // Es una actualización
            try {
                int idCategoria = Integer.parseInt(categoryIdText);
                // Obtener la categoría existente para mantener su estado activo/inactivo si no se cambia explícitamente
                CategoriaProducto existingCategory = categoriaProductoService.obtenerCategoriaProductoPorId(idCategoria);
                if (existingCategory != null) {
                    existingCategory.setNombre(nombre);
                    boolean success = categoriaProductoService.actualizarCategoriaProducto(existingCategory);
                    if (success) {
                        formMessageLabel.setText("Categoría actualizada exitosamente.");
                        formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                    } else {
                        formMessageLabel.setText("Error al actualizar categoría. Revise los datos o si el nombre ya existe.");
                        formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                    }
                } else {
                    formMessageLabel.setText("Error: Categoría con ID " + idCategoria + " no encontrada para actualizar.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                }
            } catch (NumberFormatException e) {
                formMessageLabel.setText("ID de categoría inválido para actualizar.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
        }
        formMessageLabel.setVisible(true);
        loadCategories(); // Recargar la tabla
        if (onCategoriesUpdatedCallback != null) { // Notificar a la vista de productos
            onCategoriesUpdatedCallback.run();
        }
        handleClearForm(null); // Limpiar formulario después de guardar/actualizar
    }

    /**
     * Maneja la acción del botón "Limpiar". Limpia el formulario.
     * @param event El evento de acción.
     */
    @FXML
    private void handleClearForm(ActionEvent event) {
        categoryIdField.clear();
        categoryNameField.clear();
        formMessageLabel.setVisible(false);
        formMessageLabel.setText("");
        categoriesTable.getSelectionModel().clearSelection();
        setEditDeleteButtonsDisable(true); // Deshabilitar botones de edición/desactivación
    }

    /**
     * Maneja la acción del botón "Desactivar". Desactiva lógicamente la categoría seleccionada.
     * @param event El evento de acción.
     */
    @FXML
    private void handleDeactivateCategory(ActionEvent event) {
        CategoriaProducto selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Desactivación");
            alert.setHeaderText("Desactivar Categoría: " + selectedCategory.getNombre());
            alert.setContentText("¿Está seguro de que desea desactivar esta categoría? " +
                    "Los productos asociados a ella seguirán existiendo, pero no podrá crear nuevos productos con esta categoría.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = categoriaProductoService.desactivarCategoriaProducto(selectedCategory.getIdCategoriaProducto());
                if (success) {
                    formMessageLabel.setText("Categoría desactivada exitosamente.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                } else {
                    formMessageLabel.setText("Error al desactivar categoría. Asegúrese de que no sea la categoría 'Otros'.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                }
            }
        } else {
            formMessageLabel.setText("Seleccione una categoría para desactivar.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
        }
        formMessageLabel.setVisible(true);
        loadCategories();
        if (onCategoriesUpdatedCallback != null) { // Notificar a la vista de productos
            onCategoriesUpdatedCallback.run();
        }
        handleClearForm(null);
    }

    /**
     * Maneja la acción del botón "Activar". Activa lógicamente la categoría seleccionada.
     * @param event El evento de acción.
     */
    @FXML
    private void handleActivateCategory(ActionEvent event) {
        CategoriaProducto selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Activación");
            alert.setHeaderText("Activar Categoría: " + selectedCategory.getNombre());
            alert.setContentText("¿Está seguro de que desea activar esta categoría? Estará disponible para su selección en productos.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = categoriaProductoService.activarCategoriaProducto(selectedCategory.getIdCategoriaProducto());
                if (success) {
                    formMessageLabel.setText("Categoría activada exitosamente.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                } else {
                    formMessageLabel.setText("Error al activar categoría.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                }
            }
        } else {
            formMessageLabel.setText("Seleccione una categoría para activar.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
        }
        formMessageLabel.setVisible(true);
        loadCategories();
        if (onCategoriesUpdatedCallback != null) { // Notificar a la vista de productos
            onCategoriesUpdatedCallback.run();
        }
        handleClearForm(null);
    }

    /**
     * Maneja la acción del botón "Cerrar Ventana".
     * @param event El evento de acción.
     */
    @FXML
    private void handleCloseWindow(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}