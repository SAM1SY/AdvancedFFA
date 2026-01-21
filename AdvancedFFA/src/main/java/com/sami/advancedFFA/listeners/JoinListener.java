package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.models.UserStats;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final Main plugin;

    public JoinListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // 1. Force Teleport to Spawn
        World spawnWorld = Bukkit.getWorld("spawn");
        if (spawnWorld != null) {
            // Logic: 0.5 0 0.5 | Yaw -90 (Facing West) | Pitch 0
            Location spawnLoc = new Location(spawnWorld, 0.5, 0, 0.5, -90f, 0f);
            p.teleport(spawnLoc);
        }

        // 2. Delayed Welcome Message (Wait 5 ticks for Data to be ready in RAM)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            UserStats stats = plugin.getStatsManager().getStats(p.getUniqueId());

            p.sendMessage("§8§m---------------------------------");
            p.sendMessage("§6§lSERVER §8» §7Welcome back, §f" + p.getName());

            if (stats != null) {
                p.sendMessage("§7Level: §e" + stats.getLevel());
                p.sendMessage("§7XP: §6" + stats.getXp());
            }

            p.sendMessage("§8§m---------------------------------");

            // Send a clear title to guide the player
            p.sendTitle("§6§lSERVER FFA", "§7Interact with an NPC to start!", 10, 40, 10);
        }, 5L);
    }
}