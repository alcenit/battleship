/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.view;

import com.cenit.battleship.model.SaveGameInfo;
import com.cenit.battleship.services.StorageService;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author Usuario
 */
public class SavedViewController implements Initializable {

    @FXML
    private TableView<SaveGameInfo> tablaGuardados;
    @FXML
    private TableColumn<SaveGameInfo, String> colNombre;
    @FXML
    private TableColumn<SaveGameInfo, String> colFecha;
    @FXML
    private TableColumn<SaveGameInfo, String> colTurnos;
    @FXML
    private TableColumn<SaveGameInfo, String> colBarcosJugador;
    @FXML
    private TableColumn<SaveGameInfo, String> colBarcosCPU;
    @FXML
    private TableColumn<SaveGameInfo, String> colTamaño;

    @FXML
    private Button btnCargar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnCerrar;

    private StorageService storageService;
    private SaveGameListener listener;

    public interface SaveGameListener {

        void onPartidaCargada(String nombreArchivo);

        void onDialogoCerrado();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        storageService = new StorageService();
        configurarTabla();
        cargarGuardados();
        configurarEventos();
    }

    public void setSaveGameListener(SaveGameListener listener) {
        this.listener = listener;
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreArchivo"));
        colFecha.setCellValueFactory(cellData
                -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedDate()));
        colTurnos.setCellValueFactory(cellData
                -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getElapsedTurns())));
        colBarcosJugador.setCellValueFactory(cellData
                -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getPlayerSunkenShips())));
        colBarcosCPU.setCellValueFactory(cellData
                -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getCpuSunkenShips())));
        colTamaño.setCellValueFactory(cellData
                -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedSize()));

        // Selección única
        tablaGuardados.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void cargarGuardados() {
        List<SaveGameInfo> guardados = storageService.listSavedGames();
        tablaGuardados.getItems().setAll(guardados);
    }

    private void configurarEventos() {
        btnCargar.setOnAction(e -> cargarPartidaSeleccionada());
        btnEliminar.setOnAction(e -> eliminarPartidaSeleccionada());
        btnCerrar.setOnAction(e -> cerrarDialogo());

        // Doble click para cargar
        tablaGuardados.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                cargarPartidaSeleccionada();
            }
        });
    }

    private void cargarPartidaSeleccionada() {
        SaveGameInfo seleccionado = tablaGuardados.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            if (listener != null) {
                listener.onPartidaCargada(seleccionado.getFilename());
            }
            cerrarDialogo();
        } else {
            showAlert("Selección requerida", "Por favor selecciona una partida para cargar.");
        }
    }

    private void eliminarPartidaSeleccionada() {
        SaveGameInfo seleccionado = tablaGuardados.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Eliminar partida");
            confirmacion.setHeaderText("¿Eliminar partida: " + seleccionado.getFilename() + "?");
            confirmacion.setContentText("Esta acción no se puede deshacer.");

            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (storageService.deleteGame(seleccionado.getFilename())) {
                        cargarGuardados(); // Recargar lista
                        showAlert("Éxito", "Partida eliminada correctamente.");
                    } else {
                        showAlert("Error", "No se pudo eliminar la partida.");
                    }
                }
            });
        } else {
            showAlert("Selección requerida", "Por favor selecciona una partida para eliminar.");
        }
    }

    private void cerrarDialogo() {
        if (listener != null) {
            listener.onDialogoCerrado();
        }
        btnCerrar.getScene().getWindow().hide();
    }

    private void showAlert(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
