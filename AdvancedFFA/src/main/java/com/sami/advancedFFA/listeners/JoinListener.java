package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class JoinListener implements Listener {

    private final Main plugin;

    public JoinListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        plugin.getStatsManager().loadPlayer(player);

        teleportToSpawn(player);
        player.getInventory().clear();
        player.setFoodLevel(20);
        player.setHealth(20);

        int level = plugin.getStatsManager().getLevel(uuid);
        String levelColor = plugin.getStatsManager().getLevelColor(uuid);
        e.setJoinMessage("§8[§a+§8] " + levelColor + "[" + level + "] §7" + player.getName());

        giveLobbyItems(player);
        player.sendTitle("§6§lADVANCED FFA", "§7Welcome to the arena!", 10, 70, 20);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getNametagManager().updateNametag(player);

            for (Player online : Bukkit.getOnlinePlayers()) {
                plugin.getNametagManager().updateNametag(online);
            }
        }, 100L);

        String[] modes = {"Standard", "Speed", "Beast"};
        for (String mode : modes) {
            plugin.getKitManager().getLayout(uuid, mode);
        }
    }

    private void teleportToSpawn(Player player) {
        if (Bukkit.getWorld("spawn") != null) {
            Location spawnLoc = new Location(Bukkit.getWorld("spawn"), 0.5, 1.0, 0.5, -90, 0);
            player.teleport(spawnLoc);
        }
    }

    public void giveLobbyItems(Player p) {
        p.getInventory().clear();
        p.clearActivePotionEffects();
        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta emeraldItemMeta = emerald.getItemMeta();
        ItemStack star = new ItemStack(Material.NETHER_STAR);
        ItemMeta starItemMeta = star.getItemMeta();
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta paperItemMeta = paper.getItemMeta();

        if (emeraldItemMeta != null) {
            emeraldItemMeta.setDisplayName("§a§lLeaderBoard §r§7(Right Click)");
            emerald.setItemMeta(emeraldItemMeta);
        }
        if (starItemMeta != null) {
            starItemMeta.setDisplayName("§b§lSettings §r§7(Right Click)");
            star.setItemMeta(starItemMeta);
        }
        if (paperItemMeta != null) {
            paperItemMeta.setDisplayName("§f§lStats §r§7(Right Click)");
            paper.setItemMeta(paperItemMeta);
        }

        p.getInventory().setItem(0, paper);
        p.getInventory().setItem(4, emerald);
        p.getInventory().setItem(8, star);
    }
}