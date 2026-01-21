package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {
    private final Main plugin;
    private final Map<UUID, PlayerStats> statsCache = new HashMap<>();

    public StatsManager(Main plugin) { this.plugin = plugin; }

    public static class PlayerStats {
        public int kills, deaths, level;
        public PlayerStats(int k, int d, int l) { this.kills = k; this.deaths = d; this.level = l; }
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
            // Lvl up every 50 kills
            int newLvl = (s.kills / 50) + 1;
            if (newLvl > s.level) {
                s.level = newLvl;
                p.sendMessage("§b§lLEVEL UP §8» §7You are now Level §f" + s.level);
            }
        }
    }

    public void addDeath(Player p) {
        PlayerStats s = statsCache.get(p.getUniqueId());
        if (s != null) s.deaths++;
    }

    public int getLevel(UUID uuid) { return statsCache.containsKey(uuid) ? statsCache.get(uuid).level : 1; }
    public String getLevelColor(UUID uuid) { return getLevel(uuid) >= 50 ? "§c" : "§7"; }
}