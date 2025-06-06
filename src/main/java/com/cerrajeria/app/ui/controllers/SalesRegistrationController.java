package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.models.*;
import com.cerrajeria.app.services.*;
import com.cerrajeria.app.SessionManager;
import com.cerrajeria.app.ui.models.VentaItem;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SalesRegistrationController {

    // --- Componentes de la UI ---
    @FXML private TextField searchItemField;
    @FXML private TableView<Object> searchResultsTable;
    @FXML private TableColumn<Object, Integer> colSearchId;
    @FXML private TableColumn<Object, String> colSearchType;
    @FXML private TableColumn<Object, String> colSearchName;
    @FXML private TableColumn<Object, BigDecimal> colSearchPrice;
    @FXML private TableColumn<Object, Integer> colSearchStock;
    @FXML private TextField quantityField;
    @FXML private TextField sellingPriceField;
    @FXML private Label commentLabel;
    @FXML private TextArea commentArea;
    @FXML private Label itemMessageLabel;

    @FXML private TableView<VentaItem> cartTable;
    @FXML private TableColumn<VentaItem, Integer> colCartId;
    @FXML private TableColumn<VentaItem, String> colCartType;
    @FXML private TableColumn<VentaItem, String> colCartName;
    @FXML private TableColumn<VentaItem, Integer> colCartQuantity;
    @FXML private TableColumn<VentaItem, BigDecimal> colCartPrice;
    @FXML private TableColumn<VentaItem, BigDecimal> colCartSubtotal;
    @FXML private TableColumn<VentaItem, String> colCartComment;
    @FXML private TableColumn<VentaItem, Button> colCartActions;
    @FXML private Label totalLabel;
    @FXML private Label saleMessageLabel;

    // --- Servicios ---
    private final ProductoService productoService = new ProductoService();
    private final ServicioService servicioService = new ServicioService();
    private final VentaService ventaService = new VentaService();

    // --- Datos dinámicos ---
    private final ObservableList<Object> searchResultsList = FXCollections.observableArrayList();
    private final ObservableList<VentaItem> cartItems = FXCollections.observableArrayList();
    private Object selectedItemInSearch;

    @FXML
    private void initialize() {
        // Tabla de resultados de búsqueda
        colSearchId.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Producto p) return new SimpleObjectProperty<>(p.getIdProducto());
            if (cellData.getValue() instanceof Servicio s) return new SimpleObjectProperty<>(s.getIdServicio());
            return null;
        });
        colSearchType.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Producto) return new SimpleObjectProperty<>("Producto");
            if (cellData.getValue() instanceof Servicio) return new SimpleObjectProperty<>("Servicio");
            return null;
        });
        colSearchName.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Producto p) return new SimpleObjectProperty<>(p.getNombre());
            if (cellData.getValue() instanceof Servicio s) return new SimpleObjectProperty<>(s.getNombre());
            return null;
        });
        colSearchPrice.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Producto p) return new SimpleObjectProperty<>(p.getPrecio());
            if (cellData.getValue() instanceof Servicio s) return new SimpleObjectProperty<>(s.getPrecio());
            return null;
        });
        colSearchStock.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Producto p) return new SimpleObjectProperty<>(p.getStock());
            return new SimpleObjectProperty<>(0);
        });
        searchResultsTable.setItems(searchResultsList);

        // Listener para selección y comentarios
        searchResultsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedItemInSearch = newVal;
            itemMessageLabel.setVisible(false);
            commentLabel.setVisible(false);
            commentLabel.setManaged(false);
            commentArea.setVisible(false);
            commentArea.setManaged(false);
            commentArea.clear();
            if (newVal != null) {
                BigDecimal refPrice = (newVal instanceof Producto p) ? p.getPrecio() : ((Servicio) newVal).getPrecio();
                String name = (newVal instanceof Producto p) ? p.getNombre() : ((Servicio) newVal).getNombre();
                sellingPriceField.setText(String.format("%.2f", refPrice));
                if (name.equalsIgnoreCase("Otros")) {
                    commentLabel.setVisible(true);
                    commentLabel.setManaged(true);
                    commentArea.setVisible(true);
                    commentArea.setManaged(true);
                }
            } else {
                sellingPriceField.clear();
            }
        });

        // Tabla de carrito
        colCartId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCartType.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colCartName.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCartQuantity.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCartPrice.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colCartSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colCartComment.setCellValueFactory(new PropertyValueFactory<>("comentario"));
        colCartActions.setCellFactory(param -> new TableCell<>() {
            final Button deleteButton = new Button("X");
            {
                deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px;");
                deleteButton.setOnAction(event -> {
                    VentaItem item = getTableView().getItems().get(getIndex());
                    cartItems.remove(item);
                    calculateTotal();
                    saleMessageLabel.setVisible(false);
                });
            }
            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
        cartTable.setItems(cartItems);

        // Validaciones
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) quantityField.setText(oldVal);
        });
        sellingPriceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) sellingPriceField.setText(oldVal);
        });

        calculateTotal();
        itemMessageLabel.setVisible(false);
        saleMessageLabel.setVisible(false);
    }

    @FXML
    private void handleSearchItem(ActionEvent event) {
        String searchText = searchItemField.getText().toLowerCase().trim();
        searchResultsList.clear();
        itemMessageLabel.setVisible(false);

        if (searchText.isEmpty()) {
            itemMessageLabel.setText("Ingrese un nombre o ID para buscar.");
            itemMessageLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
            itemMessageLabel.setVisible(true);
            return;
        }

        List<Producto> productos = productoService.obtenerTodosLosProductos().stream()
                .filter(p -> p.isActivo() && (String.valueOf(p.getIdProducto()).contains(searchText) || p.getNombre().toLowerCase().contains(searchText)))
                .toList();
        List<Servicio> servicios = servicioService.obtenerTodosLosServicios().stream()
                .filter(s -> s.isActivo() && (String.valueOf(s.getIdServicio()).contains(searchText) || s.getNombre().toLowerCase().contains(searchText)))
                .toList();
        searchResultsList.addAll(productos);
        searchResultsList.addAll(servicios);

        if (searchResultsList.isEmpty()) {
            itemMessageLabel.setText("No se encontraron productos o servicios activos.");
            itemMessageLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
            itemMessageLabel.setVisible(true);
        }
    }

    @FXML
    private void handleAddToCart(ActionEvent event) {
        itemMessageLabel.setVisible(false);
        saleMessageLabel.setVisible(false);
        if (selectedItemInSearch == null) {
            itemMessageLabel.setText("Seleccione un producto o servicio.");
            itemMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            itemMessageLabel.setVisible(true);
            return;
        }
        int cantidad = Integer.parseInt(quantityField.getText().trim());
        BigDecimal precioVenta = new BigDecimal(sellingPriceField.getText().trim());
        String itemComment = commentArea.getText().trim();
        String itemName = (selectedItemInSearch instanceof Producto p) ? p.getNombre() : ((Servicio) selectedItemInSearch).getNombre();
        if (itemName.equalsIgnoreCase("Otros") && itemComment.isEmpty()) {
            itemMessageLabel.setText("El comentario es obligatorio para 'Otros'.");
            itemMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            itemMessageLabel.setVisible(true);
            return;
        }

        if (selectedItemInSearch instanceof Producto producto) {
            Optional<VentaItem> existingItem = cartItems.stream()
                    .filter(i -> i.getId() == producto.getIdProducto() && "Producto".equals(i.getTipo()))
                    .findFirst();
            if (existingItem.isPresent()) {
                VentaItem item = existingItem.get();
                int nuevaCantidad = item.getCantidad() + cantidad;
                if (producto.getStock() < nuevaCantidad) {
                    itemMessageLabel.setText("Stock insuficiente.");
                    itemMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                    itemMessageLabel.setVisible(true);
                    return;
                }
                item.setCantidad(nuevaCantidad);
                item.setPrecioUnitario(precioVenta);
                item.setComentario(itemComment);
            } else {
                if (producto.getStock() < cantidad) {
                    itemMessageLabel.setText("Stock insuficiente.");
                    itemMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                    itemMessageLabel.setVisible(true);
                    return;
                }
                cartItems.add(new VentaItem(producto.getIdProducto(), producto.getNombre(), "Producto", cantidad, precioVenta, itemComment));
            }
        } else if (selectedItemInSearch instanceof Servicio servicio) {
            Optional<VentaItem> existingItem = cartItems.stream()
                    .filter(i -> i.getId() == servicio.getIdServicio() && "Servicio".equals(i.getTipo()))
                    .findFirst();
            if (existingItem.isPresent()) {
                VentaItem item = existingItem.get();
                item.setCantidad(item.getCantidad() + cantidad);
                item.setPrecioUnitario(precioVenta);
                item.setComentario(itemComment);
            } else {
                cartItems.add(new VentaItem(servicio.getIdServicio(), servicio.getNombre(), "Servicio", cantidad, precioVenta, itemComment));
            }
        }

        itemMessageLabel.setText("Añadido al carrito.");
        itemMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
        itemMessageLabel.setVisible(true);
        calculateTotal();
        searchResultsTable.getSelectionModel().clearSelection();
        selectedItemInSearch = null;
        quantityField.setText("1");
        sellingPriceField.clear();
        commentArea.clear();
        commentLabel.setVisible(false);
        commentLabel.setManaged(false);
        commentArea.setVisible(false);
        commentArea.setManaged(false);
    }

    private void calculateTotal() {
        BigDecimal total = cartItems.stream()
                .map(VentaItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalLabel.setText("Total: $" + total);
    }

    @FXML
    private void handleFinalizeSale(ActionEvent event) {
        saleMessageLabel.setVisible(false);
        if (cartItems.isEmpty()) {
            saleMessageLabel.setText("El carrito está vacío.");
            saleMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            saleMessageLabel.setVisible(true);
            return;
        }
        // Obtener usuario logueado desde SessionManager
        Usuario usuarioActual = SessionManager.getUsuarioActual();
        if (usuarioActual == null) {
            saleMessageLabel.setText("Error: No hay usuario logueado.");
            saleMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            saleMessageLabel.setVisible(true);
            return;
        }

        int idUsuarioActual = usuarioActual.getIdUsuario();

        List<DetalleVentaProducto> productosParaVenta = cartItems.stream()
                .filter(i -> "Producto".equals(i.getTipo()))
                .map(i -> {
                    DetalleVentaProducto d = new DetalleVentaProducto();
                    d.setIdProducto(i.getId());
                    d.setCantidad(i.getCantidad());
                    d.setPrecioUnitarioFinal(i.getPrecioUnitario());
                    d.setDescripcion(i.getComentario());
                    return d;
                }).collect(Collectors.toList());
        List<DetalleVentaServicio> serviciosParaVenta = cartItems.stream()
                .filter(i -> "Servicio".equals(i.getTipo()))
                .map(i -> {
                    DetalleVentaServicio d = new DetalleVentaServicio();
                    d.setIdServicio(i.getId());
                    d.setCantidad(i.getCantidad());
                    d.setPrecioUnitarioFinal(i.getPrecioUnitario());
                    d.setDescripcion(i.getComentario());
                    return d;
                }).collect(Collectors.toList());

        Venta venta = ventaService.registrarVenta(idUsuarioActual, productosParaVenta, serviciosParaVenta);
        if (venta != null) {
            saleMessageLabel.setText("Venta finalizada. ID: " + venta.getIdVenta());
            saleMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            handleClearCart(null);
            searchResultsList.clear();
            searchItemField.clear();
        } else {
            saleMessageLabel.setText("Error al finalizar la venta.");
            saleMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
        saleMessageLabel.setVisible(true);
    }

    @FXML
    private void handleClearCart(ActionEvent event) {
        cartItems.clear();
        calculateTotal();
        saleMessageLabel.setVisible(false);
    }
}
