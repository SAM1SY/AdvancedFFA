package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.Random;

public class NPCListener implements Listener {

    private final Main plugin;
    private final Random random = new Random();

    public NPCListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCInteract(PlayerInteractAtEntityEvent e) {
        if (!(e.getRightClicked() instanceof ArmorStand)) return;

        ArmorStand npc = (ArmorStand) e.getRightClicked();
        if (npc.getCustomName() != null && npc.getCustomName().contains("Standard Arena")) {
            e.setCancelled(true);
            teleportToArena(e.getPlayer());
        }
    }

    private void teleportToArena(Player player) {
        World arenaWorld = Bukkit.getWorld("arena");
        if (arenaWorld == null) {
            player.sendMessage("§cError: 'arena' world not found.");
            return;
        }

        double angle = random.nextDouble() * 2 * Math.PI;
        double r = 25 * Math.sqrt(random.nextDouble());
        double x = r * Math.cos(angle);
        double z = r * Math.sin(angle);

        Location teleportLoc = new Location(arenaWorld, x + 0.5, 0.0, z + 0.5);

        player.teleport(teleportLoc);
        player.sendMessage("§a§lSERVER §8» §7Sending you to §fStandard Arena§7...");
    }
}