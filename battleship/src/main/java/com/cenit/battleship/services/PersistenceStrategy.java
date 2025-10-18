/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.services;

import com.cenit.battleship.model.GameState;
import java.util.List;

/**
 * Interfaz que define las operaciones de persistencia para el juego Battleship.
 * Patrón Strategy que permite diferentes implementaciones (JSON, Binario, XML, etc.)
 */
public interface PersistenceStrategy {
    
    /**
     * Guarda el estado del juego en un archivo
     * @param gameState Estado del juego a guardar
     * @param nombreArchivo Nombre del archivo (sin extensión)
     * @return true si se guardó correctamente, false en caso de error
     */
    boolean guardar(GameState gameState, String nombreArchivo);
    
    /**
     * Carga el estado del juego desde un archivo
     * @param nombreArchivo Nombre del archivo (sin extensión)
     * @return Estado del juego cargado, o null si hay error
     */
    GameState cargar(String nombreArchivo);
    
    /**
     * Lista todos los archivos de guardado disponibles
     * @return Lista de nombres de archivos de guardado (sin extensión)
     */
    List<String> listarGuardados();
    
    /**
     * Elimina un archivo de guardado
     * @param nombreArchivo Nombre del archivo a eliminar (sin extensión)
     * @return true si se eliminó correctamente, false en caso de error
     */
    boolean eliminar(String nombreArchivo);
    
    /**
     * Obtiene la extensión de archivo que usa esta estrategia
     * @return Extensión de archivo (ej: ".json", ".dat")
     */
    default String getExtension() {
        return ".save";
    }
    
    /**
     * Verifica si un archivo de guardado existe
     * @param nombreArchivo Nombre del archivo a verificar
     * @return true si el archivo existe, false en caso contrario
     */
    default boolean existeGuardado(String nombreArchivo) {
        List<String> guardados = listarGuardados();
        return guardados.contains(nombreArchivo);
    }
    
    /**
     * Obtiene información básica sobre la estrategia
     * @return Descripción de la estrategia
     */
    default String getDescripcion() {
        return "Estrategia de persistencia genérica";
    }
}