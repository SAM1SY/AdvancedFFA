package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {
    private final Main plugin;
    private final Map<UUID, PlayerStats> statsCache = new HashMap<>();

    private final Map<String, Map<String, Integer>> globalLeaderboardCache = new HashMap<>();

    public StatsManager(Main plugin) { this.plugin = plugin; }

    public static class PlayerStats {
        public int kills, deaths, level;
        public PlayerStats(int k, int d, int l) { this.kills = k; this.deaths = d; this.level = l; }
    }

    public void updateGlobalLeaderboards() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            syncOnlinePlayerToData(online);
        }

        String[] statsToUpdate = {"kills", "deaths", "best-streak", "level"};
        for (String key : statsToUpdate) {
            globalLeaderboardCache.put(key, plugin.getPlayerDataManager().getTop10(key));
        }
    }
    private void syncOnlinePlayerToData(Player player) {
        PlayerStats s = statsCache.get(player.getUniqueId());
        if (s != null) {
            plugin.getPlayerDataManager().saveName(player.getUniqueId(), player.getName());
            plugin.getPlayerDataManager().saveStats(player.getUniqueId(), s.kills, s.deaths, s.level);
        }
    }

    public Map<String, Integer> getCachedTop10(String statKey) {
        return globalLeaderboardCache.getOrDefault(statKey, new LinkedHashMap<>());
    }

    public void loadPlayer(Player player) {
        int[] data = plugin.getPlayerDataManager().loadStats(player.getUniqueId());
        statsCache.put(player.getUniqueId(), new PlayerStats(data[0], data[1], data[2]));
    }

    public void saveAndUnloadPlayer(Player player) {
        PlayerStats s = statsCache.get(player.getUniqueId());
        if (s != null) {
            plugin.getPlayerDataManager().saveStats(player.getUniqueId(), s.kills, s.deaths, s.level);
            statsCache.remove(player.getUniqueId());
        }
    }

    public void addKill(Player p) {
        PlayerStats s = statsCache.get(p.getUniqueId());
        if (s != null) {
            s.kills++;
            int newLvl = (s.kills / 50) + 1;
            if (newLvl > s.level) {
                s.level = newLvl;
                p.sendMessage("§b§lLEVEL UP §8» §7You are now Level §f" + s.level);
            }
        }
    }

    public String getLevelColor(UUID uuid) {
        int level = getLevel(uuid);
        if (level >= 100) return "§4";
        if (level >= 50) return "§c";
        if (level >= 10) return "§e";
        return "§7";
    }

    public void addDeath(Player p) {
        PlayerStats s = statsCache.get(p.getUniqueId());
        if (s != null) s.deaths++;
    }

    public int getLevel(UUID uuid) { return statsCache.containsKey(uuid) ? statsCache.get(uuid).level : 1; }
}