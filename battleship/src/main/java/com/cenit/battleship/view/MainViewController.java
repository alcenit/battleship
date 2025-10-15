/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.view;

import com.cenit.battleship.App;
import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.sevices.StorageService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
            String modoSeleccionado = comboModeGame.getValue();
            // Pasar el modo de juego al controlador de colocación
           // ColocacionViewController.setModoJuego(modoSeleccionado);
            App.changeView("view/ColocacionView");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
   
   
    private void openConfiguration() {
        try {
            App.changeView("view/ConfiguracionView");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void exitGame() {
        App.getPrimaryStage().close();
    }
    
     private void showAlert(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
     
     
}    
    

