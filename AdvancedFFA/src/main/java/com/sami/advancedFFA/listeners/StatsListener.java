package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class StatsListener implements Listener {

    private final Main plugin;

    public StatsListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDataLoad(PlayerJoinEvent e) {
        plugin.getStatsManager().loadPlayer(e.getPlayer());
    }

    @EventHandler
    public void onDataSave(@NotNull PlayerQuitEvent e) {
        plugin.getStatsManager().saveAndUnloadPlayer(e.getPlayer());
    }
}