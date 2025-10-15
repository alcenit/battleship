/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.sevices;


import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.model.Configuration;
import com.cenit.battleship.model.SaveGameInfo;
import com.cenit.battleship.model.enums.GamePhase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class StorageService {
  private static final String SAVE_DIR = "saves/";
    private static final String SAVE_EXTENSION = ".bsg"; // BattleShip Game
    private Gson gson;

    public StorageService() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
        crearDirectorioGuardados();
    }

    private void crearDirectorioGuardados() {
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
        } catch (IOException e) {
            System.err.println("Error al crear directorio de guardados: " + e.getMessage());
        }
    }

    // ========== GUARDADO DE PARTIDA ==========

    public boolean guardarPartida(GameController gameController, String nombreArchivo) {
        try {
            GamePhase gameState = crearGameState(gameController);
            String json = gson.toJson(gameState);
            
            Path archivo = Paths.get(SAVE_DIR + nombreArchivo + SAVE_EXTENSION);
            Files.write(archivo, json.getBytes());
            
            System.out.println("Partida guardada: " + archivo.toAbsolutePath());
            return true;
            
        } catch (Exception e) {
            System.err.println("Error al guardar partida: " + e.getMessage());
            return false;
        }
    }

    public boolean guardarPartidaAutomatico(GameController gameController) {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String nombreArchivo = "partida_" + timestamp;
        return guardarPartida(gameController, nombreArchivo);
    }

    // ========== CARGA DE PARTIDA ==========

    public GameController loadGame(String nombreArchivo) {
        try {
            Path archivo = Paths.get(SAVE_DIR + nombreArchivo + SAVE_EXTENSION);
            if (!Files.exists(archivo)) {
                throw new FileNotFoundException("Archivo de guardado no encontrado: " + archivo);
            }

            String json = new String(Files.readAllBytes(archivo));
            GamePhase estadoJuego = gson.fromJson(json, GamePhase.class);
            
            return recrearGameController(estadoJuego);
            
        } catch (Exception e) {
            System.err.println("Error al cargar partida: " + e.getMessage());
            return null;
        }
    }

    // ========== LISTADO Y GESTIÓN DE GUARDADOS ==========

    public List<SaveGameInfo> listarPartidasGuardadas() {
        List<SaveGameInfo> partidas = new ArrayList<>();
        
        try {
            Files.list(Paths.get(SAVE_DIR))
                .filter(path -> path.toString().endsWith(SAVE_EXTENSION))
                .forEach(path -> {
                    SaveGameInfo info = obtenerInfoGuardado(path);
                    if (info != null) {
                        partidas.add(info);
                    }
                });
                
        } catch (IOException e) {
            System.err.println("Error al listar partidas guardadas: " + e.getMessage());
        }
        
        // Ordenar por fecha (más reciente primero)
        partidas.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));
        return partidas;
    }

    private SaveGameInfo obtenerInfoGuardado(Path archivo) {
        try {
            String json = new String(Files.readAllBytes(archivo));
            GamePhase state = gson.fromJson(json, GamePhase.class);
            
            return new SaveGameInfo(
                archivo.getFileName().toString().replace(SAVE_EXTENSION, ""),
                
                state.getFechaGuardado(),
                state.getTurnosTranscurridos(),
                state.getBarcosHundidosJugador(),
                state.getBarcosHundidosCPU(),
                archivo.toFile().length()
            );
            
        } catch (Exception e) {
            System.err.println("Error al obtener info del guardado: " + e.getMessage());
            return null;
        }
    }

    public boolean eliminarPartida(String nombreArchivo) {
        try {
            Path archivo = Paths.get(SAVE_DIR + nombreArchivo + SAVE_EXTENSION);
            return Files.deleteIfExists(archivo);
        } catch (IOException e) {
            System.err.println("Error al eliminar partida: " + e.getMessage());
            return false;
        }
    }

    // ========== SERIALIZACIÓN ==========

    private GamePhase crearGameState(GameController gameController) {
        GamePhase state = new GamePhase();
        
        // Información básica
        state.setFechaGuardado(new Date());
        state.setTurnosTranscurridos(gameController.getTurnosTranscurridos());
        
        // Estado del juego
        state.setTurnoJugador(gameController.isPlayerTurn());
        state.setEstadoJuego(gameController.getGameState());
        
        // Tableros
        state.setTableroJugador(serializarTablero(gameController.getTableroJugador()));
        state.setTableroCPU(serializarTablero(gameController.getTableroCPU()));
        
        // Barcos
        state.setBarcosJugador(serializarBarcos(gameController.getBarcosJugador()));
        state.setBarcosCPU(serializarBarcos(gameController.getBarcosCPU()));
        
        // Habilidades
        state.setHabilidadesJugador(serializarHabilidades(gameController.getHabilidadesJugador()));
        state.setHabilidadesCPU(serializarHabilidades(gameController.getHabilidadesCPU()));
        
        // Barcos hundidos
        state.setBarcosHundidosJugador(new ArrayList<>(gameController.getBarcosHundidosJugador()));
        state.setBarcosHundidosCPU(new ArrayList<>(gameController.getBarcosHundidosCPU()));
        
        // Configuración
        state.setConfiguration(Configuration.getInstance());
        
        return state;
    }

    private GameController recrearGameController(GamePhase estado) {
        GameController gameController = new GameController();
        
        // Restaurar tableros
        deserializarTablero(gameController.getTableroJugador(), estado.getTableroJugador());
        deserializarTablero(gameController.getTableroCPU(), estado.getTableroCPU());
        
        // Restaurar barcos y sus estados
        deserializarBarcos(gameController.getBarcosJugador(), estado.getBarcosJugador());
        deserializarBarcos(gameController.getBarcosCPU(), estado.getBarcosCPU());
        
        // Restaurar habilidades
        deserializarHabilidades(gameController.getHabilidadesJugador(), estado.getHabilidadesJugador());
        deserializarHabilidades(gameController.getHabilidadesCPU(), estado.getHabilidadesCPU());
        
        // Restaurar estado del juego
        gameController.setTurnoJugador(estado.isTurnoJugador());
        gameController.setGameState(estado.getEstadoJuego());
        
        return gameController;
    }

    // ========== MÉTODOS DE SERIALIZACIÓN ESPECÍFICOS ==========

    private TableroState serializarTablero(Tablero tablero) {
        TableroState state = new TableroState();
        state.setTamaño(tablero.getTamaño());
        
        EstadoCasilla[][] estados = new EstadoCasilla[state.getTamaño()][state.getTamaño()];
        for (int i = 0; i < state.getTamaño(); i++) {
            for (int j = 0; j < state.getTamaño(); j++) {
                estados[i][j] = tablero.getCasilla(i, j).getEstado();
            }
        }
        state.setEstadosCasillas(estados);
        
        return state;
    }

    private void deserializarTablero(Tablero tablero, TableroState state) {
        for (int i = 0; i < state.getTamaño(); i++) {
            for (int j = 0; j < state.getTamaño(); j++) {
                tablero.getCasilla(i, j).setEstado(state.getEstadosCasillas()[i][j]);
            }
        }
    }

    private List<BarcoState> serializarBarcos(List<Barco> barcos) {
        List<BarcoState> states = new ArrayList<>();
        for (Barco barco : barcos) {
            BarcoState state = new BarcoState();
            state.setTipo(barco.getTipo());
            state.setImpactosRecibidos(barco.getImpactosRecibidos());
            state.setPosiciones(obtenerPosicionesBarco(barco));
            states.add(state);
        }
        return states;
    }

    private void deserializarBarcos(List<Barco> barcos, List<BarcoState> states) {
        // Reconstruir barcos con sus estados
        for (int i = 0; i < barcos.size(); i++) {
            Barco barco = barcos.get(i);
            BarcoState state = states.get(i);
            
            // Restaurar impactos
            for (int j = 0; j < state.getImpactosRecibidos(); j++) {
                barco.registrarImpacto();
            }
        }
    }

    private List<Coordenada> obtenerPosicionesBarco(Barco barco) {
        List<Coordenada> posiciones = new ArrayList<>();
        for (Casilla casilla : barco.getPosiciones()) {
            // Buscar coordenada de la casilla en el tablero
            // Esto requiere implementación adicional
        }
        return posiciones;
    }

    private HabilidadesState serializarHabilidades(SistemaHabilidades habilidades) {
        HabilidadesState state = new HabilidadesState();
        state.setPuntosHabilidad(habilidades.getPuntosHabilidad());
        state.setHabilidadesDisponibles(new HashMap<>(habilidades.getHabilidadesDisponibles()));
        state.setUsosRestantes(new HashMap<>(habilidades.getUsosRestantes()));
        return state;
    }

    private void deserializarHabilidades(SistemaHabilidades habilidades, HabilidadesState state) {
        habilidades.ganarPuntosHabilidad(state.getPuntosHabilidad());
        // Restaurar usos restantes - requiere modificación en SistemaHabilidades
    }   
}
