package com.cenit.battleship.view;

import com.cenit.battleship.App;
import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.model.GameConfiguration;
import com.cenit.battleship.model.PlayerProfile;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.Skill;
import com.cenit.battleship.model.SkillSystem;
import com.cenit.battleship.model.enums.Difficulty;
import com.cenit.battleship.model.enums.ShipType;
import com.cenit.battleship.services.ProfileService;
import com.cenit.battleship.services.StorageService;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
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
    private ComboBox<String> comboDifficulty;
    @FXML
    private Label lblTitle;

    private StorageService storageService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        storageService = new StorageService();
        configureControls();
        configureEvents();
        loadSavedSettings();
    }

    private void configureControls() {
        // Configurar ComboBox de modos de juego
        comboModeGame.getItems().addAll(
                "Clásico",
                "Flota Especial",
                "Táctico",
                "Asimétrico",
                "Relámpago",
                "Enjambre"
        );
        comboModeGame.setValue("Clásico");

        // Configurar ComboBox de dificultad
        comboDifficulty.getItems().addAll(
                "Fácil",
                "Normal",
                "Difícil",
                "Experto"
        );
        comboDifficulty.setValue("Normal");

        // Estilos
        btnNewGame.getStyleClass().add("btn-primary");
        btnContinue.getStyleClass().add("btn-secondary");
        btnConfiguration.getStyleClass().add("btn-secondary");
        btnExit.getStyleClass().add("btn-danger");
    }

    private void configureEvents() {
        btnNewGame.setOnAction(e -> startNewGame(e));
        btnContinue.setOnAction(e -> continueGame());
        btnConfiguration.setOnAction(e -> openConfiguration());
        btnExit.setOnAction(e -> exitGame());
    }

    @FXML
    private void startNewGame(ActionEvent event) {
        try {
            System.out.println("🎮 Iniciando nuevo juego...");

            // 1. Validar selecciones del usuario
            if (!validateGameSelections()) {
                return;
            }

            // 2. Obtener configuración del juego
            String gameMode = comboModeGame.getValue();
            String playerName = ensureValidPlayerName();
            Difficulty difficulty = convertDisplayToDifficulty(comboDifficulty.getValue());

            // 3. Guardar configuraciones actuales
            saveCurrentSettings(gameMode, difficulty);

            // 4. Crear y configurar el controlador del juego
            GameController gameController = createAndConfigureGame(gameMode, difficulty);

            // 5. Configurar perfil de jugador y estadísticas
            setupPlayerProfile(gameController, playerName, gameMode, difficulty);

            // 6. Obtener la ventana actual (Stage) desde el evento del botón
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // 7. Preparar y mostrar la siguiente vista
            if (!prepareNextView(currentStage, gameController, gameMode, playerName)) {
                System.err.println("❌ No se pudo preparar la vista de colocación. Abortando.");
                return;
            }

            System.out.println("✅ Nuevo juego iniciado exitosamente");

        } catch (Exception ex) {
            handleStartGameError(ex);
        }
    }

    private void loadSavedSettings() {
        try {
            GameConfiguration config = GameConfiguration.getInstance();

            // Cargar modo de juego guardado
            String savedMode = config.getGameMode();
            if (savedMode != null && comboModeGame.getItems().contains(savedMode)) {
                comboModeGame.setValue(savedMode);
            }

            // Cargar dificultad guardada
            String savedDifficulty = config.getCpuDifficulty().name();
            if (savedDifficulty != null) {
                String displayDifficulty = convertDifficultyToDisplay(savedDifficulty);
                if (comboDifficulty.getItems().contains(displayDifficulty)) {
                    comboDifficulty.setValue(displayDifficulty);
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error cargando configuraciones guardadas: " + e.getMessage());
        }
    }

    private String convertDifficultyToDisplay(String difficulty) {
        switch (difficulty) {
            case "EASY":
                return "Fácil";
            case "NORMAL":
                return "Normal";
            case "HARD":
                return "Difícil";
            case "EXPERT":
                return "Experto";
            default:
                return "Normal";
        }
    }

    private Difficulty convertDisplayToDifficulty(String display) {
        switch (display) {
            case "Fácil":
                return Difficulty.EASY;
            case "Normal":
                return Difficulty.NORMAL;
            case "Difícil":
                return Difficulty.HARD;
            case "Experto":
                return Difficulty.EXPERT;
            default:
                return Difficulty.NORMAL;
        }
    }

    private boolean validateGameSelections() {
        if (comboModeGame.getValue() == null || comboModeGame.getValue().isEmpty()) {
            showAlert("Modo de Juego Requerido",
                    "Por favor selecciona un modo de juego para continuar.");
            comboModeGame.requestFocus();
            return false;
        }

        if (comboDifficulty.getValue() == null || comboDifficulty.getValue().isEmpty()) {
            showAlert("Dificultad Requerida",
                    "Por favor selecciona un nivel de dificultad.");
            comboDifficulty.requestFocus();
            return false;
        }

        System.out.println("✅ Validaciones pasadas - Listo para iniciar juego");
        return true;
    }

    private String ensureValidPlayerName() {
        String playerName = GameConfiguration.getInstance().getPlayerName();

        if (playerName == null || playerName.trim().isEmpty() || !isValidPlayerName(playerName)) {
            playerName = promptForPlayerNameWithValidation();

            if (playerName == null || playerName.trim().isEmpty()) {
                playerName = "Comandante";
                GameConfiguration.getInstance().setPlayerName(playerName);
            }
        }

        System.out.println("👤 Jugador: " + playerName);
        return playerName;
    }

    private void saveCurrentSettings(String gameMode, Difficulty difficulty) {
        try {
            GameConfiguration config = GameConfiguration.getInstance();
            config.setGameMode(gameMode);
            config.setCpuDifficulty(difficulty);
            config.saveConfiguration();
            System.out.println("💾 Configuraciones guardadas: Modo=" + gameMode + ", Dificultad=" + difficulty);
        } catch (Exception e) {
            System.err.println("⚠️ Error guardando configuraciones: " + e.getMessage());
        }
    }

    private GameController createAndConfigureGame(String gameMode, Difficulty difficulty) {
        System.out.println("🎯 Configurando juego - Modo: " + gameMode + ", Dificultad: " + difficulty.getDisplayName());

        // Crea un perfil por defecto para el jugador
        PlayerProfile defaultProfile = new PlayerProfile("Jugador");
        GameController controller = new GameController(defaultProfile, difficulty);

        controller.setDifficulty(difficulty);

        // Aplicar configuraciones específicas del modo
        applyGameModeConfigurations(controller, gameMode);

        // Configurar flota según el modo
        setupGameFleet(controller, gameMode);

        System.out.println("✅ Juego configurado exitosamente - Modo: " + gameMode
                + ", Dificultad: " + difficulty.getDisplayName());
        return controller;
    }

    private void setupPlayerProfile(GameController gameController, String playerName, String gameMode, Difficulty difficulty) {
        try {
            ProfileService profileService = new ProfileService();
            PlayerProfile playerProfile;

            if (profileService.profileExists(playerName)) {
                playerProfile = profileService.getProfile(playerName);
                System.out.println("📁 Perfil existente cargado: " + playerName);
            } else {
                playerProfile = profileService.createProfile(playerName);
                System.out.println("🆕 Nuevo perfil creado: " + playerName);
            }

            // Actualizar última vez jugado
            playerProfile.setLastPlayed(new Date());

            // Guardar preferencias de juego actual
            playerProfile.setPreference("lastGameMode", gameMode);
            playerProfile.setPreference("lastDifficulty", difficulty.name());

            // Guardar perfil actualizado
            profileService.saveProfile(playerProfile);

            System.out.println("✅ Perfil de jugador configurado");

        } catch (Exception e) {
            System.err.println("⚠️ Error al configurar perfil: " + e.getMessage());
        }
    }

    private boolean prepareNextView(Stage currentStage, GameController gameController, String gameMode, String playerName) {
        try {
            // Verificar que el controlador esté listo
            if (!gameController.areFleetsReady()) {
                showAlert("Error de Configuración", "Las flotas no están listas. No se puede iniciar el juego.");
                return false;
            }

            // Cargar el FXML y obtener el controlador
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cenit/battleship/view/PlacementView.fxml"));
            Parent root = loader.load();

            PlacementViewController placementController = loader.getController();
            if (placementController == null) {
                showAlert("Error", "No se pudo cargar el controlador de colocación.");
                return false;
            }

            // Configurar usando métodos de instancia
            placementController.setGameController(gameController);
            placementController.setGameMode(gameMode);
            placementController.setPlayerName(playerName);
            placementController.setDifficulty(comboDifficulty.getValue());

            // Crear una nueva escena con el contenido cargado
            Scene placementScene = new Scene(root);

            // Asignar la nueva escena a la ventana actual
            currentStage.setScene(placementScene);
            currentStage.setTitle("Coloca tus barcos");

            // Opcional: Ajustar el tamaño de la ventana al nuevo contenido
            currentStage.sizeToScene();

            System.out.println("✅ Vista de colocación preparada y mostrada exitosamente");
            return true;

        } catch (IOException e) {
            System.err.println("❌ Error al preparar vista: " + e.getMessage());
            showAlert("Error", "No se pudo preparar la vista del juego: " + e.getMessage());
            return false;
        }
    }

    private void setupGameFleet(GameController controller, String gameMode) {
        switch (gameMode) {
            case "Flota Especial":
                controller.setSpecialFleet();
                break;
            case "Táctico":
                controller.setTacticalFleet();
                break;
            case "Asimétrico":
                controller.setAsymmetricFleet();
                break;
            case "Relámpago":
                controller.setMinimalFleet();
                break;
            case "Enjambre":
                controller.setSwarmFleet();
                break;
            default:
                controller.setStandardFleet();
        }

        // Mostrar información de la flota configurada
        System.out.println(controller.getFleetInfo());

        // Verificar balance (solo informativo)
        if (!controller.areFleetsBalanced()) {
            System.out.println("⚠️  Las flotas no están balanceadas - Modo de juego desafiante");
        }
    }

    private void applyGameModeConfigurations(GameController controller, String gameMode) {
        System.out.println("🎮 Configurando modo: " + gameMode);

        switch (gameMode) {
            case "Flota Especial" -> configureSpecialFleetMode(controller);
            case "Táctico" -> configureTacticalMode(controller);
            case "Asimétrico" -> configureAsymmetricMode(controller);
            case "Relámpago" -> configureLightningMode(controller);
            case "Enjambre" -> configureSwarmMode(controller);
            default -> configureClassicMode(controller);
        }
    }

    private void configureClassicMode(GameController controller) {
        // Configuración clásica estándar
        SkillSystem skills = controller.getPlayerSkills();
        skills.reset();
        skills.addSkill(Skill.SONAR, 2);
        skills.addSkill(Skill.RADAR, 1);
        skills.setSkillPoints(4);
    }

    private void configureSpecialFleetMode(GameController controller) {
        SkillSystem skills = controller.getPlayerSkills();
        skills.reset();
        skills.addSkill(Skill.SONAR, 3);
        skills.addSkill(Skill.RADAR, 2);
        skills.addSkill(Skill.DRONE, 2);
        skills.setSkillPoints(6);
    }

    private void configureTacticalMode(GameController controller) {
        SkillSystem skills = controller.getPlayerSkills();
        skills.reset();
        skills.addSkill(Skill.SONAR, 2);
        skills.addSkill(Skill.RADAR, 2);
        skills.addSkill(Skill.DRONE, 2);
        skills.addSkill(Skill.GUIDED_MISSILE, 1);
        skills.addSkill(Skill.CLUSTER_BOMB, 1);
        skills.setSkillPoints(8);
    }

    private void configureAsymmetricMode(GameController controller) {
        SkillSystem skills = controller.getPlayerSkills();
        skills.reset();
        skills.addSkill(Skill.SONAR, 3);
        skills.addSkill(Skill.JAMMING, 2);
        skills.addSkill(Skill.REPAIR, 3);
        skills.addSkill(Skill.CLUSTER_BOMB, 2);
        skills.setSkillPoints(7);
    }

    private void configureLightningMode(GameController controller) {
        SkillSystem skills = controller.getPlayerSkills();
        skills.reset();
        skills.addSkill(Skill.GUIDED_MISSILE, 3);
        skills.addSkill(Skill.CLUSTER_BOMB, 3);
        skills.addSkill(Skill.DRONE, 1);
        skills.setSkillPoints(6);
    }

    private void configureSwarmMode(GameController controller) {
        SkillSystem skills = controller.getPlayerSkills();
        skills.reset();
        skills.addSkill(Skill.SONAR, 4);
        skills.addSkill(Skill.RADAR, 3);
        skills.addSkill(Skill.DRONE, 3);
        skills.setSkillPoints(8);
    }

    private void handleStartGameError(Exception ex) {
        System.err.println("💥 Error crítico al iniciar juego: " + ex.getMessage());
        ex.printStackTrace();

        showAlert("Error al Iniciar Juego",
                "No se pudo iniciar el juego debido a un error inesperado:\n"
                + ex.getMessage() + "\n\n"
                + "Por favor intenta nuevamente.");
    }

    private void continueGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cenit/battleship/view/SavedView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Cargar Partida Guardada");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(App.getPrimaryStage());
            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "No se pudieron cargar las partidas guardadas: " + ex.getMessage());
        }
    }

    private void openConfiguration() {
        try {
            App.changeView("view/ConfigurationView");
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "No se pudo abrir la configuración: " + ex.getMessage());
        }
    }

    private void exitGame() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Salir del Juego");
        confirmacion.setHeaderText("¿Estás seguro de que quieres salir?");
        confirmacion.setContentText("Cualquier progreso no guardado se perderá.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                App.getPrimaryStage().close();
            }
        });
    }

    private String promptForPlayerNameWithValidation() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nombre del Jugador");
        dialog.setHeaderText("Ingresa tu nombre");
        dialog.setContentText("Nombre:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String name = result.get().trim();
            if (isValidPlayerName(name)) {
                GameConfiguration.getInstance().setPlayerName(name);
                return name;
            } else {
                showAlert("Nombre Inválido",
                        "El nombre debe tener entre 2 y 15 caracteres y solo contener letras y espacios.");
                return promptForPlayerNameWithValidation();
            }
        }
        return "Comandante";
    }

    private boolean isValidPlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        String trimmedName = name.trim();

        // Longitud válida (2-15 caracteres)
        if (trimmedName.length() < 2 || trimmedName.length() > 15) {
            return false;
        }

        // Solo letras y espacios
        if (!trimmedName.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            return false;
        }

        // No solo espacios
        if (trimmedName.replaceAll("\\s+", "").isEmpty()) {
            return false;
        }

        return true;
    }

    private void showAlert(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}