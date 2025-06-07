package com.cerrajeria.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image; // Importación necesaria para Image

import java.io.IOException;

/**
 * Clase principal de la aplicación JavaFX.
 * Es el punto de entrada para la interfaz de usuario.
 * Se encarga de cargar las diferentes vistas (escenas) de la aplicación.
 */
public class MainApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage; // Guardar la referencia al Stage principal.

        // --- Configuración del Título de la Ventana ---
        // Puedes cambiar este texto al nombre que desees para la ventana principal.
        primaryStage.setTitle("Sistema de Gestión 7LLAVES ADP"); // Título actualizado

        // --- Configuración del Icono de la Aplicación ---
        // Asegúrate de que "app_icon.png" esté en la carpeta src/main/resources.
        try {
            // Se carga la imagen desde el classpath, que incluye la carpeta 'resources'.
            // El '/' al inicio indica la raíz del classpath.
            Image appIcon = new Image(MainApplication.class.getResourceAsStream("/app_icon.png"));
            primaryStage.getIcons().add(appIcon);
        } catch (Exception e) {
            System.err.println("Error al cargar el icono de la aplicación: " + e.getMessage());
            // No es un error crítico si el icono no se carga, la aplicación seguirá funcionando.
        }


        // Cargar la escena de Login inicialmente al iniciar la aplicación.
        showLoginScreen();
    }

    /**
     * Muestra la pantalla de Login.
     * Carga el archivo FXML correspondiente y lo establece como la escena principal.
     * @throws IOException Si el archivo FXML no se puede cargar (ruta incorrecta o error en el FXML).
     */
    public static void showLoginScreen() throws IOException {
        // La ruta del FXML ahora es relativa a la raíz de la carpeta 'resources'.
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/LoginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle("7LLAVES  - Inicio de Sesión"); // Opcional: un título específico para el login
        primaryStage.show();
        primaryStage.centerOnScreen();
    }

    /**
     * Muestra el Dashboard del Administrador.
     * Ahora carga el archivo FXML real del Admin Dashboard.
     * @throws IOException Si el archivo FXML no se puede cargar.
     */
    public static void showAdminDashboard() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/AdminDashboardView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        //primaryStage.setTitle("7LLAVES ADP - Dashboard Administrador"); // Opcional: un título específico para el dashboard
        primaryStage.show();
        primaryStage.centerOnScreen();
        System.out.println("Navegando al Dashboard del Administrador...");
    }

    /**
     * Método placeholder para mostrar el Dashboard del Vendedor.
     * En el futuro, cargará el FXML del dashboard del vendedor.
     * @throws IOException Si el archivo FXML no se puede cargar.
     * Muestra el dashboard para usuarios con rol Vendedor.
     */
    public static void showVendedorDashboard() throws IOException {
        System.out.println("Navegando al Dashboard del Vendedor... (Falta implementación de la vista)");
        //Cuando implementes esta vista, el FXML podría estar en:
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/VendedorDashboardView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);

        primaryStage.show();
        primaryStage.centerOnScreen();
        primaryStage.centerOnScreen();
        System.out.println("Dashboard de Vendedor cargado");
    }

    /**
     * Punto de entrada principal para la aplicación Java.
     * Llama al método launch() de JavaFX para iniciar la interfaz gráfica.
     * @param args Argumentos de la línea de comandos.
     */
    public static void main(String[] args) {
        launch(); // Inicia la aplicación JavaFX.
    }
}
