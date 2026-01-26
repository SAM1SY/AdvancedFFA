package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LeaderboardListener implements Listener {

    private final Main plugin;
    private final String LEADERBOARD_NAME = "§a§lLeaderBoard §7(Right Click)";
    private final String GUI_TITLE = "§8Global Leaderboards";

    public LeaderboardListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = e.getItem();
            if (isLeaderboardItem(item)) {
                e.setCancelled(true);
                openLeaderboard(e.getPlayer());
            }
        }
    }

    public void openLeaderboard(Player p) {
        plugin.getStatsManager().updateGlobalLeaderboards();

        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        inv.setItem(10, createStatItem(Material.DIAMOND_SWORD, "§a§lKills", "kills"));
        inv.setItem(12, createStatItem(Material.SKELETON_SKULL, "§c§lDeaths", "deaths"));
        inv.setItem(14, createStatItem(Material.NETHERITE_SWORD, "§e§lStreaks", "best_streak"));
        inv.setItem(16, createStatItem(Material.EXPERIENCE_BOTTLE, "§b§lLevel", "level"));

        p.openInventory(inv);
    }

    private ItemStack createStatItem(Material mat, String name, String statKey) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        lore.add("§7§m-----------------------");

        Map<String, Integer> topData = plugin.getStatsManager().getCachedTop10(statKey);

        if (topData == null || topData.isEmpty()) {
            lore.add("§cUpdating data...");
            lore.add("§7Please check back in a minute.");
        } else {
            int rank = 1;
            for (Map.Entry<String, Integer> entry : topData.entrySet()) {
                String rankColor = (rank == 1) ? "§6§l" : (rank == 2) ? "§f§l" : (rank == 3) ? "§e§l" : "§7";
                lore.add(rankColor + "#" + rank + " §f" + entry.getKey() + " §8» §a" + entry.getValue());
                rank++;
                if (rank > 10) break;
            }
        }

        lore.add("§7§m-----------------------");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(GUI_TITLE)) {
            e.setCancelled(true);
            return;
        }

        ItemStack current = e.getCurrentItem();
        if (isLeaderboardItem(current)) {
            if (e.getClick() == ClickType.NUMBER_KEY || e.isShiftClick() || e.getClick() == ClickType.DROP) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (isLeaderboardItem(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
        }
    }

    private boolean isLeaderboardItem(ItemStack item) {
        return item != null &&
                item.getType() == Material.EMERALD &&
                item.hasItemMeta() &&
                item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals(LEADERBOARD_NAME);
    }
}