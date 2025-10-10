/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.view;

import com.cenit.battleship.App;
import com.cenit.battleship.controller.CPUController;
import com.cenit.battleship.model.Configuration;
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
        switch(config.getCPUDifficulty()) {
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
        // Dificultad
        switch(DifficultyCombo.getValue()) {
            case "Fácil": config.setCPUDifficulty(CPUController.Difficulty.EASY); break;
            case "Normal": config.setCPUDifficulty(CPUController.Difficulty.NORMAL); break;
            case "Difícil": config.setCPUDifficulty(CPUController.Difficulty.HARD); break;
            case "Experto": config.setCPUDifficulty(CPUController.Difficulty.EXPERT); break;
        }
        
        // Sonido
        config.setSoundEnabled(checkSound.isSelected());
        config.setSoundVolume(sliderVolume.getValue() / 100.0);
        
        // Animaciones y velocidad
        config.setAnimationsEnabled(checkAnimations.isSelected());
        config.setGameSpeed(sliderVelocity.getValue() / 100.0);
        
        // Jugador
        config.setPlayerName(txtName.getText());
        config.setShowHelp(checkHelps.isSelected());
        
        // Tema
        config.setVisualTheme(themeCombo.getValue());
        
        // Guardar en archivo
        config.saveConfiguration();
        
        // Mostrar mensaje de confirmación
        showMessage("Configuración guardada exitosamente", "La configuración se ha guardado correctamente.");
        
        try {
            App.changeView("view/MainView");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void restoreDefaults() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Restaurar configuración");
        alert.setHeaderText("¿Restaurar configuración predeterminada?");
        alert.setContentText("Se perderán todos los cambios no guardados.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                config.ResetSettings();
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
