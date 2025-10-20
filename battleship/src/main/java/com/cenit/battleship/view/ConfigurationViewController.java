/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.view;

import com.cenit.battleship.App;
import com.cenit.battleship.model.Configuration;
import com.cenit.battleship.model.enums.Difficulty;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;


public class ConfigurationViewController implements Initializable {
    
   
   
    @FXML private ComboBox<String> DifficultyCombo;
    @FXML private CheckBox checkSound;
    @FXML private CheckBox checkAnimations;
    @FXML private Slider sliderVolume;
    @FXML private Slider sliderVelocity;
    @FXML private TextField txtName;
    @FXML private CheckBox checkHelps;
    @FXML private ComboBox<String> themeCombo;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Button btndefault;
    @FXML private Label lblVolume;
    @FXML private Label lblVelocity;
    
    private Configuration config;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = Configuration.getInstance();
        loadCurrentConfiguration();
        controlsConfiguration();
        configureEvents();
    }

    private void loadCurrentConfiguration() {
        // Dificultad
        switch(config.getCpuDifficulty()) {
            case EASY: DifficultyCombo.setValue("Fácil"); break;
            case NORMAL: DifficultyCombo.setValue("Normal"); break;
            case HARD: DifficultyCombo.setValue("Difícil"); break;
            case EXPERT: DifficultyCombo.setValue("Experto"); break;
        }
        
        // Sonido y animaciones
        checkSound.setSelected(config.isSoundEnabled());
        checkAnimations.setSelected(config.isAnimationsEnabled());
        
        // Sliders
        sliderVolume.setValue(config.getSoundVolume() * 100);
        sliderVelocity.setValue(config.getGameSpeed() * 100);
        
        // Jugador
        txtName.setText(config.getPlayerName());
        checkHelps.setSelected(config.isShowHelp());
        
        // Tema
        themeCombo.setValue(config.getVisualTheme());
        
        updateLabels();
    }

    private void controlsConfiguration() {
        // Configurar ComboBoxes
        DifficultyCombo.getItems().addAll("Fácil", "Normal", "Difícil", "Experto");
        themeCombo.getItems().addAll("default", "oscuro", "azul", "verde", "rojo");
        
        // Configurar Sliders
        sliderVolume.valueProperty().addListener((obs, oldVal, newVal) -> updateLabels());
        sliderVelocity.valueProperty().addListener((obs, oldVal, newVal) -> updateLabels());
        
        // Habilitar/deshabilitar controles relacionados
        checkSound.selectedProperty().addListener((obs, oldVal, newVal) -> {
            sliderVolume.setDisable(!newVal);
            updateLabels();
        });
    }

    private void configureEvents() {
        btnSave.setOnAction(e -> saveConfiguration());
        btnCancel.setOnAction(e -> cancel());
        btndefault.setOnAction(e -> restoreDefaults());
    }

    private void updateLabels() {
        lblVolume.setText("Volumen: " + (int)sliderVolume.getValue() + "%");
        lblVelocity.setText("Velocidad: " + (int)sliderVelocity.getValue() + "%");
        
        // Actualizar estado del slider de volumen
        if (!checkSound.isSelected()) {
            lblVolume.setText("Volumen: Deshabilitado");
        }
    }

    private void saveConfiguration() {
    try {
        System.out.println("💾 Guardando configuración...");
        
        // Validar y configurar dificultad
        configureDifficulty();
        
        // Configuración de sonido
        config.setSoundEnabled(checkSound.isSelected());
        config.setSoundVolume(validateVolume(sliderVolume.getValue()));
        
        // Configuración de animaciones
        config.setAnimationsEnabled(checkAnimations.isSelected());
        config.setGameSpeed(validateGameSpeed(sliderVelocity.getValue()));
        
        // Configuración de jugador
        configurePlayerSettings();
        
        // Configuración de tema visual
        configureVisualTheme();
        
        // Guardar configuración - método void
        config.saveConfiguration();
        System.out.println("✅ Configuración guardada exitosamente");
        
        showSuccessMessage();
        navigateToMainView();
        
    } catch (Exception ex) {
        handleSaveError(ex);
    }
}

