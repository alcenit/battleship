package com.cenit.battleship;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL; // CAMBIO: Añadido import para URL
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.StageStyle;

/**
 * JavaFX App - Battleship Game Aplicación principal del juego Battleship
 */
public class App extends Application {

    private static Scene scene;
    private static Stage primaryStage;
    
    private Image appIcon; // Variable de instancia para almacenar el icono

    // Constantes de configuración
    private static final String APP_TITLE = "Battleship - Edición Especial";
    private static final double MIN_WIDTH = 1000;
    private static final double MIN_HEIGHT = 700;
    private static final double DEFAULT_WIDTH = 1200;
    private static final double DEFAULT_HEIGHT = 800;
    private static final String DEFAULT_VIEW = "com/cenit/battleship/view/MainView";
    private static final String CSS_PATH = "/com/cenit/battleship/styles/mainview.css";
    // CONSTATNTE DIMENSIONES DE TABLERO Y CONFIGURACIONES EN CLASE GAMECONFIGURATION

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
    /**
    private void setupPrimaryStage(Stage stage) {
        // Configuración básica
        stage.setTitle(APP_TITLE);
        stage.setScene(scene);

        // Aplicar color de fondo al nodo raíz
        if (scene.getRoot() != null) {
            scene.getRoot().setStyle("-fx-background-color: #4682B4;"); // Azul acero
        }

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
    */
    private void setupPrimaryStage(Stage stage) {
    // Configuración básica
    stage.setTitle(APP_TITLE);
    stage.setScene(scene);
    
    // Eliminar el marco nativo de Windows
    stage.initStyle(StageStyle.UNDECORATED);
    
    // Configurar icono de la aplicación
        setupAppIcon(stage);
    
    // Crear barra de título personalizada
    createCustomTitleBar(stage);
    
    
    
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

private void createCustomTitleBar(Stage stage) {
    // Obtener el nodo raíz actual
    Parent currentRoot = scene.getRoot();
    
    // Crear un BorderPane como nuevo contenedor principal
    BorderPane mainContainer = new BorderPane();
    
    // Crear barra de título personalizada
    HBox titleBar = new HBox();
    titleBar.setStyle("-fx-background-color: #2E8B57; -fx-padding: 8 10 8 10;");
    titleBar.setAlignment(Pos.CENTER_LEFT);
    titleBar.setPrefHeight(35);
    
    // Icono y título de la aplicación
    HBox titleContent = new HBox();
    titleContent.setAlignment(Pos.CENTER_LEFT);
    titleContent.setSpacing(8);
    
     // Agregar el icono a la barra de título (si está disponible)
    if (appIcon != null) {
        ImageView iconView = new ImageView(appIcon);
        iconView.setFitHeight(25);
        iconView.setFitWidth(25);
        iconView.setPreserveRatio(true);
        titleContent.getChildren().add(iconView);
    } else {
        // Icono alternativo o emoji si no se pudo cargar
        Label iconPlaceholder = new Label("🚢");
        titleContent.getChildren().add(iconPlaceholder);
    }
    
    // Puedes agregar un icono pequeño aquí si quieres
    Label titleLabel = new Label( APP_TITLE);
    titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
    
    titleContent.getChildren().add(titleLabel);
    
    // Contenedor para botones de control de ventana
    HBox controlButtons = new HBox();
    controlButtons.setAlignment(Pos.CENTER_RIGHT);
    controlButtons.setSpacing(5);
    
    // Botón de minimizar
    Button minimizeBtn = createControlButton("−", "#4499ff");
    minimizeBtn.setOnAction(e -> stage.setIconified(true));
    
    // Botón de cerrar
    Button closeBtn = createControlButton("×", "#ff4444");
    closeBtn.setOnAction(e -> stage.close());
    
    controlButtons.getChildren().addAll(minimizeBtn, closeBtn);
    
    // Espaciador para empujar los botones a la derecha
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    
    // Agregar elementos a la barra de título
    titleBar.getChildren().addAll(titleContent, spacer, controlButtons);
    
    // Configurar el layout principal
    mainContainer.setTop(titleBar);
    mainContainer.setCenter(currentRoot); // Mantener tu contenido original
    
    // Actualizar la escena con el nuevo layout
    scene.setRoot(mainContainer);
    
    // Hacer la ventana arrastrable desde la barra de título
    makeWindowDraggable(titleBar, stage);
}

private Button createControlButton(String text, String color) {
    Button button = new Button(text);
    button.setStyle(
        "-fx-background-color: " + color + "; " +
        "-fx-text-fill: white; " +
        "-fx-font-weight: bold; " +
        "-fx-font-size: 12; " +
        "-fx-min-width: 30; " +
        "-fx-min-height: 20; " +
        "-fx-border-radius: 3; " +
        "-fx-background-radius: 3; " +
        "-fx-cursor: hand;"
    );
    
    // Efecto hover
    button.setOnMouseEntered(e -> {
        if (text.equals("×")) {
            button.setStyle(button.getStyle() + "-fx-background-color: #ff6666;");
        } else {
            button.setStyle(button.getStyle() + "-fx-background-color: #55aaff;");
        }
    });
    
    button.setOnMouseExited(e -> {
        button.setStyle(button.getStyle().replace("-fx-background-color: #ff6666;", "-fx-background-color: " + color + ";")
                                   .replace("-fx-background-color: #55aaff;", "-fx-background-color: " + color + ";"));
    });
    
    return button;
}

private void makeWindowDraggable(Node node, Stage stage) {
    final double[] xOffset = new double[1];
    final double[] yOffset = new double[1];
    
    node.setOnMousePressed(event -> {
        xOffset[0] = event.getSceneX();
        yOffset[0] = event.getSceneY();
    });
    
    node.setOnMouseDragged(event -> {
        stage.setX(event.getScreenX() - xOffset[0]);
        stage.setY(event.getScreenY() - yOffset[0]);
    });
}
    /**
     * Configura el icono de la aplicación
     */
    private void setupAppIcon(Stage stage) {
        try {
        appIcon = new Image(getClass().getResourceAsStream("/com/cenit/battleship/images/app-icon.png"));
        stage.getIcons().add(appIcon);
        System.out.println("✅ Icono de aplicación cargado");
        } catch (Exception e) {
            System.err.println("⚠️  No se pudo cargar el icono de la aplicación");
            // La aplicación puede funcionar sin icono
        }
    }

    // ========== MÉTODOS ESTÁTICOS PARA CONTROL DE VISTAS ==========
    /**
     * Cambia la vista raíz de la aplicación
     *
     * @param fxml Ruta del archivo FXML (sin extensión)
     * @throws IOException Si no se puede cargar el FXML
     */
    public static void setRoot(String fxml) throws IOException {
        Parent newRoot = loadFXML(fxml);
        scene.setRoot(newRoot);
        // CAMBIO: Añadida traza de depuración CRUCIAL
        System.out.println("DEBUG: scene.getRoot() cambiado a: " + scene.getRoot().getClass().getSimpleName());
    }

    /**
     * Cambia a una vista específica (alias de setRoot)
     *
     * @param fxmlPath Ruta del archivo FXML
     * @throws IOException Si no se puede cargar el FXML
     */
    public static void changeView(String fxmlPath) {
    try {
        System.out.println("? Cambiando a vista: " + fxmlPath);
        
        // Cargar el FXML
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlPath));
        Parent root = loader.load();
        
        // Obtener el stage principal
        Stage primaryStage = getPrimaryStage();
        if (primaryStage == null) {
            System.err.println("? ERROR: PrimaryStage es null");
            return;
        }
        
        // Crear nueva escena
        Scene scene = new Scene(root);
        
        // Preservar el tamaño actual de la ventana
        double currentWidth = primaryStage.getWidth();
        double currentHeight = primaryStage.getHeight();
        
        // Cambiar la escena
        primaryStage.setScene(scene);
        
        // Restaurar el tamaño y posición
        if (currentWidth > 0 && currentHeight > 0) {
            primaryStage.setWidth(currentWidth);
            primaryStage.setHeight(currentHeight);
        }
        
        // Forzar el redibujado
        primaryStage.show();
        
        System.out.println("? Vista cambiada exitosamente a: " + fxmlPath);
        
    } catch (IOException e) {
        System.err.println("? ERROR CRÍTICO en changeView(): " + e.getMessage());
        e.printStackTrace();
    }
}

    
    

    /**
     * Carga un archivo FXML
     *
     * @param fxml Ruta del archivo FXML (sin extensión)
     * @return El nodo raíz cargado
     * @throws IOException Si no se puede cargar el archivo
     */
    private static Parent loadFXML(String fxml) throws IOException {
        String fullPath = "/" + fxml + ".fxml";
        // CAMBIO: Mejorado el manejo de recursos para dar un error más claro
        URL resourceUrl = App.class.getResource(fullPath);
        if (resourceUrl == null) {
            throw new IOException("No se puede encontrar el recurso FXML: " + fullPath);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(resourceUrl);

        System.out.println("📁 Cargando FXML: " + fullPath);
        Parent root = fxmlLoader.load();
        System.out.println("✅ FXML cargado exitosamente: " + fxml);

        return root;
    }

    // ========== MÉTODOS DE UTILIDAD ==========
    /**
     * Obtiene el stage principal de la aplicación
     *
     * @return Stage principal
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Obtiene la escena principal de la aplicación
     *
     * @return Escena principal
     */
    public static Scene getScene() {
        return scene;
    }

    /**
     * Muestra un diálogo de error
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
