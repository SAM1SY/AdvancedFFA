package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class SpawnProtectionListener implements Listener {

    private final Main plugin;

    public SpawnProtectionListener(Main plugin) {
        this.plugin = plugin;
    }

    private boolean isSpawnWorld(String worldName) {
        return worldName.equalsIgnoreCase("spawn");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (isSpawnWorld(p.getWorld().getName())) {
            // Allow OPs in Creative to build
            if (p.isOp() && p.getGameMode() == GameMode.CREATIVE) return;

            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (isSpawnWorld(p.getWorld().getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (isSpawnWorld(p.getWorld().getName())) {
            if (p.isOp() && p.getGameMode() == GameMode.CREATIVE) return;

            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        if (isSpawnWorld(e.getEntity().getWorld().getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent e) {
        if (isSpawnWorld(e.getEntity().getWorld().getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent e) {
        if (isSpawnWorld(e.getWorld().getName()) && e.toWeatherState()) {
            e.setCancelled(true);
        }
    }
}