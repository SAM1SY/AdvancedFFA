package com.sami.advancedFFA.commands;

import com.sami.advancedFFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class KitCommand implements CommandExecutor {

    private final Main plugin;
    public KitCommand(Main plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if (!sender.hasPermission("ffa.kit.use")) return true;
        openModeSelector((Player) sender);
        return true;
    }

    public void openModeSelector(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Select Mode to Edit");

        inv.setItem(11, createNamedItem(Material.DIAMOND_SWORD, "§fStandard Mode"));
        inv.setItem(13, createNamedItem(Material.FEATHER, "§bSpeed Mode"));
        inv.setItem(15, createNamedItem(Material.DIAMOND_CHESTPLATE, "§4Beast Mode"));

        p.openInventory(inv);
    }

    public void openEditor(Player p, String mode) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Editing: " + mode);

        ItemStack glass = createNamedItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 9; i < 54; i++) {
            inv.setItem(i, glass);
        }

        for (int i = 0; i < 9; i++) inv.setItem(i, null);
        for (int i = 36; i <= 40; i++) inv.setItem(i, null);

        int prot = plugin.getKitManager().getProtForMode(mode);
        inv.setItem(39, plugin.getKitManager().createItem(Material.DIAMOND_HELMET, Enchantment.PROTECTION, prot));
        inv.setItem(38, plugin.getKitManager().createItem(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION, prot));
        inv.setItem(37, plugin.getKitManager().createItem(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION, prot));
        inv.setItem(36, plugin.getKitManager().createItem(Material.DIAMOND_BOOTS, Enchantment.PROTECTION, prot));

        Map<Material, Integer> layout = plugin.getKitManager().getLayout(p.getUniqueId(), mode);
        for (Map.Entry<Material, Integer> entry : layout.entrySet()) {
            int slot = entry.getValue();
            if ((slot >= 0 && slot <= 8) || slot == 40) {
                if (entry.getKey() != Material.BARRIER && entry.getKey() != Material.AIR) {
                    inv.setItem(slot, plugin.getKitManager().getPreviewItem(entry.getKey(), mode));
                }
            }
        }

        // 5. Buttons
        inv.setItem(51, createNamedItem(Material.RED_WOOL, "§c§lCANCEL"));
        inv.setItem(52, createNamedItem(Material.ORANGE_WOOL, "§6§lRESET"));
        inv.setItem(53, createNamedItem(Material.LIME_WOOL, "§a§lSAVE"));

        p.openInventory(inv);
    }

    private ItemStack createNamedItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}