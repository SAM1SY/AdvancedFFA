package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.models.UserStats;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    private final Main plugin;
    private final Map<UUID, UserStats> statsCache = new HashMap<>();

    public StatsManager(Main plugin) {
        this.plugin = plugin;
    }

    // DB -> RAM
    public void loadPlayer(Player player) {
        UserStats stats = plugin.getDatabaseManager().loadPlayerStats(player.getUniqueId());
        statsCache.put(player.getUniqueId(), stats);
    }

    // RAM -> DB and remove
    public void saveAndUnloadPlayer(Player player) {
        UserStats stats = statsCache.get(player.getUniqueId());
        if (stats != null) {
            plugin.getDatabaseManager().savePlayerStats(stats);
            statsCache.remove(player.getUniqueId());
        }
    }

    public UserStats getStats(UUID uuid) {
        return statsCache.get(uuid);
    }
}