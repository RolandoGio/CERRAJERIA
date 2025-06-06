package com.cerrajeria.app.ui.controllers;

import com.cerrajeria.app.MainApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/**
 * Controlador para el Dashboard del Administrador.
 * Maneja la navegación entre las diferentes secciones de gestión
 * y la lógica para cerrar sesión.
 */
public class AdminDashboardController {

    @FXML
    private StackPane contentArea; // El área donde se cargará el contenido de cada sección

    @FXML
    private void initialize() {
        System.out.println("Dashboard de Administrador inicializado.");
        // Opcional: Cargar la vista de gestión de productos por defecto al iniciar
        // handleProductsSection(null);
    }

    @FXML
    private void handleProductsSection(ActionEvent event) {
        System.out.println("Navegando a Gestión de Productos...");
        loadFXMLIntoContentArea("/ProductsManagementView.fxml");
    }

    @FXML
    private void handleServicesSection(ActionEvent event) {
        System.out.println("Navegando a Gestión de Servicios...");
        loadFXMLIntoContentArea("/ServicesManagementView.fxml");
    }

    @FXML
    private void handleUsersSection(ActionEvent event) {
        System.out.println("Navegando a Gestión de Usuarios...");
        loadFXMLIntoContentArea("/UsersManagementView.fxml");
    }

    @FXML
    private void handleSalesSection(ActionEvent event) {
        System.out.println("Navegando a Registro de Ventas...");
        loadFXMLIntoContentArea("/SalesRegistrationView.fxml");
    }

    @FXML
    private void handleCommissionsSection(ActionEvent event) {
        System.out.println("Navegando a Gestión de Comisiones...");
        loadFXMLIntoContentArea("/CommissionsManagementView.fxml");
    }

    @FXML
    private void handleFinancialSection(ActionEvent event) {
        System.out.println("Navegando a Control Financiero...");
        loadFXMLIntoContentArea("/FinancialControlView.fxml"); // Implementa la vista cuando esté lista
    }

    @FXML
    private void handleReportsSection(ActionEvent event) {
        System.out.println("Navegando a Reportes Globales...");
        loadFXMLIntoContentArea("/ReportsView.fxml"); // Implementa la vista cuando esté lista
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Cerrando sesión...");
        try {
            MainApplication.showLoginScreen();
        } catch (IOException e) {
            System.err.println("Error al cargar la pantalla de Login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFXMLIntoContentArea(String fxmlPath) {
        try {
            Node node = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(node);
            System.out.println("Cargada vista: " + fxmlPath);
        } catch (IOException e) {
            System.err.println("Error al cargar FXML en el área de contenido: " + fxmlPath + " - " + e.getMessage());
            e.printStackTrace();
            Label errorLabel = new Label("Error al cargar la vista. Revise la consola.");
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            contentArea.getChildren().setAll(errorLabel);
        }
    }
}
