package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.models.DetalleVentaProducto;
import com.cerrajeria.app.models.DetalleVentaServicio;
import com.cerrajeria.app.models.Producto;
import com.cerrajeria.app.models.Servicio;
import com.cerrajeria.app.models.Venta;
import com.cerrajeria.app.services.ProductoService;
import com.cerrajeria.app.services.ServicioService;
import com.cerrajeria.app.services.VentaService;
import com.cerrajeria.app.ui.models.VentaItem;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;

/**
 * Controlador para la vista de Registro de Ventas.
 * Permite al usuario (Administrador/Vendedor) buscar productos y servicios,
 * añadirlos a un carrito, calcular el total y finalizar la venta.
 * Incorpora precio de venta editable y comentario obligatorio para ítems "Otros".
 */
public class SalesRegistrationController {

    // --- Componentes de la UI (inyectados desde FXML) ---
    // Sección de Búsqueda
    @FXML private TextField searchItemField;
    @FXML private TableView<Object> searchResultsTable;
    @FXML private TableColumn<Object, Integer> colSearchId;
    @FXML private TableColumn<Object, String> colSearchType;
    @FXML private TableColumn<Object, String> colSearchName;
    @FXML private TableColumn<Object, BigDecimal> colSearchPrice;
    @FXML private TableColumn<Object, Integer> colSearchStock;
    @FXML private TextField quantityField;
    @FXML private TextField sellingPriceField; // Nuevo campo: Precio de venta editable
    @FXML private Label commentLabel; // Etiqueta para comentario
    @FXML private TextArea commentArea; // Área de texto para comentario
    @FXML private Label itemMessageLabel;

    // Sección del Carrito
    @FXML private TableView<VentaItem> cartTable;
    @FXML private TableColumn<VentaItem, Integer> colCartId;
    @FXML private TableColumn<VentaItem, String> colCartType;
    @FXML private TableColumn<VentaItem, String> colCartName;
    @FXML private TableColumn<VentaItem, Integer> colCartQuantity;
    @FXML private TableColumn<VentaItem, BigDecimal> colCartPrice;
    @FXML private TableColumn<VentaItem, BigDecimal> colCartSubtotal;
    @FXML private TableColumn<VentaItem, String> colCartComment; // Nueva columna para comentario
    @FXML private TableColumn<VentaItem, Button> colCartActions;
    @FXML private Label totalLabel;
    @FXML private Label saleMessageLabel;

    // --- Servicios de negocio ---
    private ProductoService productoService;
    private ServicioService servicioService;
    private VentaService ventaService; // Ahora se usará para la persistencia

    // --- Listas de datos para la UI ---
    private ObservableList<Object> searchResultsList;
    private ObservableList<VentaItem> cartItems;

    // --- Atributo para almacenar el ítem seleccionado en la tabla de búsqueda ---
    private Object selectedItemInSearch;

    // --- Constructor ---
    public SalesRegistrationController() {
        this.productoService = new ProductoService();
        this.servicioService = new ServicioService();
        this.ventaService = new VentaService(); // Inicializar VentaService
        this.searchResultsList = FXCollections.observableArrayList();
        this.cartItems = FXCollections.observableArrayList();
    }

