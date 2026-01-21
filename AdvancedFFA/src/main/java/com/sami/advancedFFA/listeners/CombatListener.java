package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;

public class CombatListener implements Listener {

    private final Main plugin;
    private final HashMap<UUID, Long> combatLog = new HashMap<>();
    private final int COMBAT_TIME = 20;

    public CombatListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombatHit(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Player victim = (Player) e.getEntity();
            Player attacker = (Player) e.getDamager();

            tagPlayer(victim);
            tagPlayer(attacker);
        }
    }

    private void tagPlayer(Player player) {
        if (!combatLog.containsKey(player.getUniqueId())) {
            player.sendMessage("§c§lCOMBAT §8» §7You are now in combat! Do not log out.");
        }
        combatLog.put(player.getUniqueId(), System.currentTimeMillis() + (COMBAT_TIME * 1000L));
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (isTagged(p) && !p.isOp()) {
            e.setCancelled(true);
            p.sendMessage("§cYou cannot use commands while in combat!");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (isTagged(p) && !p.isOp()) {
            // Tempban for 30 minutes
            String reason = "§cLogged out during combat!";
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(p.getName(), reason,
                    new java.util.Date(System.currentTimeMillis() + (30 * 60 * 1000L)), "Console");
            p.kickPlayer(reason);
        }
        combatLog.remove(p.getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        combatLog.remove(victim.getUniqueId()); // Clear tag on death

        e.getDrops().clear();
        victim.getInventory().clear();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            victim.spigot().respawn();
            World spawnWorld = Bukkit.getWorld("spawn");
            if (spawnWorld != null) {
                Location spawnLoc = new Location(spawnWorld, 0.5, 0, 0.5, -90, 0);
                victim.teleport(spawnLoc);
            }
        }, 1L);
    }

    private boolean isTagged(Player p) {
        if (!combatLog.containsKey(p.getUniqueId())) return false;
        if (System.currentTimeMillis() > combatLog.get(p.getUniqueId())) {
            combatLog.remove(p.getUniqueId());
            p.sendMessage("§a§lCOMBAT §8» §7You are no longer in combat.");
            return false;
        }
        return true;
    }
}