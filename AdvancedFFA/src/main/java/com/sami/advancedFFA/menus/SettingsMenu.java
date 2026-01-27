package com.sami.advancedFFA.menus;

import com.sami.advancedFFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class SettingsMenu {

    private final Main plugin;

    public SettingsMenu(Main plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Settings");

        inv.setItem(11, createItem(Material.PAPER, "§b§lChat Settings", "§7Configure your chat experience."));
        inv.setItem(15, createItem(Material.DIAMOND_SWORD, "§a§lGameplay Settings", "§7Configure combat and world visuals."));

        p.openInventory(inv);
    }

    public void openChatSettings(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Chat Settings");

        boolean isInGuildChat = plugin.getGuildManager().isInGuildChat(p);
        String guildStatus = isInGuildChat ? "§bGUILD" : "§aGLOBAL";
        inv.setItem(10, createItem(Material.EXPERIENCE_BOTTLE, "§eChat Mode",
                "§7Current Mode: " + guildStatus, "", "§eClick to toggle!"));

        inv.setItem(11, createToggleItem(p, "mention_sound", Material.BELL, "§eMention Sounds"));
        inv.setItem(12, createToggleItem(p, "pm_sound", Material.BELL, "§ePrivate Message Sounds"));
        inv.setItem(14, createToggleItem(p, "global_chat", Material.OAK_SIGN, "§eSee Global Chat"));
        inv.setItem(15, createToggleItem(p, "pm_enabled", Material.BIRCH_SIGN, "§eToggle Private Messages"));

        inv.setItem(22, createItem(Material.ARROW, "§cBack", "§7Go back to the main menu"));
        p.openInventory(inv);
    }

    public void openGameplaySettings(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Gameplay Settings");

        int mode = getSetting(p, "kill_msg_mode", 0);
        String modeText = (mode == 0) ? "§aALL MESSAGES" : (mode == 1) ? "§eSELF ONLY" : "§cNONE";

        inv.setItem(11, createItem(Material.BOOK, "§eKill Messages", "§7Current: " + modeText, "§8Click to cycle through options"));

        boolean canFly = p.getAllowFlight();
        inv.setItem(13, createItem(Material.FEATHER, "§eSpawn Flight",
                "§7Status: " + (canFly ? "§aENABLED" : "§cDISABLED"),
                "§8Only for §bELITE §8and §aMVP §8ranks."));

        int timeMode = getSetting(p, "time_mode", 0);
        String timeText = (timeMode == 0) ? "Day" : (timeMode == 1) ? "Sunset" : "Night";
        inv.setItem(15, createItem(Material.CLOCK, "§ePersonal Time", "§7Change your world time.", "§8Current: §f" + timeText));

        inv.setItem(22, createItem(Material.ARROW, "§cBack", "§7Go back to the main menu"));
        p.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> list = new ArrayList<>();
            for (String s : lore) list.add(s);
            meta.setLore(list);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createToggleItem(Player p, String key, Material mat, String name) {
        boolean isEnabled = getSetting(p, key, 1) == 1;
        String status = isEnabled ? "§aENABLED" : "§cDISABLED";
        return createItem(mat, name, "§7Status: " + status, "", "§eClick to toggle!");
    }

    public int getSetting(Player p, String key, int defaultValue) {
        NamespacedKey nKey = new NamespacedKey(plugin, key);
        return p.getPersistentDataContainer().getOrDefault(nKey, PersistentDataType.INTEGER, defaultValue);
    }

    public void setSetting(Player p, String key, int value) {
        NamespacedKey nKey = new NamespacedKey(plugin, key);
        p.getPersistentDataContainer().set(nKey, PersistentDataType.INTEGER, value);
    }
}