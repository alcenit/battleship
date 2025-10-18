/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.view;

import com.cenit.battleship.App;
import com.cenit.battleship.controller.CPUController;
import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.model.Configuration;
import com.cenit.battleship.model.PlayerProfile;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.Skill;
import com.cenit.battleship.model.SkillSystem;
import com.cenit.battleship.model.enums.ShipType;
import com.cenit.battleship.services.StorageService;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainViewController implements Initializable {

    @FXML
    private VBox mainContainer;
    @FXML
    private Button btnNewGame;
    @FXML
    private Button btnContinue;
    @FXML
    private Button btnConfiguration;
    @FXML
    private Button btnExit;
    @FXML
    private ComboBox<String> comboModeGame;
    @FXML
    private Label lblTitle;

    private StorageService storageService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        storageService = new StorageService();
        configureControls();
        configureEvents();
    }

    private void configureControls() {
        // Configurar ComboBox de modos de juego
        comboModeGame.getItems().addAll(
                "Clásico",
                "Flota Especial",
                "Táctico",
                "Asimétrico",
                "Extremo"
        );
        comboModeGame.setValue("Clásico");

        // Estilos
        btnNewGame.getStyleClass().add("btn-primary");
        btnContinue.getStyleClass().add("btn-secondary");
        btnConfiguration.getStyleClass().add("btn-secondary");
        btnExit.getStyleClass().add("btn-danger");
    }

    private void configureEvents() {
        btnNewGame.setOnAction(e -> startNewGame());
        btnContinue.setOnAction(e -> continueGame());
        btnConfiguration.setOnAction(e -> openConfiguration());
        btnExit.setOnAction(e -> exitGame());
    }

    private void startNewGame() {
        try {
            // 1. Validar selecciones
            if (!validateSelections()) {
                return;
            }

            // 2. Obtener configuración
            String gameMode = comboModeGame.getValue();
            String playerName = ensurePlayerName();

            // 3. Crear y configurar juego
            GameController gameController = createAndConfigureGame(gameMode);

            // 4. Preparar siguiente vista
            prepareNextView(gameController, gameMode, playerName);

            // 5. Cambiar a vista de colocación
            App.changeView("view/ColocacionView");

        } catch (Exception ex) {
            handleStartGameError(ex);
        }
    }

    private boolean validateSelections() {
        if (comboModeGame.getValue() == null) {
            showAlert("Modo no seleccionado", "Por favor selecciona un modo de juego");
            return false;
        }
        return true;
    }

    private String ensurePlayerName() {
        String name = Configuration.getInstance().getPlayerName();
        if (name == null || name.trim().isEmpty()) {
            name = promptForPlayerNameWithAvatar();//promptForPlayerName();
        }
        return name;
    }

    private GameController createAndConfigureGame(String gameMode) {
        CPUController.Difficulty difficulty = getDifficultyFromMode(gameMode);
        GameController controller = new GameController(difficulty);

        // Aplicar configuraciones específicas del modo
        applyGameModeConfigurations(controller, gameMode);

        return controller;
    }

    private void prepareNextView(GameController controller, String gameMode, String playerName) {
        PlacementViewController.setGameController(controller);
        PlacementViewController.setGameMode(gameMode);
        PlacementViewController.setPlayerName(playerName);
    }

    private void handleStartGameError(Exception ex) {
        System.err.println("Error al iniciar juego: " + ex.getMessage());
        ex.printStackTrace();
        showAlert("Error Inesperado",
                "No se pudo iniciar el juego. Por favor intenta nuevamente.\n"
                + "Error: " + ex.getMessage());
    }

    private void continueGame() {
        try {
            // Mostrar diálogo de partidas guardadas
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GuardadosView.fxml"));
            Parent root = loader.load();

            SavedViewController controller = loader.getController();
            controller.setSaveGameListener(new SavedViewController.SaveGameListener() {
                @Override
                public void onPartidaCargada(String nombreArchivo) {

                    loadGame(nombreArchivo);
                }

                @Override
                public void onDialogoCerrado() {
                    // No hacer nada
                }
            });

            Stage stage = new Stage();
            stage.setTitle("Cargar Partida");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "No se pudieron cargar las partidas guardadas.");
        }
    }

    private void loadGame(String nombreArchivo) {
        try {
            GameController gameControllerCargado = storageService.loadGame(nombreArchivo);
            if (gameControllerCargado != null) {
                // Pasar el controlador cargado a la vista de juego
                GameViewController.setGameController(gameControllerCargado);
                App.changeView("view/JuegoView");
            } else {
                showAlert("Error", "No se pudo cargar la partida seleccionada.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Error al cargar la partida: " + ex.getMessage());
        }
    }

    private String promptForPlayerNameWithAvatar() {
        Dialog<PlayerProfile> dialog = new Dialog<>();
        dialog.setTitle("Crear Perfil");
        dialog.setHeaderText("Configura tu perfil de jugador");

        // Crear contenido avanzado
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Campo de nombre
        Label nameLabel = new Label("Nombre del Jugador:");
        TextField nameField = new TextField();
        nameField.setPromptText("Ingresa tu nombre...");
        nameField.setPrefWidth(200);

        // Selector de avatar (opcional)
        Label avatarLabel = new Label("Selecciona un avatar:");
        HBox avatarBox = createAvatarSelector();

        // Etiqueta de error
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(false);

        content.getChildren().addAll(nameLabel, nameField, avatarLabel, avatarBox, errorLabel);
        dialog.getDialogPane().setContent(content);

        // Botones
        ButtonType startButton = new ButtonType("Comenzar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(startButton, ButtonType.CANCEL);

        // Validación en tiempo real
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            ValidationResult validation = validatePlayerName(newVal);
            errorLabel.setText(validation.getErrorMessage());
            errorLabel.setVisible(!validation.isValid());

            // Habilitar/deshabilitar botón basado en validación
            Node startButtonNode = dialog.getDialogPane().lookupButton(startButton);
            startButtonNode.setDisable(!validation.isValid());
        });

        // Result converter
        dialog.setResultConverter(buttonType -> {
            if (buttonType == startButton) {
                String name = nameField.getText().trim();
                String avatar = getSelectedAvatar();
                return new PlayerProfile(name, avatar);
            }
            return null;
        });

        Optional<PlayerProfile> result = dialog.showAndWait();
        return result.map(PlayerProfile::getName).orElse("Comandante");
    }

    private String getPlayerName() {
        // Opción 1: Desde configuración
        Configuration config = Configuration.getInstance();
        String name = config.getPlayerName();

        // Opción 2: Desde un campo de texto en la vista principal
        // if (txtPlayerName != null) {
        //     name = txtPlayerName.getText().trim();
        // }
        // Opción 3: Diálogo para ingresar nombre
        if (name == null || name.isEmpty()) {
            name = showNameInputDialog();
        }

        return name;
    }

    private String showNameInputDialog() {
        TextInputDialog dialog = new TextInputDialog("Jugador");
        dialog.setTitle("Nombre del Jugador");
        dialog.setHeaderText("Ingresa tu nombre");
        dialog.setContentText("Nombre:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse("Jugador");
    }

    private GameController createGameController(String modo) {
        CPUController.Difficulty dificultad = getDifficultyFromMode(modo);
        GameController gameController = new GameController(dificultad);

        // Configuraciones específicas del modo
        switch (modo) {
            case "Flota Especial":
                setupSpecialFleet(gameController);
                break;
            case "Táctico":
                setupTacticalMode(gameController);
                break;
            case "Asimétrico":
                setupAsymmetricMode(gameController);
                break;
        }

        return gameController;
    }

    private CPUController.Difficulty getDifficultyFromMode(String modo) {
        switch (modo) {
            case "Principiante":
                return CPUController.Difficulty.EASY;
            case "Normal":
                return CPUController.Difficulty.NORMAL;
            case "Avanzado":
                return CPUController.Difficulty.HARD;
            case "Experto":
                return CPUController.Difficulty.EXPERT;
            default:
                return CPUController.Difficulty.NORMAL;
        }
    }

    private void configureGameMode(String modo) {
        CPUController.Difficulty dificultad;

        switch (modo) {
            case "Principiante":
                dificultad = CPUController.Difficulty.EASY;
                break;
            case "Normal":
                dificultad = CPUController.Difficulty.NORMAL;
                break;
            case "Avanzado":
                dificultad = CPUController.Difficulty.HARD;
                break;
            case "Experto":
                dificultad = CPUController.Difficulty.EXPERT;
                break;
            case "Flota Especial":
                dificultad = CPUController.Difficulty.NORMAL;
                // Configurar flota especial
                configureSpecialFleet();
                break;
            case "Táctico":
                dificultad = CPUController.Difficulty.HARD;
                // Configurar habilidades tácticas
                configureTacticalMode();
                break;
            default:
                dificultad = CPUController.Difficulty.NORMAL;
        }

        // Guardar configuración
        Configuration config = Configuration.getInstance();
        config.setCpuDifficulty(dificultad);
        config.saveConfiguration();
    }

    private void setupSpecialFleet(GameController gameController) {
        // Configurar flota con barcos especiales
        List<Ship> specialFleet = createSpecialFleet();
        gameController.setPlayerShips(specialFleet);
        // También configurar flota especial para la CPU
        gameController.setCpuShips(createSpecialFleet());
    }

    private void setupTacticalMode(GameController gameController) {
        // Habilitar habilidades especiales desde el inicio
        SkillSystem playerSkills = gameController.getPlayerSkills();
        playerSkills.addSkill(Skill.SONAR, 3);
        playerSkills.addSkill(Skill.RADAR, 2);
        playerSkills.addSkill(Skill.GUIDED_MISSILE, 1);

        // Dar puntos iniciales
        playerSkills.setSkillPoints(5);
    }

    private void setupAsymmetricMode(GameController gameController) {
        // Jugador: barcos pequeños pero muchos
        List<Ship> playerFleet = Arrays.asList(
                new Ship(ShipType.FRIGATE),
                new Ship(ShipType.FRIGATE),
                new Ship(ShipType.FRIGATE),
                new Ship(ShipType.DESTROYER),
                new Ship(ShipType.DESTROYER),
                new Ship(ShipType.SUBMARINE)
        );

        // CPU: barcos grandes pero pocos
        List<Ship> cpuFleet = Arrays.asList(
                new Ship(ShipType.CARRIER),
                new Ship(ShipType.BATTLESHIP),
                new Ship(ShipType.CRUISER)
        );

        gameController.setPlayerShips(playerFleet);
        gameController.setCpuShips(cpuFleet);
    }

    private void openConfiguration() {
        try {
            App.changeView("view/ConfiguracionView");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void savePlayerName(String name) {
        Configuration config = Configuration.getInstance();
        config.setPlayerName(name);
        config.saveConfiguration();
        System.out.println("Nombre guardado: " + name);
    }
    
    private HBox createAvatarSelector() {
    HBox avatarBox = new HBox(10);
    ToggleGroup avatarGroup = new ToggleGroup();
    
    String[] avatars = {"🚢", "⚓", "🧭", "🌟", "⚔️", "🛡️"};
    
    for (String avatar : avatars) {
        ToggleButton btn = new ToggleButton(avatar);
        btn.setToggleGroup(avatarGroup);
        btn.setStyle("-fx-font-size: 20px; -fx-pref-width: 40px; -fx-pref-height: 40px;");
        avatarBox.getChildren().add(btn);
    }
    
    // Seleccionar primer avatar por defecto
    //en vez de (!avatars.isEmpty())
    if (!avatars.equals(null)) {
        ((ToggleButton) avatarBox.getChildren().get(0)).setSelected(true);
    }
    
    return avatarBox;
}

private String getSelectedAvatar() {
    // Lógica para obtener avatar seleccionado
    return "🚢"; // Por defecto
}


    private void exitGame() {
        App.getPrimaryStage().close();
    }
    
    
    private void validateNameInRealTime(TextField field, String newValue) {
        if (newValue.length() > 15) {
            field.setText(newValue.substring(0, 15));
            field.positionCaret(15);
        }

        // Cambiar color del borde según validación
        if (isValidPlayerName(newValue)) {
            field.setStyle("-fx-border-color: green; -fx-border-width: 1px;");
        } else {
            field.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        }
    }
    
    //este metodo puede ser una duplicacion revisar 
    private boolean isValidPlayerName(String name) {
    if (name == null || name.trim().isEmpty()) {
        return false;
    }
    
    String trimmedName = name.trim();
    
    // Longitud válida (2-15 caracteres)
    if (trimmedName.length() < 2 || trimmedName.length() > 15) {
        return false;
    }
    
    // Solo letras, números y espacios
    if (!trimmedName.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 ]+$")) {
        return false;
    }
    
    // No solo espacios
    if (trimmedName.replaceAll("\\s+", "").isEmpty()) {
        return false;
    }
    
    return true;
}
    private static class ValidationResult {

        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    private ValidationResult validatePlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "El nombre no puede estar vacío");
        }

        String trimmedName = name.trim();

        if (trimmedName.length() < 2) {
            return new ValidationResult(false, "El nombre debe tener al menos 2 caracteres");
        }

        if (trimmedName.length() > 15) {
            return new ValidationResult(false, "El nombre no puede tener más de 15 caracteres");
        }

        if (!trimmedName.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            return new ValidationResult(false, "Solo se permiten letras y espacios");
        }

        if (trimmedName.matches(".*\\d+.*")) {
            return new ValidationResult(false, "No se permiten números en el nombre");
        }

        return new ValidationResult(true, "");
    }

    private void showAlert(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

}
