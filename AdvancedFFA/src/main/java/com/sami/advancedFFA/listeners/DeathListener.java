package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import org.bukkit.*;
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

        int victimStreak = plugin.getStatsManager().getStreak(victim.getUniqueId());
        plugin.getStatsManager().addDeath(victim);

        if (killer != null && !killer.equals(victim)) {
            plugin.getStatsManager().addKill(killer);
        }

        e.setDeathMessage(null);

        String killerName = (killer != null) ? killer.getName() : "Environment";
        String streakMsg = (victimStreak >= 5) ?
                " §c§lSTREAK ENDED §8» §f" + victim.getName() + "§7's streak of §e" + victimStreak + " §7was ended by §f" + killerName :
                " §c§lDEATH §8» §f" + victim.getName() + " §7was killed by §f" + killerName;

        for (Player online : Bukkit.getOnlinePlayers()) {
            int mode = plugin.getSettingsMenu().getSetting(online, "kill_msg_mode", 0);

            if (mode == 0) {
                online.sendMessage(streakMsg);
            }
            else if (mode == 1) {
                if (online.equals(victim) || (killer != null && online.equals(killer))) {
                    online.sendMessage(streakMsg);
                }
            }
        }

        if (killer != null && !killer.equals(victim)) {
            int killerStreak = plugin.getStatsManager().getStreak(killer.getUniqueId());
            killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            killer.playSound(killer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1.2f);
            killer.sendMessage("§a§lKILL §8» §7You killed §f" + victim.getName() + " §8[§eStreak: " + killerStreak + "§8]");
        }

        e.getDrops().clear();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (victim.isOnline()) {
                victim.spigot().respawn();
                victim.getInventory().clear();
                giveSpawnItem(victim);

                World spawnWorld = Bukkit.getWorld("spawn");
                if (spawnWorld != null) {
                    Location spawnLoc = new Location(spawnWorld, 0.5, 0.0, 0.5, -90, 0);
                    victim.teleport(spawnLoc);
                    victim.clearActivePotionEffects();
                    victim.setHealth(20.0);
                } else {
                    plugin.getLogger().warning("Spawn world not found! Teleport failed for " + victim.getName());
                }
            }
        }, 1L);
    }

    private void giveSpawnItem(Player p) {
        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta emeraldMeta = emerald.getItemMeta();

        ItemStack star = new ItemStack(Material.NETHER_STAR);
        ItemMeta starMeta = star.getItemMeta();

        if (emeraldMeta != null) {
            emeraldMeta.setDisplayName("§a§lLeaderBoard §7(Right Click)");
            emerald.setItemMeta(emeraldMeta);
        }
        if (starMeta != null) {
            starMeta.setDisplayName("§b§lSettings §7(Right Click)");
            star.setItemMeta(starMeta);
        }

        p.getInventory().setItem(4, emerald);
        p.getInventory().setItem(8, star);
    }
}