package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LeaderboardManager {
    private final Main plugin;

    public LeaderboardManager(Main plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskLater(plugin, this::updateHolograms, 60L);
        startUpdateTask();
    }

    private void startUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            plugin.getStatsManager().updateGlobalLeaderboards();
            updateHolograms();
        }, 6000L, 6000L);
    }

    public void updateHolograms() {
        double bottomY = -3.0;
        double spawnY = bottomY + 4.0;

        setupBoard("top-levels", "§b§lTOP 10 LEVELS", "level",
                new Location(Bukkit.getWorld("spawn"), 30, spawnY, -14.5, 25, 0));

        setupBoard("top-kills", "§6§lTOP 10 KILLS", "kills",
                new Location(Bukkit.getWorld("spawn"), 37.5, spawnY, -6.5, 64, 0));

        setupBoard("top-streaks", "§e§lTOP 10 STREAKS", "best_streak",
                new Location(Bukkit.getWorld("spawn"), 37.5, spawnY, 7.5, 115, 0));

        setupBoard("top-deaths", "§c§lTOP 10 DEATHS", "deaths",
                new Location(Bukkit.getWorld("spawn"), 30, spawnY, 14.5, 153, 0));
    }

    private void setupBoard(String id, String title, String statKey, Location loc) {
        List<String> lines = new ArrayList<>();

        lines.add("§f§l§n" + title);
        lines.add("§8§l§m-----------------------");

        Map<String, Integer> data = plugin.getStatsManager().getCachedTop10(statKey);
        int rank = 1;

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            if (rank > 10) break;

            String color = (rank == 1) ? "§6§l" : (rank == 2) ? "§f§l" : (rank == 3) ? "§e§l" : "§7§l";

            lines.add(color + "#" + rank + " §f§l" + entry.getKey().toUpperCase() + " §8§l- " + color + entry.getValue());
            rank++;
        }

        while (rank <= 10) {
            lines.add("§8§l#" + rank + " ---");
            rank++;
        }

        Hologram holo = DHAPI.getHologram(id);
        if (holo == null) {
            DHAPI.createHologram(id, loc, lines);
        } else {
            DHAPI.setHologramLines(holo, lines);
            DHAPI.moveHologram(holo, loc);
        }
    }
}