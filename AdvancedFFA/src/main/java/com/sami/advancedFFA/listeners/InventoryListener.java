package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.commands.KitCommand;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryListener implements Listener {

    private final Main plugin;
    private final List<Material> ALLOWED = Arrays.asList(
            Material.DIAMOND_SWORD, Material.WOODEN_SWORD,
            Material.GOLDEN_APPLE, Material.SHIELD
    );

    public InventoryListener(Main plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();

        if (title.equals("§8Select Mode to Edit")) {
            e.setCancelled(true);
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            KitCommand cmd = (KitCommand) plugin.getCommand("kit").getExecutor();
            String name = clicked.getItemMeta().getDisplayName();

            // Unified capitalization: "Standard", "Speed", "Beast"
            if (name.contains("Standard")) cmd.openEditor(p, "Standard");
            else if (name.contains("Speed")) cmd.openEditor(p, "Speed");
            else if (name.contains("Beast")) cmd.openEditor(p, "Beast");
            return;
        }

        if (title.startsWith("§8Editing: ")) {
            int slot = e.getRawSlot();
            String mode = title.replace("§8Editing: ", "");

            if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
                e.setCancelled(true);
                return;
            }

            // Allow moving items in the hotbar (0-8) and offhand (40)
            if ((slot >= 0 && slot <= 8) || slot == 40) return;

            e.setCancelled(true);

            if (slot == 53) { // SAVE
                saveLayout(p, e.getInventory(), mode);
                p.closeInventory();
            } else if (slot == 52) { // RESET
                plugin.getKitManager().removeLayout(p.getUniqueId(), mode);
                ((KitCommand) plugin.getCommand("kit").getExecutor()).openEditor(p, mode);
                p.sendMessage("§a§lKIT §8» §7Layout reset to default!");
            } else if (slot == 51) { // CANCEL
                p.closeInventory();
            }
        }
    }

    private void saveLayout(Player p, Inventory inv, String mode) {
        Map<Material, Integer> newLayout = new HashMap<>();
        int[] checkSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 40};

        for (int i : checkSlots) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;

            Material type = item.getType();
            if (ALLOWED.contains(type)) {
                newLayout.put(type, i);
            }
        }

        if (newLayout.containsKey(Material.DIAMOND_SWORD)) {
            plugin.getKitManager().setLayout(p.getUniqueId(), mode, newLayout);
            plugin.getPlayerDataManager().saveLayout(p.getUniqueId(), mode, newLayout);
            p.sendMessage("§a§lKIT §8» §7Layout saved for " + mode + "!");
        } else {
            p.sendMessage("§c§lKIT §8» §7Diamond Sword required!");
        }
    }
}