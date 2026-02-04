package com.sami.advancedFFA.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class StatsPaperListener implements Listener {

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) { return; }
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.PAPER) { return; }

        p.performCommand("stats");

        e.setCancelled(true);
    }

}
