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
import org.bukkit.event.inventory.InventoryType;
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

    public LeaderboardListener(Main plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = e.getItem();
            if (item != null && item.getType() == Material.EMERALD && item.hasItemMeta()) {
                if (item.getItemMeta().getDisplayName().equals(LEADERBOARD_NAME)) {
                    e.setCancelled(true);
                    openLeaderboard(e.getPlayer());
                }
            }
        }
    }

    public void openLeaderboard(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Global Leaderboards");

        inv.setItem(10, createStatItem(Material.DIAMOND_SWORD, "§a§lKills", "kills"));
        inv.setItem(12, createStatItem(Material.SKELETON_SKULL, "§a§lDeaths", "deaths"));
        inv.setItem(14, createStatItem(Material.NETHERITE_SWORD, "§a§lStreaks", "best-streak"));
        inv.setItem(16, createStatItem(Material.EXPERIENCE_BOTTLE, "§a§lLevel", "level"));

        p.openInventory(inv);
    }

    private ItemStack createStatItem(Material mat, String name, String statKey) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        lore.add("§7§m-----------------------");

        Map<String, Integer> topData = plugin.getPlayerDataManager().getTop10(statKey);
        int rank = 1;

        for (Map.Entry<String, Integer> entry : topData.entrySet()) {
            lore.add("§e#" + rank + " §f" + entry.getKey() + " §7- §a" + entry.getValue());
            rank++;
        }

        if (topData.isEmpty()) lore.add("§cNo data found yet.");

        lore.add("§7§m-----------------------");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("§8Global Leaderboards")) {
            e.setCancelled(true);
            return;
        }

        ItemStack current = e.getCurrentItem();
        ItemStack cursor = e.getCursor();

        if (isLeaderboardItem(current) || isLeaderboardItem(cursor)) {
            e.setCancelled(true);
            if (e.getClick() == ClickType.NUMBER_KEY || e.getClick().isShiftClick()) {
                e.getWhoClicked().closeInventory();
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (isLeaderboardItem(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cYou cannot drop the Leaderboard tool!");
        }
    }

    private boolean isLeaderboardItem(ItemStack item) {
        return item != null && item.getType() == Material.EMERALD &&
                item.hasItemMeta() && item.getItemMeta().getDisplayName().equals(LEADERBOARD_NAME);
    }
}