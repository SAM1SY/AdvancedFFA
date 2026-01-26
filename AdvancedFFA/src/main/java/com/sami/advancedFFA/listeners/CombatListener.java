package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class CombatListener implements Listener {

    private final Main plugin;
    private final HashMap<UUID, Long> combatLog = new HashMap<>();
    private final HashMap<UUID, UUID> lastAttacker = new HashMap<>();
    private final int COMBAT_TIME = 20;

    public CombatListener(Main plugin) {
        this.plugin = plugin;
        startActionBarTask();
    }

    private void startActionBarTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Iterator<Map.Entry<UUID, Long>> iterator = combatLog.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<UUID, Long> entry = iterator.next();
                    Player player = Bukkit.getPlayer(entry.getKey());

                    if (player == null || !player.isOnline()) {
                        iterator.remove();
                        continue;
                    }

                    long remainingMillis = entry.getValue() - now;

                    if (remainingMillis <= 0) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§a§lNo longer in combat"));
                        player.sendMessage("§a§lCOMBAT §8» §7You are no longer in combat.");
                        iterator.remove();
                    } else {
                        int seconds = (int) Math.ceil(remainingMillis / 1000.0);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                new TextComponent("§c§lCombat Tag §8» §f" + seconds + "s remaining"));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombatHit(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Player victim = (Player) e.getEntity();
            Player attacker = (Player) e.getDamager();

            if (victim.isInvulnerable() || attacker.isInvulnerable()) { e.setCancelled(true); return; }

            if (victim.getAllowFlight()) {
                victim.setAllowFlight(false);
                victim.setFlying(false);
            }
            if (attacker.getAllowFlight()) {
                attacker.setAllowFlight(false);
                attacker.setFlying(false);
            }

            tagPlayer(victim);
            tagPlayer(attacker);

            lastAttacker.put(victim.getUniqueId(), attacker.getUniqueId());
        }
    }

    private void tagPlayer(Player player) {
        if (!combatLog.containsKey(player.getUniqueId())) {
            player.sendMessage("§c§lCOMBAT §8» §7You are now in combat! Do not log out.");
        }
        combatLog.put(player.getUniqueId(), System.currentTimeMillis() + (COMBAT_TIME * 1000L));
    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
        e.getEntity().setFoodLevel(20);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (isTagged(p) && !p.isOp()) {
            String msg = e.getMessage().toLowerCase();
            if (msg.startsWith("/kit") || msg.startsWith("/spawn")) {
                e.setCancelled(true);
                p.sendMessage("§cYou cannot use that command while in combat!");
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        UUID victimId = p.getUniqueId();

        if (isTagged(p) && !p.isOp()) {
            UUID attackerId = lastAttacker.get(victimId);

            if (attackerId != null) {
                Player attacker = Bukkit.getPlayer(attackerId);
                if (attacker != null && attacker.isOnline()) {
                    plugin.getStatsManager().addKill(attacker);
                    attacker.sendMessage("§a§lCOMBAT §8» §f" + p.getName() + " §7logged out! You received the kill.");
                }
            }

            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(p.getName(), "§cLogged out during combat!",
                    new java.util.Date(System.currentTimeMillis() + (30 * 60 * 1000L)), "Console");

            p.setHealth(0);
        }

        combatLog.remove(victimId);
        lastAttacker.remove(victimId);
    }

    public boolean isTagged(Player p) {
        Long time = combatLog.get(p.getUniqueId());
        return time != null && System.currentTimeMillis() < time;
    }

    @EventHandler
    public void onDeath(org.bukkit.event.entity.PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (combatLog.containsKey(p.getUniqueId())) {
            removeTag(p.getUniqueId());
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§a§lCombat ended by death"));
        }
    }

    public void removeTag(UUID uuid) {
        combatLog.remove(uuid);
    }
}