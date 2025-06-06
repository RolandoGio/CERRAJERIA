package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.models.CategoriaServicio;
import com.cerrajeria.app.models.*;
import com.cerrajeria.app.services.*;
import com.cerrajeria.app.services.CategoriaServicioService;
import com.cerrajeria.app.services.ServicioService;

import javafx.beans.property.ReadOnlyStringWrapper;
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

/**
 * Controlador para la vista de Gestión de Servicios.
 * Permite al Administrador crear, editar, visualizar, activar y desactivar servicios.
 */
public class ServicesManagementController {

    // --- Componentes de la UI (inyectados desde FXML) ---
    @FXML private TextField serviceIdField;
    @FXML private TextField serviceNameField;
    @FXML private ComboBox<CategoriaServicio> serviceCategoryCombo; // ComboBox de Categorías de Servicio
    @FXML private TextField servicePriceField;
    @FXML private TextArea serviceDescriptionArea;
    @FXML private Label formMessageLabel;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button deactivateButton;
    @FXML private Button activateButton;
    @FXML private TextField searchField;
    @FXML private TableView<Servicio> servicesTable;
    @FXML private TableColumn<Servicio, Integer> colId;
    @FXML private TableColumn<Servicio, String> colName;
    @FXML private TableColumn<Servicio, String> colDescription;
    @FXML private TableColumn<Servicio, String> colCategory; // Para mostrar el nombre de la categoría
    @FXML private TableColumn<Servicio, BigDecimal> colPrice;
    @FXML private TableColumn<Servicio, Boolean> colActive;

    // --- Servicios de negocio ---
    private ServicioService servicioService;
    private CategoriaServicioService categoriaServicioService;
    private ObservableList<Servicio> serviceList; // Lista observable para la tabla
    private ObservableList<CategoriaServicio> categoryServiceList; // Lista observable para el ComboBox

    // --- Constructor ---
    public ServicesManagementController() {
        this.servicioService = new ServicioService();
        this.categoriaServicioService = new CategoriaServicioService();
        this.serviceList = FXCollections.observableArrayList();
        this.categoryServiceList = FXCollections.observableArrayList();
    }

    /**
     * Método de inicialización del controlador. Se llama automáticamente después de cargar el FXML.
     * Configura la tabla, carga los datos iniciales y el ComboBox de categorías.
     */
    @FXML
    private void initialize() {
        // Configurar las columnas de la tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("idServicio"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("activo"));

        // Columna para el nombre de la categoría (similar a Productos)
        colCategory.setCellValueFactory(cellData -> {
            int idCategoria = cellData.getValue().getIdCategoriaServicio();
            CategoriaServicio categoria = categoriaServicioService.obtenerCategoriaServicioPorId(idCategoria);
            return new ReadOnlyStringWrapper(categoria != null ? categoria.getNombre() : "Desconocida");
        });

        servicesTable.setItems(serviceList); // Vincular la lista observable a la tabla

        // Cargar datos iniciales
        loadServices();
        loadCategories();

        // Configurar listener para selección de tabla (para edición)
        servicesTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showServiceDetails(newValue));

        // Asegurarse de que el mensaje de error del formulario esté oculto al inicio
        formMessageLabel.setVisible(false);

        // Asegurar que los botones de activar/desactivar estén deshabilitados al inicio
        setEditDeleteButtonsDisable(true);

