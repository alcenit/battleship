package com.cenit.battleship;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * JavaFX App - Battleship Game
 * Aplicación principal del juego Battleship
 */
public class App extends Application {
    private static Scene scene;
    private static Stage primaryStage;
    
    // Constantes de configuración
    private static final String APP_TITLE = "Battleship - Edición Especial";
    private static final double MIN_WIDTH = 1000;
    private static final double MIN_HEIGHT = 700;
    private static final double DEFAULT_WIDTH = 1200;
    private static final double DEFAULT_HEIGHT = 800;
    private static final String DEFAULT_VIEW = "com/cenit/battleship/view/MainView";
    private static final String CSS_PATH = "/com/cenit/battleship/styles/mainview.css";

    @Override
    public void start(Stage stage) throws IOException {
        try {
            primaryStage = stage;
            
            // Cargar la vista principal
            Parent root = loadFXML(DEFAULT_VIEW);
            
            // Crear escena principal
            scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            
            // Configurar CSS
            setupStylesheets();
            
            // Configurar el stage principal
            setupPrimaryStage(stage);
            
            // Mostrar la ventana
            stage.show();
            
            System.out.println("🚀 Aplicación Battleship iniciada correctamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Error crítico", "No se pudo iniciar la aplicación: " + e.getMessage());
        }
    }

    /**
     * Configura los estilos CSS de la aplicación
     */
    private void setupStylesheets() {
        try {
            String cssResource = getClass().getResource(CSS_PATH).toExternalForm();
            scene.getStylesheets().add(cssResource);
            System.out.println("✅ CSS cargado: " + CSS_PATH);
        } catch (Exception e) {
            System.err.println("⚠️  No se pudo cargar el CSS: " + CSS_PATH);
            // La aplicación puede funcionar sin CSS
        }
    }

    /**
     * Configura el stage principal de la aplicación
     */
    private void setupPrimaryStage(Stage stage) {
        // Configuración básica
        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        
        // Configurar icono de la aplicación
        setupAppIcon(stage);
        
        // Configurar restricciones de tamaño
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        
        // Configurar evento de cierre
        stage.setOnCloseRequest(event -> {
            System.out.println("👋 Cerrando aplicación Battleship...");
            // Aquí puedes agregar lógica para guardar el estado del juego
        });
        
        // Centrar la ventana en la pantalla
        stage.centerOnScreen();
    }

    /**
     * Configura el icono de la aplicación
     */
    private void setupAppIcon(Stage stage) {
        try {
            Image icon = new Image(getClass().getResourceAsStream("/com/cenit/battleship/images/app-icon.png"));
            stage.getIcons().add(icon);
            System.out.println("✅ Icono de aplicación cargado");
        } catch (Exception e) {
            System.err.println("⚠️  No se pudo cargar el icono de la aplicación");
            // La aplicación puede funcionar sin icono
        }
    }

    // ========== MÉTODOS ESTÁTICOS PARA CONTROL DE VISTAS ==========

    /**
     * Cambia la vista raíz de la aplicación
     * @param fxml Ruta del archivo FXML (sin extensión)
     * @throws IOException Si no se puede cargar el FXML
     */
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /**
     * Cambia a una vista específica (alias de setRoot)
     * @param fxmlPath Ruta del archivo FXML
     * @throws IOException Si no se puede cargar el FXML
     */
    public static void changeView(String fxmlPath) throws IOException {
        System.out.println("fxmlPath :"+fxmlPath);
        setRoot(fxmlPath);
    }

    /**
     * Cambia a una vista específica con manejo de errores
     * @param fxmlPath Ruta del archivo FXML
     * @return true si el cambio fue exitoso, false en caso de error
     */
    public static boolean safeChangeView(String fxmlPath) {
        try {
            changeView(fxmlPath);
            System.out.println("✅ Vista cambiada a: " + fxmlPath);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error al cambiar a vista: " + fxmlPath);
            e.printStackTrace();
            showErrorDialog("Error de Navegación", 
                "No se pudo cargar la vista: " + fxmlPath + "\nError: " + e.getMessage());
            return false;
        }
    }

