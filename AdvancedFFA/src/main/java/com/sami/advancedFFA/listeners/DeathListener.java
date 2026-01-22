package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DeathListener implements Listener {

    private final Main plugin;

    public DeathListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();

        int lostStreak = plugin.getKitManager().getStreak(victim.getUniqueId());

        if (lostStreak >= 5) {
            e.setDeathMessage("§c§lDEATH §8» §f" + victim.getName() + "§7's streak of §e" + lostStreak + " §7was ended by §f" + (killer != null ? killer.getName() : "Environment"));
        } else {
            e.setDeathMessage(null);
        }

        plugin.getKitManager().resetStreak(victim);
        plugin.getStatsManager().addDeath(victim);

        if (killer != null && !killer.equals(victim)) {
            plugin.getKitManager().addKill(killer);
            plugin.getStatsManager().addKill(killer);

            int killerStreak = plugin.getKitManager().getStreak(killer.getUniqueId());
            killer.sendMessage("§a§lKILL §8» §7You killed §f" + victim.getName() + " §8[§eStreak: " + killerStreak + "§8]");
        }

        e.getDrops().clear();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (victim.isOnline()) {
                victim.spigot().respawn();

                giveLeaderboardItem(victim);

                World spawnWorld = Bukkit.getWorld("spawn");
                if (spawnWorld != null) {
                    Location spawnLoc = new Location(spawnWorld, 0.5, 0, 0.5, -90, 0);
                    victim.teleport(spawnLoc);
                }
            }
        }, 1L);
    }

    private void giveLeaderboardItem(Player p) {
        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta meta = emerald.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a§lLeaderBoard §7(Right Click)");
            emerald.setItemMeta(meta);
        }
        p.getInventory().setItem(4, emerald);
    }
}