    /**
     * Método de inicialización del controlador. Se llama automáticamente después de cargar el FXML.
     * Configura las tablas y listeners.
     */
    @FXML
    private void initialize() {
        // Configurar columnas de la tabla de resultados de búsqueda
        colSearchId.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Producto) {
                return new SimpleObjectProperty<>(((Producto) cellData.getValue()).getIdProducto());
            } else if (cellData.getValue() instanceof Servicio) {
                return new SimpleObjectProperty<>(((Servicio) cellData.getValue()).getIdServicio());
            }
            return null;
        });
        colSearchType.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Producto) {
                return new SimpleObjectProperty<>("Producto");
            } else if (cellData.getValue() instanceof Servicio) {
                return new SimpleObjectProperty<>("Servicio");
            }
            return null;
        });
        colSearchName.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Producto) {
                return new SimpleObjectProperty<>(((Producto) cellData.getValue()).getNombre());
            } else if (cellData.getValue() instanceof Servicio) {
                return new SimpleObjectProperty<>(((Servicio) cellData.getValue()).getNombre());
            }
            return null;
        });
        colSearchPrice.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Producto) {
                return new SimpleObjectProperty<>(((Producto) cellData.getValue()).getPrecio());
            } else if (cellData.getValue() instanceof Servicio) {
                return new SimpleObjectProperty<>(((Servicio) cellData.getValue()).getPrecio());
            }
            return null;
        });
        colSearchStock.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Producto) {
                return new SimpleObjectProperty<>(((Producto) cellData.getValue()).getStock());
            }
            return new SimpleObjectProperty<>(0); // Para servicios no aplica stock
        });
        searchResultsTable.setItems(searchResultsList);

        // Listener para selección en la tabla de búsqueda: rellena el precio de venta y gestiona el comentario
        searchResultsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedItemInSearch = newValue; // Almacena el ítem seleccionado
                    itemMessageLabel.setVisible(false); // Oculta mensajes al seleccionar nuevo ítem

                    // Resetear visibilidad del campo de comentario
                    commentLabel.setVisible(false);
                    commentLabel.setManaged(false);
                    commentArea.setVisible(false);
                    commentArea.setManaged(false);
                    commentArea.clear(); // Limpiar el campo de comentario

                    if (newValue != null) {
                        BigDecimal refPrice = BigDecimal.ZERO;
                        String itemName = "";
                        if (newValue instanceof Producto) {
                            Producto p = (Producto) newValue;
                            refPrice = p.getPrecio();
                            itemName = p.getNombre();
                        } else if (newValue instanceof Servicio) {
                            Servicio s = (Servicio) newValue;
                            refPrice = s.getPrecio();
                            itemName = s.getNombre();
                        }
                        sellingPriceField.setText(String.format("%.2f", refPrice)); // Rellenar precio de venta con el de referencia

                        // Mostrar el área de comentario si el nombre es "Otros" (insensible a mayúsculas/minúsculas)
                        if (itemName.equalsIgnoreCase("Otros")) {
                            commentLabel.setVisible(true);
                            commentLabel.setManaged(true);
                            commentArea.setVisible(true);
                            commentArea.setManaged(true);
                        }
                    } else {
                        sellingPriceField.clear(); // Limpiar precio si no hay selección
                    }
                }
        );

        // Configurar columnas de la tabla del carrito
        colCartId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCartType.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colCartName.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCartQuantity.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCartPrice.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colCartSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colCartComment.setCellValueFactory(new PropertyValueFactory<>("comentario")); // Nueva columna para comentario

        // Columna de acciones (eliminar)
        colCartActions.setCellFactory(param -> new TableCell<VentaItem, Button>() {
            final Button deleteButton = new Button("X"); // O usa un icono
            {
                deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px;");
                deleteButton.setOnAction(event -> {
                    VentaItem item = getTableView().getItems().get(getIndex());
                    cartItems.remove(item);
                    calculateTotal(); // Recalcular total al eliminar
                    saleMessageLabel.setVisible(false); // Ocultar mensaje de venta
                });
            }
            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        cartTable.setItems(cartItems); // Vincular la lista observable al carrito

        // Validar que la cantidad sea solo números
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                quantityField.setText(oldVal);
            }
        });
        // Validar que el precio de venta sea un número decimal válido
        sellingPriceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                sellingPriceField.setText(oldVal);
            }
        });

        calculateTotal(); // Calcular total inicial (debería ser 0)
        itemMessageLabel.setVisible(false);
        saleMessageLabel.setVisible(false);
    }

    /**
     * Maneja la acción del botón "Buscar" en la sección de búsqueda de ítems.
     * Busca productos y servicios por nombre o ID.
     * @param event El evento de acción.
     */
    @FXML
    private void handleSearchItem(ActionEvent event) {
        String searchText = searchItemField.getText().toLowerCase().trim();
        searchResultsList.clear(); // Limpiar resultados anteriores
        itemMessageLabel.setVisible(false);

        if (searchText.isEmpty()) {
            itemMessageLabel.setText("Ingrese un nombre o ID para buscar.");
            itemMessageLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
            itemMessageLabel.setVisible(true);
            return;
        }

        // Buscar en productos activos
        List<Producto> matchingProducts = productoService.obtenerTodosLosProductos().stream()
                .filter(p -> p.isActivo() && (String.valueOf(p.getIdProducto()).contains(searchText) ||
                        p.getNombre().toLowerCase().contains(searchText)))
                .collect(Collectors.toList());
        searchResultsList.addAll(matchingProducts);

        // Buscar en servicios activos
        List<Servicio> matchingServices = servicioService.obtenerTodosLosServicios().stream()
                .filter(s -> s.isActivo() && (String.valueOf(s.getIdServicio()).contains(searchText) ||
                        s.getNombre().toLowerCase().contains(searchText)))
                .collect(Collectors.toList());
        searchResultsList.addAll(matchingServices);

        if (searchResultsList.isEmpty()) {
            itemMessageLabel.setText("No se encontraron productos o servicios activos.");
            itemMessageLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
            itemMessageLabel.setVisible(true);
        }
    }

    /**
     * Maneja la acción del botón "Añadir al Carrito".
     * Añade el ítem seleccionado en la tabla de búsqueda al carrito,
     * utilizando el precio de venta y validando el comentario para "Otros".
     * @param event El evento de acción.
     */
    @FXML
    private void handleAddToCart(ActionEvent event) {
        itemMessageLabel.setVisible(false);
        saleMessageLabel.setVisible(false);

        if (selectedItemInSearch == null) {
            itemMessageLabel.setText("Seleccione un producto o servicio de la tabla de búsqueda.");
            itemMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            itemMessageLabel.setVisible(true);
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(quantityField.getText().trim());
            if (cantidad <= 0) {
                throw new NumberFormatException("La cantidad debe ser mayor que cero.");
            }
        } catch (NumberFormatException e) {
            itemMessageLabel.setText("Ingrese una cantidad válida (número entero positivo).");
            itemMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            itemMessageLabel.setVisible(true);
            return;
        }

        BigDecimal precioVenta;
        try {
            precioVenta = new BigDecimal(sellingPriceField.getText().trim());
            if (precioVenta.compareTo(BigDecimal.ZERO) < 0) {
                throw new NumberFormatException("El precio de venta no puede ser negativo.");
            }
        } catch (NumberFormatException e) {
            itemMessageLabel.setText("Ingrese un precio de venta válido.");
            itemMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            itemMessageLabel.setVisible(true);
            return;
        }

        String itemComment = commentArea.getText().trim();
        String itemName = "";
        if (selectedItemInSearch instanceof Producto) {
            itemName = ((Producto) selectedItemInSearch).getNombre();
        } else if (selectedItemInSearch instanceof Servicio) {
            itemName = ((Servicio) selectedItemInSearch).getNombre();
        }

        // Validación de comentario obligatorio para "Otros"
        if (itemName.equalsIgnoreCase("Otros") && itemComment.isEmpty()) {
            itemMessageLabel.setText("El comentario es obligatorio para ítems 'Otros'.");
            itemMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            itemMessageLabel.setVisible(true);
            return;
        }


        // Lógica para añadir o actualizar el ítem en el carrito
        if (selectedItemInSearch instanceof Producto) {
            Producto producto = (Producto) selectedItemInSearch;

            // Verificar si el producto ya está en el carrito para actualizar la cantidad
            Optional<VentaItem> existingItem = cartItems.stream()
                    .filter(item -> item.getId() == producto.getIdProducto() && "Producto".equals(item.getTipo()))
                    .findFirst();

            if (existingItem.isPresent()) {
                VentaItem item = existingItem.get();
                int nuevaCantidad = item.getCantidad() + cantidad;
                // Re-validar stock con la nueva cantidad total
                if (producto.getStock() < nuevaCantidad) {
                    itemMessageLabel.setText("Stock insuficiente. No se pueden añadir " + cantidad + " más unidades de " + producto.getNombre() + ". Stock disponible: " + producto.getStock() + " (ya tienes " + item.getCantidad() + " en carrito).");
                    itemMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                    itemMessageLabel.setVisible(true);
                    return;
                }
                item.setCantidad(nuevaCantidad); // Actualiza la cantidad y el subtotal
                item.setPrecioUnitario(precioVenta); // Actualiza también el precio por si se cambió
                item.setComentario(itemComment); // Actualiza el comentario
            } else {
                // Si no existe, añadir nuevo VentaItem
                if (producto.getStock() < cantidad) { // Validar stock para el nuevo ítem
                    itemMessageLabel.setText("Stock insuficiente para el producto: " + producto.getNombre() + ". Stock disponible: " + producto.getStock());
                    itemMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                    itemMessageLabel.setVisible(true);
                    return;
                }
                cartItems.add(new VentaItem(producto.getIdProducto(), producto.getNombre(), "Producto", cantidad, precioVenta, itemComment));
            }

            itemMessageLabel.setText("Producto '" + producto.getNombre() + "' añadido x" + cantidad + ".");
            itemMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);

        } else if (selectedItemInSearch instanceof Servicio) {
            Servicio servicio = (Servicio) selectedItemInSearch;

            // Verificar si el servicio ya está en el carrito para actualizar la cantidad
            Optional<VentaItem> existingItem = cartItems.stream()
                    .filter(item -> item.getId() == servicio.getIdServicio() && "Servicio".equals(item.getTipo()))
                    .findFirst();

            if (existingItem.isPresent()) {
                VentaItem item = existingItem.get();
                item.setCantidad(item.getCantidad() + cantidad);
                item.setPrecioUnitario(precioVenta);
                item.setComentario(itemComment);
            } else {
                cartItems.add(new VentaItem(servicio.getIdServicio(), servicio.getNombre(), "Servicio", cantidad, precioVenta, itemComment));
            }

            itemMessageLabel.setText("Servicio '" + servicio.getNombre() + "' añadido x" + cantidad + ".");
            itemMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
        }

        itemMessageLabel.setVisible(true);
        calculateTotal(); // Recalcular total al añadir ítem
        searchResultsTable.getSelectionModel().clearSelection(); // Limpiar selección de búsqueda
        selectedItemInSearch = null; // Reiniciar ítem seleccionado
        quantityField.setText("1"); // Restablecer cantidad a 1
        sellingPriceField.clear(); // Limpiar precio de venta
        commentArea.clear(); // Limpiar comentario
        commentLabel.setVisible(false); // Ocultar comentario
        commentLabel.setManaged(false);
        commentArea.setVisible(false);
        commentArea.setManaged(false);
    }

    /**
     * Calcula y actualiza el total de la venta en el carrito.
     */
    private void calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (VentaItem item : cartItems) {
            total = total.add(item.getSubtotal());
        }
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    /**
     * Maneja la acción del botón "Finalizar Venta".
     * Llama al VentaService para persistir la venta y sus detalles en la DB.
     * @param event El evento de acción.
     */
    @FXML
    private void handleFinalizeSale(ActionEvent event) {
        saleMessageLabel.setVisible(false);
        if (cartItems.isEmpty()) {
            saleMessageLabel.setText("El carrito está vacío. No hay nada que vender.");
            saleMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            saleMessageLabel.setVisible(true);
            return;
        }

        // --- Lógica de persistencia real en DB ---
        // TODO: Obtener el ID del usuario real logueado. Para la demostración, usamos 1.
        int idUsuarioActual = 1;

        // Convertir VentaItem a DetalleVentaProducto y DetalleVentaServicio
        List<DetalleVentaProducto> productosParaVenta = cartItems.stream()
                .filter(item -> "Producto".equals(item.getTipo()))
                .map(item -> {
                    // Se requiere un constructor sin argumentos (por defecto) en DetalleVentaProducto
                    DetalleVentaProducto dvp = new DetalleVentaProducto();
                    dvp.setIdProducto(item.getId());
                    dvp.setCantidad(item.getCantidad());
                    dvp.setPrecioUnitarioFinal(item.getPrecioUnitario());
                    dvp.setDescripcion(item.getComentario()); // Usar el comentario del VentaItem
                    return dvp;
                })
                .collect(Collectors.toList());

        List<DetalleVentaServicio> serviciosParaVenta = cartItems.stream()
                .filter(item -> "Servicio".equals(item.getTipo()))
                .map(item -> {
                    // Se requiere un constructor sin argumentos (por defecto) en DetalleVentaServicio
                    DetalleVentaServicio dvs = new DetalleVentaServicio();
                    dvs.setIdServicio(item.getId());
                    dvs.setCantidad(item.getCantidad());
                    dvs.setPrecioUnitarioFinal(item.getPrecioUnitario());
                    dvs.setDescripcion(item.getComentario()); // Usar el comentario del VentaItem
                    return dvs;
                })
                .collect(Collectors.toList());

        // Llamar al servicio para registrar la venta
        Venta ventaRegistrada = ventaService.registrarVenta(idUsuarioActual, productosParaVenta, serviciosParaVenta);

        if (ventaRegistrada != null) {
            saleMessageLabel.setText("Venta finalizada exitosamente. ID de Venta: " + ventaRegistrada.getIdVenta());
            saleMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            saleMessageLabel.setVisible(true);

            // Limpiar el carrito y la tabla de búsqueda después de la venta exitosa
            handleClearCart(null);
            searchResultsList.clear();
            searchItemField.clear();
            // Restablecer el mensaje del carrito a la visibilidad por defecto
            itemMessageLabel.setVisible(false);
            // Asegurarse de que el total se muestre como $0.00
            calculateTotal();

        } else {
            saleMessageLabel.setText("Error al finalizar la venta. Verifique los detalles e intente de nuevo.");
            saleMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            saleMessageLabel.setVisible(true);
        }
    }

    /**
     * Maneja la acción del botón "Limpiar Carrito".
     * @param event El evento de acción.
     */
    @FXML
    private void handleClearCart(ActionEvent event) {
        cartItems.clear();
        calculateTotal();
        saleMessageLabel.setVisible(false);
    }
}
