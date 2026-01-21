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

import java.util.Map;
import java.util.UUID;

public class JoinListener implements Listener {

    private final Main plugin;
    public JoinListener(Main plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        teleportToSpawn(player);

        player.getInventory().clear();
        player.setFoodLevel(20);
        player.setHealth(20);

        giveLobbyItems(player);

        plugin.getStatsManager().loadPlayer(player);

        player.sendTitle("§6§lADVANCED FFA", "§7Welcome to the arena!", 10, 70, 20);

        String levelColor = plugin.getStatsManager().getLevelColor(uuid);
        int level = plugin.getStatsManager().getLevel(uuid);
        e.setJoinMessage("§8[§a+§8] " + levelColor + "[" + level + "] §7" + player.getName());

        String[] modes = {"Standard", "Speed", "Beast"};
        for (String mode : modes) {
            Map<Material, Integer> savedLayout = plugin.getPlayerDataManager().loadLayout(uuid, mode);

            if (savedLayout != null && !savedLayout.isEmpty()) {
                plugin.getKitManager().setLayout(uuid, mode, savedLayout);
            } else {
                plugin.getKitManager().setLayout(uuid, mode, plugin.getKitManager().getDefaultLayout());
            }
        }

    }

    private void teleportToSpawn(Player player) {
        if (Bukkit.getWorld("spawn") != null) {
            Location spawnLoc = new Location(Bukkit.getWorld("spawn"), 0.5, 0.0, 0.5, -90, 0);
            player.teleport(spawnLoc);
        } else {
            plugin.getLogger().warning("Could not teleport " + player.getName() + " - World 'spawn' not found!");
        }
    }

    public void giveLobbyItems(Player p) {
        p.getInventory().clear();
        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta meta = emerald.getItemMeta();
        meta.setDisplayName("§a§lLeaderBoard §7(Right Click)");
        emerald.setItemMeta(meta);

        p.getInventory().setItem(4, emerald);
    }
}