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
 * Controlador para el Dashboard del Vendedor.
 * Maneja la navegación básica y el cierre de sesión.
 */
public class VendedorDashboardController {

    @FXML
    private StackPane contentArea; // Área central para las vistas

    @FXML
    private void initialize() {
        System.out.println("Dashboard de Vendedor inicializado.");
    }

    @FXML
    private void handleSalesSection(ActionEvent event) {
        System.out.println("Navegando a Registro de Ventas...");
        loadFXMLIntoContentArea("/SalesRegistrationView.fxml");
    }

    @FXML
    private void handleCommissionsSection(ActionEvent event) {
        System.out.println("Sección de comisiones seleccionada. (TODO: implementar vista)");
        // TODO: load commissions view when available
    }

    @FXML
    private void handleReportsSection(ActionEvent event) {
        System.out.println("Sección de reportes seleccionada. (TODO: implementar vista)");
        // TODO: load reports view when available
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