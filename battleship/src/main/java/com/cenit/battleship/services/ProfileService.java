/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.services;



import com.cenit.battleship.model.PlayerProfile;
import com.cenit.battleship.model.GameStatistics;
import com.cenit.battleship.model.enums.Achievement;
import com.cenit.battleship.model.GameConfiguration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar perfiles de jugadores
 * Maneja creación, carga, guardado y eliminación de perfiles
 */
public class ProfileService {
    private static final String PROFILES_DIR = "profiles/";
    private static final String PROFILE_EXTENSION = ".profile";
    private static final String BACKUP_EXTENSION = ".backup";
    
    private Gson gson;
    private Map<String, PlayerProfile> loadedProfiles;
    private PlayerProfile currentProfile;

    public ProfileService() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
        this.loadedProfiles = new HashMap<>();
        createProfilesDirectory();
        loadAllProfiles();
    }

    // ========== INICIALIZACIÓN Y CONFIGURACIÓN ==========

    /**
     * Crea el directorio de perfiles si no existe
     */
    private void createProfilesDirectory() {
        try {
            Files.createDirectories(Paths.get(PROFILES_DIR));
            System.out.println("✅ Directorio de perfiles creado: " + PROFILES_DIR);
        } catch (IOException e) {
            System.err.println("❌ Error al crear directorio de perfiles: " + e.getMessage());
        }
    }

    /**
     * Carga todos los perfiles del directorio
     */
    private void loadAllProfiles() {
        try {
            if (!Files.exists(Paths.get(PROFILES_DIR))) {
                System.out.println("ℹ️  Directorio de perfiles no existe, se creará al guardar el primer perfil");
                return;
            }

            List<Path> profileFiles = Files.list(Paths.get(PROFILES_DIR))
                .filter(path -> path.toString().endsWith(PROFILE_EXTENSION))
                .collect(Collectors.toList());

            System.out.println("📁 Encontrados " + profileFiles.size() + " perfiles para cargar");

            for (Path file : profileFiles) {
                try {
                    PlayerProfile profile = loadProfileFromFile(file);
                    if (profile != null) {
                        loadedProfiles.put(profile.getPlayerName(), profile);
                        System.out.println("✅ Perfil cargado: " + profile.getPlayerName());
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error cargando perfil " + file.getFileName() + ": " + e.getMessage());
                }
            }

            System.out.println("🎯 Total de perfiles cargados: " + loadedProfiles.size());

        } catch (IOException e) {
            System.err.println("❌ Error al listar perfiles: " + e.getMessage());
        }
    }

    // ========== GESTIÓN DE PERFILES ==========

    /**
     * Crea un nuevo perfil de jugador
     */
    public PlayerProfile createProfile(String playerName) {
        return createProfile(playerName, "🚢");
    }

    /**
     * Crea un nuevo perfil con avatar personalizado
     */
    public PlayerProfile createProfile(String playerName, String avatar) {
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
        }

        String trimmedName = playerName.trim();
        
        if (loadedProfiles.containsKey(trimmedName)) {
            throw new IllegalArgumentException("Ya existe un perfil con el nombre: " + trimmedName);
        }

        if (!isValidPlayerName(trimmedName)) {
            throw new IllegalArgumentException("Nombre de jugador inválido: " + trimmedName);
        }

        PlayerProfile profile = new PlayerProfile(trimmedName, avatar);
        loadedProfiles.put(trimmedName, profile);
        currentProfile = profile;

        // Guardar inmediatamente
        saveProfile(profile);

        System.out.println("🆕 Nuevo perfil creado: " + trimmedName);
        return profile;
    }

    /**
     * Valida un nombre de jugador
     */
    private boolean isValidPlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        if (name.length() < 2 || name.length() > 20) {
            return false;
        }

        // Solo letras, números, espacios y algunos caracteres especiales
        return name.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 _.-]+$");
    }

    /**
     * Obtiene un perfil por nombre
     */
    public PlayerProfile getProfile(String playerName) {
        if (playerName == null) {
            return null;
        }
        return loadedProfiles.get(playerName.trim());
    }

    /**
     * Verifica si existe un perfil
     */
    public boolean profileExists(String playerName) {
        return playerName != null && loadedProfiles.containsKey(playerName.trim());
    }

    /**
     * Obtiene todos los perfiles
     */
    public List<PlayerProfile> getAllProfiles() {
        return new ArrayList<>(loadedProfiles.values());
    }

    /**
     * Obtiene el perfil actualmente activo
     */
    public PlayerProfile getCurrentProfile() {
        if (currentProfile == null) {
            // Intentar cargar el perfil por defecto de la configuración
            String defaultPlayerName = GameConfiguration.getInstance().getPlayerName();
            if (defaultPlayerName != null && profileExists(defaultPlayerName)) {
                currentProfile = getProfile(defaultPlayerName);
            }
        }
        return currentProfile;
    }

    /**
     * Establece el perfil actual
     */
    public void setCurrentProfile(String playerName) {
        if (playerName != null && profileExists(playerName)) {
            currentProfile = getProfile(playerName);
            // Actualizar configuración global
            GameConfiguration.getInstance().setPlayerName(playerName);
            System.out.println("👤 Perfil actual establecido: " + playerName);
        } else {
            System.err.println("❌ No se pudo establecer el perfil: " + playerName);
        }
    }

    /**
     * Establece el perfil actual directamente
     */
    public void setCurrentProfile(PlayerProfile profile) {
        if (profile != null) {
            currentProfile = profile;
            loadedProfiles.put(profile.getPlayerName(), profile);
            GameConfiguration.getInstance().setPlayerName(profile.getPlayerName());
            System.out.println("👤 Perfil actual establecido: " + profile.getPlayerName());
        }
    }

    // ========== ELIMINACIÓN Y MANTENIMIENTO ==========

    /**
     * Elimina un perfil
     */
    public boolean deleteProfile(String playerName) {
        if (playerName == null || !profileExists(playerName)) {
            System.err.println("❌ Perfil no encontrado: " + playerName);
            return false;
        }

        try {
            // Eliminar del mapa
            PlayerProfile removedProfile = loadedProfiles.remove(playerName);
            
            // Eliminar archivo
            Path file = Paths.get(PROFILES_DIR + playerName + PROFILE_EXTENSION);
            boolean deleted = Files.deleteIfExists(file);
            
            // Si el perfil eliminado era el actual, limpiar referencia
            if (currentProfile != null && currentProfile.getPlayerName().equals(playerName)) {
                currentProfile = null;
            }

            if (deleted) {
                System.out.println("🗑️  Perfil eliminado: " + playerName);
                return true;
            } else {
                System.err.println("❌ No se pudo eliminar el archivo del perfil: " + playerName);
                return false;
            }

        } catch (IOException e) {
            System.err.println("❌ Error al eliminar perfil " + playerName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina todos los perfiles (¡Peligroso!)
     */
    public void deleteAllProfiles() {
        List<String> profileNames = new ArrayList<>(loadedProfiles.keySet());
        int deletedCount = 0;
        
        for (String profileName : profileNames) {
            if (deleteProfile(profileName)) {
                deletedCount++;
            }
        }
        
        System.out.println("🗑️  Eliminados " + deletedCount + " de " + profileNames.size() + " perfiles");
    }

    /**
     * Crea una copia de seguridad de un perfil
     */
    public boolean backupProfile(String playerName) {
        if (!profileExists(playerName)) {
            return false;
        }

        try {
            Path originalFile = Paths.get(PROFILES_DIR + playerName + PROFILE_EXTENSION);
            Path backupFile = Paths.get(PROFILES_DIR + playerName + BACKUP_EXTENSION);
            
            Files.copy(originalFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("💾 Copia de seguridad creada: " + backupFile.getFileName());
            return true;
            
        } catch (IOException e) {
            System.err.println("❌ Error al crear backup: " + e.getMessage());
            return false;
        }
    }

    /**
     * Restaura un perfil desde backup
     */
    public boolean restoreProfileFromBackup(String playerName) {
        try {
            Path backupFile = Paths.get(PROFILES_DIR + playerName + BACKUP_EXTENSION);
            if (!Files.exists(backupFile)) {
                System.err.println("❌ Backup no encontrado: " + playerName);
                return false;
            }

            PlayerProfile profile = loadProfileFromFile(backupFile);
            if (profile != null) {
                loadedProfiles.put(playerName, profile);
                saveProfile(profile); // Guardar como archivo normal
                System.out.println("🔄 Perfil restaurado desde backup: " + playerName);
                return true;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al restaurar backup: " + e.getMessage());
        }
        return false;
    }

    // ========== PERSISTENCIA ==========

    /**
     * Guarda un perfil en archivo
     */
    public void saveProfile(PlayerProfile profile) {
        if (profile == null) {
            System.err.println("❌ Intento de guardar perfil nulo");
            return;
        }

        try {
            String json = gson.toJson(profile);
            Path file = Paths.get(PROFILES_DIR + profile.getPlayerName() + PROFILE_EXTENSION);
            Files.write(file, json.getBytes());
            
            System.out.println("💾 Perfil guardado: " + profile.getPlayerName());
            
        } catch (IOException e) {
            System.err.println("❌ Error al guardar perfil " + profile.getPlayerName() + ": " + e.getMessage());
        }
    }

    /**
     * Guarda todos los perfiles cargados
     */
    public void saveAllProfiles() {
        int savedCount = 0;
        for (PlayerProfile profile : loadedProfiles.values()) {
            saveProfile(profile);
            savedCount++;
        }
        System.out.println("💾 Guardados " + savedCount + " perfiles");
    }

    /**
     * Carga un perfil desde archivo
     */
    private PlayerProfile loadProfileFromFile(Path file) {
        try {
            String json = new String(Files.readAllBytes(file));
            PlayerProfile profile = gson.fromJson(json, PlayerProfile.class);
            
            // Validar que el perfil cargado sea válido
            if (profile != null && profile.getPlayerName() != null) {
                return profile;
            } else {
                System.err.println("❌ Perfil inválido en archivo: " + file.getFileName());
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error cargando perfil desde " + file.getFileName() + ": " + e.getMessage());
            return null;
        }
    }

    // ========== RANKING Y ESTADÍSTICAS ==========

    /**
     * Obtiene ranking por puntos
     */
    public List<PlayerProfile> getRankingByPoints() {
        return loadedProfiles.values().stream()
            .sorted((a, b) -> Integer.compare(b.getTotalScore(), a.getTotalScore()))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene ranking por nivel
     */
    public List<PlayerProfile> getRankingByLevel() {
        return loadedProfiles.values().stream()
            .sorted((a, b) -> Integer.compare(b.getCurrentLevel(), a.getCurrentLevel()))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene ranking por porcentaje de victorias
     */
    public List<PlayerProfile> getRankingByWinPercentage() {
        return loadedProfiles.values().stream()
            .sorted((a, b) -> Double.compare(
                b.getStatistics().getWinPercentage(), 
                a.getStatistics().getWinPercentage()
            ))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene ranking por logros desbloqueados
     */
    public List<PlayerProfile> getRankingByAchievements() {
        return loadedProfiles.values().stream()
            .sorted((a, b) -> Integer.compare(
                b.getUnlockedAchievements().size(),
                a.getUnlockedAchievements().size()
            ))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene la posición en el ranking de un jugador
     */
    public int getPlayerRank(String playerName, String rankingType) {
        if (!profileExists(playerName)) {
            return -1;
        }

        List<PlayerProfile> ranking;
        switch (rankingType.toUpperCase()) {
            case "POINTS":
                ranking = getRankingByPoints();
                break;
            case "LEVEL":
                ranking = getRankingByLevel();
                break;
            case "WINS":
                ranking = getRankingByWinPercentage();
                break;
            case "ACHIEVEMENTS":
                ranking = getRankingByAchievements();
                break;
            default:
                ranking = getRankingByPoints();
        }

        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).getPlayerName().equals(playerName)) {
                return i + 1;
            }
        }
        return -1;
    }

    // ========== ESTADÍSTICAS GLOBALES ==========

    /**
     * Obtiene estadísticas globales de todos los perfiles
     */
    public GameStatistics getGlobalStatistics() {
        GameStatistics global = new GameStatistics();
        for (PlayerProfile profile : loadedProfiles.values()) {
            global.merge(profile.getStatistics());
        }
        return global;
    }

    /**
     * Obtiene conteo de logros desbloqueados
     */
    public Map<Achievement, Integer> getAchievementUnlocks() {
        Map<Achievement, Integer> unlocks = new HashMap<>();
        
        // Inicializar contadores
        for (Achievement achievement : Achievement.values()) {
            unlocks.put(achievement, 0);
        }

        // Contar desbloqueos
        for (PlayerProfile profile : loadedProfiles.values()) {
            for (Achievement achievement : profile.getUnlockedAchievements()) {
                unlocks.put(achievement, unlocks.get(achievement) + 1);
            }
        }

        return unlocks;
    }

    /**
     * Obtiene el logro más común
     */
    public Achievement getMostCommonAchievement() {
        Map<Achievement, Integer> unlocks = getAchievementUnlocks();
        return unlocks.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(Achievement.FIRST_BLOOD);
    }

    /**
     * Obtiene el logro más raro
     */
    public Achievement getRarestAchievement() {
        Map<Achievement, Integer> unlocks = getAchievementUnlocks();
        return unlocks.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(Achievement.PERFECT_ACCURACY);
    }

    // ========== INFORMACIÓN DEL SERVICIO ==========

    /**
     * Obtiene información del servicio
     */
    public String getServiceInfo() {
        return String.format(
            "📊 Profile Service Info:\n" +
            "👤 Perfiles cargados: %d\n" +
            "💾 Directorio: %s\n" +
            "🎯 Perfil actual: %s\n" +
            "🏆 Jugador top: %s",
            loadedProfiles.size(),
            PROFILES_DIR,
            currentProfile != null ? currentProfile.getPlayerName() : "Ninguno",
            getTopPlayer()
        );
    }

    /**
     * Obtiene el jugador con más puntos
     */
    private String getTopPlayer() {
        List<PlayerProfile> ranking = getRankingByPoints();
        return ranking.isEmpty() ? "Ninguno" : ranking.get(0).getPlayerName();
    }

    /**
     * Limpia recursos del servicio
     */
    public void cleanup() {
        saveAllProfiles();
        loadedProfiles.clear();
        currentProfile = null;
        System.out.println("🧹 ProfileService limpiado");
    }
}