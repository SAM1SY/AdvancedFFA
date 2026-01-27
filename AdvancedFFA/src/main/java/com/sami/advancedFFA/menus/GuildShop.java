package com.sami.advancedFFA.menus;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.models.Guild;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class GuildShop {
    private final Main plugin;
    public GuildShop(Main plugin) { this.plugin = plugin; }

    public void open(Player p) {
        Guild guild = plugin.getGuildManager().getGuild(p.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 27, "§8Guild Tag Color Shop");

        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta pm = pane.getItemMeta(); pm.setDisplayName(" "); pane.setItemMeta(pm);
        for (int i = 0; i < 27; i++) inv.setItem(i, pane);

        inv.setItem(10, createColorItem(guild, Material.GOLD_INGOT, "§6§lThe Supreme", 150, "#B8860B", "#FFFACD"));
        inv.setItem(11, createColorItem(guild, Material.FIRE_CHARGE, "§4§lDragon", 30, "#800000", "#FF8000"));
        inv.setItem(12, createColorItem(guild, Material.WHITE_WOOL, "§f§lSnow White", 20, "#FFFFFF", "#B0E0E6"));
        inv.setItem(13, createColorItem(guild, Material.RED_DYE, "§c§lPure Red", 20, "#FF0000", "#990000"));
        inv.setItem(14, createColorItem(guild, Material.LAPIS_LAZULI, "§b§lKitsune Blue", 25, "#00B4DB", "#0083B0"));
        inv.setItem(15, createColorItem(guild, Material.AMETHYST_SHARD, "§5§lCelestial", 40, "#C33764", "#1D2671"));
        inv.setItem(16, createColorItem(guild, Material.PINK_DYE, "§d§lLight Pink", 30, "#FFB6C1", "#FF69B4"));

        p.openInventory(inv);
    }

    private ItemStack createColorItem(Guild guild, Material mat, String name, int cost, String h1, String h2) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        String hexPair = h1 + ":" + h2;
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        lore.add("§7§m-----------------------");
        if (guild.getTagColor().equals(hexPair)) {
            lore.add("§a§lACTIVE");
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else if (guild.ownsColor(hexPair)) {
            lore.add("§e§lOWNED"); lore.add("§7Click to switch!");
        } else {
            lore.add("§7Cost: §e" + cost + " Coins"); lore.add("§7Click to purchase!");
        }
        lore.add("§7§m-----------------------");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}