package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class onQuit implements Listener {

    private final Main plugin;

    public onQuit(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuitEvent(PlayerQuitEvent e) {
        plugin.getStatsManager().saveAndUnloadPlayer(e.getPlayer());
    }
}