private void configureDifficulty() {
    String selectedDifficulty = DifficultyCombo.getValue();
    System.out.println("🎯 Configurando dificultad: " + selectedDifficulty);
    
    switch(selectedDifficulty) {
        case "Fácil": 
            config.setCpuDifficulty(Difficulty.EASY);
            break;
        case "Normal": 
            config.setCpuDifficulty(Difficulty.NORMAL);
            break;
        case "Difícil": 
            config.setCpuDifficulty(Difficulty.HARD);
            break;
        case "Experto": 
            config.setCpuDifficulty(Difficulty.EXPERT);
            break;
        default:
            config.setCpuDifficulty(Difficulty.NORMAL);
            System.out.println("⚠️  Dificultad no reconocida, usando Normal por defecto");
            break;
    }
}

private void configurePlayerSettings() {
    String playerName = txtName.getText().trim();
    if (playerName.isEmpty()) {
        playerName = "Comandante";
        System.out.println("👤 Usando nombre por defecto: " + playerName);
    } else if (!isValidPlayerName(playerName)) {
        showMessage("Nombre inválido", 
            "El nombre debe tener entre 2-15 caracteres y solo letras/espacios.");
        playerName = "Comandante";
    }
    
    config.setPlayerName(playerName);
    config.setShowHelp(checkHelps.isSelected());
    
    System.out.println("👤 Jugador: " + playerName + ", Ayudas: " + checkHelps.isSelected());
}

private void configureVisualTheme() {
    String theme = themeCombo.getValue();
    if (theme != null && !theme.trim().isEmpty()) {
        config.setVisualTheme(theme);
        System.out.println("🎨 Tema configurado: " + theme);
    } else {
        config.setVisualTheme("Clásico");
        System.out.println("🎨 Usando tema por defecto: Clásico");
    }
}

private double validateVolume(double volume) {
    return Math.max(0.0, Math.min(1.0, volume / 100.0));
}

private double validateGameSpeed(double speed) {
    return Math.max(0.1, Math.min(1.0, speed / 100.0));
}

private boolean isValidPlayerName(String name) {
    return name != null && 
           name.length() >= 2 && 
           name.length() <= 15 && 
           name.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$");
}

private void showSuccessMessage() {
    showMessage("Configuración guardada", 
        "La configuración se ha guardado correctamente.\n\n" +
        "• Dificultad: " + DifficultyCombo.getValue() + "\n" +
        "• Jugador: " + config.getPlayerName() + "\n" +
        "• Sonido: " + (config.isSoundEnabled() ? "Activado" : "Desactivado"));
}

private void navigateToMainView() {
    try {
        App.changeView("view/MainView");
    } catch (Exception ex) {
        System.err.println("⚠️ Error navegando al menú principal: " + ex.getMessage());
        // No mostrar error al usuario en este caso
    }
}

private void handleSaveError(Exception ex) {
    System.err.println("❌ Error crítico guardando configuración: " + ex.getMessage());
    ex.printStackTrace();
    showMessage("Error al guardar", 
        "No se pudo guardar la configuración:\n" + ex.getMessage() + 
        "\n\nPor favor verifica que tengas permisos de escritura.");
}

    private void restoreDefaults() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Restaurar configuración");
        alert.setHeaderText("¿Restaurar configuración predeterminada?");
        alert.setContentText("Se perderán todos los cambios no guardados.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                config.resetToDefaults();
                loadCurrentConfiguration();
                showMessage("Configuración restaurada", "Se han restaurado los valores predeterminados.");
            }
        });
    }

    private void cancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancelar cambios");
        alert.setHeaderText("¿Salir sin guardar?");
        alert.setContentText("Los cambios no guardados se perderán.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    App.changeView("view/MainView");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void showMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
   
    
      
}