    /**
     * Carga un archivo FXML
     * @param fxml Ruta del archivo FXML (sin extensión)
     * @return El nodo raíz cargado
     * @throws IOException Si no se puede cargar el archivo
     */
    private static Parent loadFXML(String fxml) throws IOException {
        String fullPath = "/" + fxml + ".fxml";
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fullPath));
        
        System.out.println("📁 Cargando FXML: " + fullPath);
        Parent root = fxmlLoader.load();
        System.out.println("✅ FXML cargado exitosamente: " + fxml);
        
        return root;
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Obtiene el stage principal de la aplicación
     * @return Stage principal
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Obtiene la escena principal de la aplicación
     * @return Escena principal
     */
    public static Scene getScene() {
        return scene;
    }

    /**
     * Muestra un diálogo de error
     * @param title Título del diálogo
     * @param message Mensaje de error
     */
    public static void showErrorDialog(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de información
     * @param title Título del diálogo
     * @param message Mensaje informativo
     */
    public static void showInfoDialog(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    /**
     * Centra la ventana en la pantalla
     */
    public static void centerOnScreen() {
        if (primaryStage != null) {
            primaryStage.centerOnScreen();
        }
    }

    /**
     * Cambia el título de la ventana principal
     * @param newTitle Nuevo título
     */
    public static void setWindowTitle(String newTitle) {
        if (primaryStage != null) {
            primaryStage.setTitle(newTitle);
        }
    }

    /**
     * Restablece el título por defecto de la ventana
     */
    public static void resetWindowTitle() {
        setWindowTitle(APP_TITLE);
    }

    /**
     * Verifica si la aplicación está en modo pantalla completa
     * @return true si está en pantalla completa
     */
    public static boolean isFullScreen() {
        return primaryStage != null && primaryStage.isFullScreen();
    }

    /**
     * Alterna el modo pantalla completa
     */
    public static void toggleFullScreen() {
        if (primaryStage != null) {
            primaryStage.setFullScreen(!primaryStage.isFullScreen());
        }
    }

    // ========== MÉTODOS DE INICIALIZACIÓN Y LIMPIEZA ==========

    @Override
    public void init() throws Exception {
        super.init();
        System.out.println("🎮 Inicializando Battleship...");
        // Aquí puedes inicializar recursos globales
    }

    @Override
    public void stop() throws Exception {
        System.out.println("🛑 Cerrando Battleship...");
        // Aquí puedes liberar recursos y guardar estado
        super.stop();
    }

    /**
     * Punto de entrada principal de la aplicación
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Iniciando Battleship Game...");
            System.out.println("📋 Argumentos: " + String.join(" ", args));
            
            // Procesar argumentos de línea de comandos
            processCommandLineArgs(args);
            
            // Lanzar la aplicación JavaFX
            launch(args);
            
        } catch (Exception e) {
            System.err.println("💥 Error fatal al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Procesa los argumentos de línea de comandos
     * @param args Argumentos recibidos
     */
    private static void processCommandLineArgs(String[] args) {
        for (String arg : args) {
            switch (arg.toLowerCase()) {
                case "-debug":
                case "--debug":
                    System.out.println("🐛 Modo debug activado");
                    // Activar logging detallado
                    break;
                case "-fullscreen":
                case "--fullscreen":
                    System.out.println("🖥️  Modo pantalla completa activado");
                    // Podrías guardar esta preferencia
                    break;
                case "-help":
                case "--help":
                    showHelp();
                    System.exit(0);
                    break;
                default:
                    System.out.println("⚠️  Argumento desconocido: " + arg);
            }
        }
    }

    /**
     * Muestra la ayuda de línea de comandos
     */
    private static void showHelp() {
        System.out.println("\n🎮 Battleship Game - Ayuda de línea de comandos\n");
        System.out.println("Uso: java -jar battleship.jar [OPCIONES]");
        System.out.println("\nOpciones:");
        System.out.println("  -debug, --debug     Activa el modo debug");
        System.out.println("  -fullscreen, --fullscreen  Inicia en pantalla completa");
        System.out.println("  -help, --help       Muestra esta ayuda");
        System.out.println("\nEjemplos:");
        System.out.println("  java -jar battleship.jar -debug");
        System.out.println("  java -jar battleship.jar --fullscreen");
    }
}