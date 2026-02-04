package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.utils.GuardTrait;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Random;


public class NPCListener implements Listener {
    private final Main plugin;
    private final Random random = new Random();

    public NPCListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCitizensClick(NPCRightClickEvent e) {
        NPC npc = e.getNPC();
        Player player = e.getClicker();

        if (npc.hasTrait(GuardTrait.class)) {
            String name = ChatColor.stripColor(npc.getName());
            if (name.equalsIgnoreCase("Standard Arena")) {
                teleportToArena(player, "Standard");
            } else if (name.equalsIgnoreCase("Speed Arena")) {
                teleportToArena(player, "Speed");
            } else if (name.equalsIgnoreCase("Beast Arena")) {
                teleportToArena(player, "Beast");
            } else if (name.equalsIgnoreCase("Kit Editor")) {
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
                plugin.getKitCommand().openModeSelector(player);
            }
        }
    }

    public void teleportToArena(Player p, String mode) {
        World arenaWorld = Bukkit.getWorld("arena");
        if (arenaWorld == null) {
            p.sendMessage("§cError: Arena world 'arena' not found.");
            return;
        }

        String path = "arenas." + mode.toLowerCase();

        double centerX = plugin.getConfig().getDouble(path + ".center-x");
        double centerZ = plugin.getConfig().getDouble(path + ".center-z");
        double yLevel = plugin.getConfig().getDouble(path + ".y-level");
        double radius = plugin.getConfig().getDouble(path + ".spawn-radius");
        double x = centerX + (random.nextDouble() * radius * 2) - radius;
        double z = centerZ + (random.nextDouble() * radius * 2) - radius;

        Location spawnLoc = new Location(arenaWorld, x, yLevel, z);

        p.teleport(spawnLoc);
        p.playSound(p.getLocation(), Sound.ENTITY_ARMADILLO_AMBIENT, 1f, 2f);
        p.setFlying(false);
        p.setAllowFlight(false);

        if (plugin.getKitManager() != null) {
            plugin.getKitManager().giveKit(p, mode);
        }

        p.sendTitle("§a§l" + mode.toUpperCase(), "§7Good luck!", 5, 20, 5);

    }

}

