/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.view;

import com.cenit.battleship.App;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 *
 * @author Usuario
 */

public class MainViewController implements Initializable {
    
    @FXML private VBox mainContainer;
    @FXML private Button btnNuevoJuego;
    @FXML private Button btnContinuar;
    @FXML private Button btnConfiguracion;
    @FXML private Button btnSalir;
    @FXML private ComboBox<String> comboModoJuego;
    @FXML private Label lblTitulo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarControles();
        configurarEventos();
    }

    private void configurarControles() {
        // Configurar ComboBox de modos de juego
        comboModoJuego.getItems().addAll(
            "Clásico",
            "Flota Especial", 
            "Táctico",
            "Asimétrico",
            "Extremo"
        );
        comboModoJuego.setValue("Clásico");
        
        // Estilos
        btnNuevoJuego.getStyleClass().add("btn-primary");
        btnContinuar.getStyleClass().add("btn-secondary");
        btnConfiguracion.getStyleClass().add("btn-secondary");
        btnSalir.getStyleClass().add("btn-danger");
    }

    private void configurarEventos() {
        btnNuevoJuego.setOnAction(e -> iniciarNuevoJuego());
        btnContinuar.setOnAction(e -> continuarJuego());
        btnConfiguracion.setOnAction(e -> abrirConfiguracion());
        btnSalir.setOnAction(e -> salirDelJuego());
    }

    private void iniciarNuevoJuego() {
        try {
            String modoSeleccionado = comboModoJuego.getValue();
            // Pasar el modo de juego al controlador de colocación
           // ColocacionViewController.setModoJuego(modoSeleccionado);
            App.changeView("view/ColocacionView");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void continuarJuego() {
        // TODO: Implementar carga de partida guardada
        System.out.println("Continuar juego - pendiente");
    }

    private void abrirConfiguracion() {
        try {
            App.changeView("view/ConfiguracionView");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void salirDelJuego() {
        App.getPrimaryStage().close();
    }
}    
    

