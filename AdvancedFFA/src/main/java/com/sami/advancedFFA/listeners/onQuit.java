package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class onQuit implements Listener {

    private final Main plugin;

    public onQuit(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        int level = plugin.getStatsManager().getLevel(uuid);
        String levelColor = plugin.getStatsManager().getLevelColor(uuid);

        e.setQuitMessage("§8[§c-§8] " + levelColor + "[" + level + "] §7" + player.getName());

        plugin.getNametagManager().removeNametag(e.getPlayer());
        plugin.getStatsManager().saveAndUnloadPlayer(player);
        player.removeMetadata("selected_kit", plugin);
    }
}