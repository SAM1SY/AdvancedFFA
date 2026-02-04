package com.sami.advancedFFA.commands;

import com.sami.advancedFFA.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class SpawnCommand implements CommandExecutor {
    private final Main plugin;
    private final HashMap<UUID, Integer> tasks = new HashMap<>();

    public SpawnCommand(Main plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
        if (!sender.hasPermission("ffa.spawn.use")) return true;

        if (plugin.getCombatListener().isTagged(p)) {
            p.sendMessage("§c§lERROR §8» §7You cannot go to spawn while in combat!");
            return true;
        }

        if (tasks.containsKey(p.getUniqueId())) {
            p.sendMessage("§c§lERROR §8» §7Teleportation is already in progress.");
            return true;
        }

        Location startLoc = p.getLocation().clone();

        p.playSound(p.getLocation(), Sound.BLOCK_BELL_RESONATE, 1.0f, 0.7f);

        BukkitRunnable teleportTask = new BukkitRunnable() {
            int seconds = 3;

            @Override
            public void run() {
                if (p.getLocation().distanceSquared(startLoc) > 0.1) {
                    p.sendMessage("§c§lERROR §8» §7Teleport cancelled! You moved.");
                    p.stopSound(Sound.BLOCK_BELL_RESONATE);
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, .9f);
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§4§lYou moved!"));
                    cancelTeleport(p.getUniqueId());
                    this.cancel();
                    return;
                }

                if (seconds > 0) {
                    sendProgressBar(p, seconds);
                    seconds--;
                } else {
                    executeTeleport(p);
                    cancelTeleport(p.getUniqueId());
                    this.cancel();
                }
            }
        };

        teleportTask.runTaskTimer(plugin, 0L, 20L);
        tasks.put(p.getUniqueId(), teleportTask.getTaskId());

        return true;
    }

    private void executeTeleport(Player p) {
        Location spawn = new Location(Bukkit.getWorld("spawn"), 0.5, 0, 0.5, -90, 0);
        p.teleport(spawn);
        p.getInventory().clear();
        p.clearActivePotionEffects();
        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta emeraldItemMeta = emerald.getItemMeta();
        ItemStack star = new ItemStack(Material.NETHER_STAR);
        ItemMeta starItemMeta = star.getItemMeta();
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta pm = paper.getItemMeta();
        if (pm != null) { pm.setDisplayName("§f§lStats §r§7(Right Click)"); paper.setItemMeta(pm); }

        if (emeraldItemMeta != null) {
            emeraldItemMeta.setDisplayName("§a§lLeaderBoard §7(Right Click)");
            emerald.setItemMeta(emeraldItemMeta);
        }
        if (starItemMeta != null) {
            starItemMeta.setDisplayName("§b§lSettings §7(Right Click)");
            star.setItemMeta(starItemMeta);
        }

        p.getInventory().setItem(0, paper);
        p.getInventory().setItem(4, emerald);
        p.getInventory().setItem(8, star);

        p.sendMessage("§a§lSERVER §8» §7Teleported to spawn.");
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§a§lTELEPORTED!"));
    }

    private void sendProgressBar(Player p, int secondsLeft) {
        String bar;
        if (secondsLeft == 3) bar = "§eTeleporting: §7§l■■■";
        else if (secondsLeft == 2) bar = "§eTeleporting: §a■§7§l■■";
        else bar = "§eTeleporting: §a■■§7§l■";

        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(bar));
    }

    private void cancelTeleport(UUID uuid) {
        tasks.remove(uuid);
    }
}