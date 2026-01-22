package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerDataManager {

    private final Main plugin;
    private final File file;
    private final FileConfiguration config;

    public PlayerDataManager(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveName(UUID uuid, String name) {
        config.set("players." + uuid.toString() + ".name", name);
        saveFile();
    }

    public void saveStats(UUID uuid, int kills, int deaths, int level) {
        String path = "players." + uuid.toString() + ".stats";
        config.set(path + ".kills", kills);
        config.set(path + ".deaths", deaths);
        config.set(path + ".level", level);
        saveFile();
    }

    public int[] loadStats(UUID uuid) {
        String path = "players." + uuid.toString() + ".stats";
        if (!config.contains(path)) return new int[]{0, 0, 1};

        return new int[]{
                config.getInt(path + ".kills"),
                config.getInt(path + ".deaths"),
                config.getInt(path + ".level")
        };
    }

    public void saveBestStreak(UUID uuid, int best) {
        config.set("players." + uuid.toString() + ".stats.best-streak", best);
        saveFile();
    }

    public int loadBestStreak(UUID uuid) {
        return config.getInt("players." + uuid.toString() + ".stats.best-streak", 0);
    }

    public void saveLayout(UUID uuid, String mode, Map<Material, Integer> layout) {
        String path = "players." + uuid.toString() + ".layouts." + mode;
        config.set(path, null);
        for (Map.Entry<Material, Integer> entry : layout.entrySet()) {
            config.set(path + "." + entry.getKey().name(), entry.getValue());
        }
        saveFile();
    }

    public Map<Material, Integer> loadLayout(UUID uuid, String mode) {
        String path = "players." + uuid.toString() + ".layouts." + mode;
        if (!config.contains(path)) return null;

        Map<Material, Integer> layout = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section != null) {
            for (String matName : section.getKeys(false)) {
                try {
                    layout.put(Material.valueOf(matName), section.getInt(matName));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return layout;
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Integer> getTop10(String statKey) {
        Map<String, Integer> dataMap = new HashMap<>();
        ConfigurationSection playersSection = config.getConfigurationSection("players");

        if (playersSection == null) return new LinkedHashMap<>();

        for (String uuidStr : playersSection.getKeys(false)) {
            // Updated Path to match saveStats and saveBestStreak
            int value = config.getInt("players." + uuidStr + ".stats." + statKey, 0);
            String name = config.getString("players." + uuidStr + ".name");

            if (name != null) {
                dataMap.put(name, value);
            }
        }

        return dataMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }
}