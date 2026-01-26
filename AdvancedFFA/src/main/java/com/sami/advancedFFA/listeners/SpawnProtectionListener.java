package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
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
        if (p.isOp() && p.getGameMode() == GameMode.CREATIVE) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        if (isSpawnWorld(e.getPlayer().getWorld().getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() && p.getGameMode() == GameMode.CREATIVE) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (isSpawnWorld(e.getPlayer().getWorld().getName())) {
            if (e.getPlayer().getY() <= -40) {
                Location spawnLoc = new Location(Bukkit.getWorld("spawn"), 0.5, 1.0, 0.5, -90, 0);
                e.getPlayer().teleport(spawnLoc);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
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