        // Validador de entrada para el precio (solo números y un punto decimal)
        servicePriceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                servicePriceField.setText(oldVal);
            }
        });
    }

    /**
     * Carga todos los servicios de la base de datos y actualiza la tabla.
     */
    private void loadServices() {
        serviceList.clear();
        serviceList.addAll(servicioService.obtenerTodosLosServicios());
        servicesTable.refresh(); // Asegura que la tabla se redibuje
    }

    /**
     * Carga todas las categorías de servicio activas para el ComboBox.
     */
    private void loadCategories() {
        categoryServiceList.clear();
        categoryServiceList.addAll(categoriaServicioService.obtenerCategoriasServicioActivas());
        serviceCategoryCombo.setItems(categoryServiceList);
        // Configurar cómo se muestra el objeto CategoriaServicio en el ComboBox
        serviceCategoryCombo.setConverter(new StringConverter<CategoriaServicio>() {
            @Override
            public String toString(CategoriaServicio categoria) {
                return categoria != null ? categoria.getNombre() : "";
            }

            @Override
            public CategoriaServicio fromString(String string) {
                return null; // No necesitamos convertir de String a CategoriaServicio para este caso de uso
            }
        });
    }

    /**
     * Muestra los detalles del servicio seleccionado en el formulario para edición.
     * @param servicio El servicio seleccionado en la tabla.
     */
    private void showServiceDetails(Servicio servicio) {
        if (servicio != null) {
            serviceIdField.setText(String.valueOf(servicio.getIdServicio()));
            serviceNameField.setText(servicio.getNombre());
            serviceDescriptionArea.setText(servicio.getDescripcion());
            // Seleccionar la categoría correcta en el ComboBox
            CategoriaServicio categoriaSeleccionada = categoriaServicioService.obtenerCategoriaServicioPorId(servicio.getIdCategoriaServicio());
            serviceCategoryCombo.getSelectionModel().select(categoriaSeleccionada);
            servicePriceField.setText(String.valueOf(servicio.getPrecio()));
            formMessageLabel.setVisible(false); // Ocultar mensaje al seleccionar
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
        saveButton.setDisable(false); // Siempre se puede guardar (crear o actualizar)
        deactivateButton.setDisable(disable);
        activateButton.setDisable(disable);
    }

    /**
     * Maneja la acción del botón "Guardar".
     * Puede crear un nuevo servicio o actualizar uno existente.
     * @param event El evento de acción.
     */
    @FXML
    private void handleSaveService(ActionEvent event) {
        formMessageLabel.setVisible(false); // Ocultar mensaje previo

        // Validar entradas
        String nombre = serviceNameField.getText();
        String descripcion = serviceDescriptionArea.getText();
        CategoriaServicio categoria = serviceCategoryCombo.getSelectionModel().getSelectedItem();
        String precioStr = servicePriceField.getText();

        if (nombre.isEmpty() || descripcion.isEmpty() || categoria == null || precioStr.isEmpty()) {
            formMessageLabel.setText("Todos los campos son obligatorios.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            formMessageLabel.setVisible(true);
            return;
        }

        BigDecimal precio;
        try {
            precio = new BigDecimal(precioStr);
            if (precio.compareTo(BigDecimal.ZERO) < 0) {
                throw new NumberFormatException("El precio debe ser positivo.");
            }
        } catch (NumberFormatException e) {
            formMessageLabel.setText("Asegúrate de que el Precio sea un número válido y positivo.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            formMessageLabel.setVisible(true);
            return;
        }

        // Determinar si es una creación o una actualización
        String serviceIdText = serviceIdField.getText();
        if (serviceIdText == null || serviceIdText.isEmpty() || serviceIdText.equals("Automático")) {
            // Es un nuevo servicio
            boolean success = servicioService.crearServicio(nombre, descripcion, precio, categoria.getIdCategoriaServicio());
            if (success) {
                formMessageLabel.setText("Servicio creado exitosamente.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                handleClearForm(null); // Limpiar formulario
                loadServices(); // Recargar tabla
            } else {
                formMessageLabel.setText("Error al crear servicio. Revise los datos.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
        } else {
            // Es una actualización de un servicio existente
            try {
                int idServicio = Integer.parseInt(serviceIdText);
                Servicio servicioExistente = servicioService.obtenerServicioPorId(idServicio); // Obtener el servicio existente

                if (servicioExistente != null) {
                    // Actualizar solo los campos que vienen del formulario
                    servicioExistente.setNombre(nombre);
                    servicioExistente.setDescripcion(descripcion);
                    servicioExistente.setIdCategoriaServicio(categoria.getIdCategoriaServicio());
                    servicioExistente.setPrecio(precio);
                    // El campo 'activo' se gestiona mediante botones específicos (activar/desactivar)
                    // y no se sobrescribe directamente desde el formulario de edición general.

                    boolean success = servicioService.actualizarServicio(servicioExistente);
                    if (success) {
                        formMessageLabel.setText("Servicio actualizado exitosamente.");
                        formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                        handleClearForm(null); // Limpiar formulario
                        loadServices(); // Recargar tabla
                    } else {
                        formMessageLabel.setText("Error al actualizar servicio. Revise los datos.");
                        formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                    }
                } else {
                    formMessageLabel.setText("Error: Servicio con ID " + idServicio + " no encontrado para actualizar.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                }
            } catch (NumberFormatException e) {
                formMessageLabel.setText("ID de servicio inválido para actualizar.");
                formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
        }
        formMessageLabel.setVisible(true); // Mostrar el mensaje
    }

    /**
     * Maneja la acción del botón "Limpiar". Limpia el formulario.
     * @param event El evento de acción.
     */
    @FXML
    private void handleClearForm(ActionEvent event) {
        serviceIdField.clear();
        serviceNameField.clear();
        serviceDescriptionArea.clear();
        serviceCategoryCombo.getSelectionModel().clearSelection();
        servicePriceField.clear();
        formMessageLabel.setVisible(false);
        formMessageLabel.setText("");
        servicesTable.getSelectionModel().clearSelection(); // Desseleccionar la tabla
        setEditDeleteButtonsDisable(true); // Deshabilitar botones de edición/desactivación
    }

    /**
     * Maneja la acción del botón "Desactivar". Desactiva lógicamente el servicio seleccionado.
     * @param event El evento de acción.
     */
    @FXML
    private void handleDeactivateService(ActionEvent event) {
        Servicio selectedService = servicesTable.getSelectionModel().getSelectedItem();
        if (selectedService != null) {
            // Confirmación para el usuario
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Desactivación");
            alert.setHeaderText("Desactivar Servicio: " + selectedService.getNombre());
            alert.setContentText("¿Está seguro de que desea desactivar este servicio? No estará disponible para su selección en ventas.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = servicioService.desactivarServicio(selectedService.getIdServicio());
                if (success) {
                    formMessageLabel.setText("Servicio desactivado exitosamente.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                    loadServices();
                    handleClearForm(null);
                } else {
                    formMessageLabel.setText("Error al desactivar servicio. " +
                            "Asegúrese de que no sea el servicio 'Otros' o que no haya referencias activas.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                }
            }
        } else {
            formMessageLabel.setText("Seleccione un servicio para desactivar.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
        }
        formMessageLabel.setVisible(true);
    }

    /**
     * Maneja la acción del botón "Activar". Activa lógicamente el servicio seleccionado.
     * @param event El evento de acción.
     */
    @FXML
    private void handleActivateService(ActionEvent event) {
        Servicio selectedService = servicesTable.getSelectionModel().getSelectedItem();
        if (selectedService != null) {
            // Confirmación para el usuario
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Activación");
            alert.setHeaderText("Activar Servicio: " + selectedService.getNombre());
            alert.setContentText("¿Está seguro de que desea activar este servicio? Estará disponible para su selección en ventas.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = servicioService.activarServicio(selectedService.getIdServicio());
                if (success) {
                    formMessageLabel.setText("Servicio activado exitosamente.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                    loadServices();
                    handleClearForm(null);
                } else {
                    formMessageLabel.setText("Error al activar servicio.");
                    formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                }
            }
        } else {
            formMessageLabel.setText("Seleccione un servicio para activar.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
        }
        formMessageLabel.setVisible(true);
    }

    /**
     * Maneja la acción del botón "Buscar". Filtra los servicios en la tabla.
     * @param event El evento de acción.
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadServices(); // Si el campo de búsqueda está vacío, recarga todos los servicios
            return;
        }

        ObservableList<Servicio> filteredList = FXCollections.observableArrayList();
        for (Servicio s : servicioService.obtenerTodosLosServicios()) {
            if (String.valueOf(s.getIdServicio()).contains(searchText) ||
                    s.getNombre().toLowerCase().contains(searchText)) {
                filteredList.add(s);
            }
        }
        serviceList.setAll(filteredList); // Actualiza la lista observable con los resultados filtrados
    }

    /**
     * Maneja la acción del botón "Actualizar Tabla". Recarga todos los servicios.
     * @param event El evento de acción.
     */
    @FXML
    private void refreshTable(ActionEvent event) {
        searchField.clear(); // Limpiar el campo de búsqueda
        loadServices(); // Recargar todos los servicios
        formMessageLabel.setVisible(false); // Ocultar mensaje de formulario
        formMessageLabel.setText("");
        handleClearForm(null); // Limpiar formulario y desseleccionar
    }

    /**
     * Maneja la acción del botón "Gestionar Categorías".
     * Abre una nueva ventana modal para la gestión de categorías de servicio.
     * @param event El evento de acción.
     */
    @FXML
    private void handleManageCategories(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/CategoryServiceManagementView.fxml"));
            Parent parent = fxmlLoader.load();

            // Obtener el controlador de la ventana de categorías de servicio
            CategoryServiceManagementController categoryController = fxmlLoader.getController();
            // Establecer un callback para que, al cerrar la ventana de categorías,
            // se recarguen las categorías en el ComboBox de servicios.
            categoryController.setOnCategoriesUpdatedCallback(this::loadCategories);

            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setTitle("Gestionar Categorías de Servicio");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Hace que la ventana sea modal
            stage.showAndWait(); // Espera a que la ventana se cierre

            // Cuando la ventana de categorías se cierra, aseguramos que el ComboBox se actualice
            loadCategories();

        } catch (IOException e) {
            System.err.println("Error al cargar la ventana de gestión de categorías de servicio: " + e.getMessage());
            e.printStackTrace();
            formMessageLabel.setText("Error al abrir la gestión de categorías de servicio.");
            formMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            formMessageLabel.setVisible(true);
        }
    }
}