package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.models.Guild;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

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

            // --- Guild System Integration ---
            Guild g = plugin.getGuildManager().getGuild(killer.getUniqueId());
            if (g != null) {
                double oldCoins = g.getCoins();

                double multiplier = g.calculateMultiplier();
                int pointsToAdd = (int) (1 * multiplier);

                g.addPoints(pointsToAdd);

                // Notify if a milestone was hit
                if (g.getCoins() > oldCoins) {
                    plugin.getGuildManager().broadcast(g, "§6§l+1 GUILD COIN §7(Reached " + g.getPoints() + " points!)");
                    for (UUID uuid : g.getMembers()) {
                        Player m = Bukkit.getPlayer(uuid);
                        if (m != null) m.playSound(m.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 2f);
                    }
                }
            }
        }

        e.setDeathMessage(null);

        // --- Kill/Death Messages ---
        handleMessages(victim, killer, victimStreak);

        if (killer != null && !killer.equals(victim)) {
            int killerStreak = plugin.getStatsManager().getStreak(killer.getUniqueId());
            killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            killer.playSound(killer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1.2f);
            killer.sendMessage("§a§lKILL §8» §7You killed §f" + victim.getName() + " §8[§eStreak: " + killerStreak + "§8]");
        }

        e.getDrops().clear();

        // --- Respawn Logic ---
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
                }
            }
        }, 1L);
    }

    private void handleMessages(Player victim, Player killer, int streak) {
        String killerName = (killer != null) ? killer.getName() : "Environment";
        String streakMsg = (streak >= 5) ?
                " §c§lSTREAK ENDED §8» §f" + victim.getName() + "§7's streak of §e" + streak + " §7was ended by §f" + killerName :
                " §c§lDEATH §8» §f" + victim.getName() + " §7was killed by §f" + killerName;

        for (Player online : Bukkit.getOnlinePlayers()) {
            int mode = plugin.getSettingsMenu().getSetting(online, "kill_msg_mode", 0);
            if (mode == 0) online.sendMessage(streakMsg);
            else if (mode == 1 && (online.equals(victim) || (killer != null && online.equals(killer)))) {
                online.sendMessage(streakMsg);
            }
        }
    }

    private void giveSpawnItem(Player p) {
        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta em = emerald.getItemMeta();
        if (em != null) { em.setDisplayName("§a§lLeaderBoard §7(Right Click)"); emerald.setItemMeta(em); }

        ItemStack star = new ItemStack(Material.NETHER_STAR);
        ItemMeta sm = star.getItemMeta();
        if (sm != null) { sm.setDisplayName("§b§lSettings §7(Right Click)"); star.setItemMeta(sm); }

        p.getInventory().setItem(4, emerald);
        p.getInventory().setItem(8, star);
    }
}