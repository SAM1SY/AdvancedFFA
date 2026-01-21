package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KitManager {
    private final Main plugin;
    private final Map<UUID, Map<String, Map<Material, Integer>>> customLayouts = new HashMap<>();
    private final Map<UUID, Integer> currentStreaks = new HashMap<>();

    public KitManager(Main plugin) { this.plugin = plugin; }

    public void giveKit(Player p, String mode) {
        p.getInventory().clear();

        int prot = getProtForMode(mode);
        p.getInventory().setHelmet(createItem(Material.DIAMOND_HELMET, Enchantment.PROTECTION, prot));
        p.getInventory().setChestplate(createItem(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION, prot));
        p.getInventory().setLeggings(createItem(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION, prot));
        p.getInventory().setBoots(createItem(Material.DIAMOND_BOOTS, Enchantment.PROTECTION, prot));

        Map<Material, Integer> layout = getLayout(p.getUniqueId(), mode);

        for (Map.Entry<Material, Integer> entry : layout.entrySet()) {
            Material mat = entry.getKey();
            int slot = entry.getValue();

            if ((slot >= 0 && slot <= 8) || slot == 40) {
                p.getInventory().setItem(slot, getPreviewItem(mat, mode));
            }
        }
        p.updateInventory();
    }

    public void setLayout(UUID uuid, String mode, Map<Material, Integer> layout) {
        customLayouts.computeIfAbsent(uuid, k -> new HashMap<>()).put(mode, layout);
    }

    // NEW: Method to clear custom layouts for the Reset Button
    public void removeLayout(UUID uuid, String mode) {
        if (customLayouts.containsKey(uuid)) {
            customLayouts.get(uuid).remove(mode);
        }
    }

    public Map<Material, Integer> getLayout(UUID uuid, String mode) {
        Map<String, Map<Material, Integer>> playerModes = customLayouts.get(uuid);
        Map<Material, Integer> layout;

        if (playerModes == null || !playerModes.containsKey(mode)) {
            layout = new HashMap<>(getDefaultLayout());
        } else {
            layout = new HashMap<>(playerModes.get(mode));
        }

        layout.keySet().removeIf(m -> m == Material.BARRIER || m == Material.AIR);
        return layout;
    }

    public ItemStack getPreviewItem(Material mat, String mode) {
        if (mat == Material.WOODEN_SWORD) return createWoodSword();

        Enchantment ench = mat.toString().contains("SWORD") ? Enchantment.SHARPNESS : Enchantment.PROTECTION;
        int level = mat.toString().contains("SWORD") ? 1 : getProtForMode(mode);

        return createItem(mat, ench, level);
    }

    public int getProtForMode(String mode) {
        if (mode.equalsIgnoreCase("Beast")) return 4;
        if (mode.equalsIgnoreCase("Speed")) return 3;
        return 1;
    }

    public ItemStack createItem(Material mat, Enchantment ench, int lvl) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(ench, lvl, true);
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createWoodSword() {
        ItemStack item = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.SHARPNESS, 2, true);
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
        }
        return item;
    }

    public Map<Material, Integer> getDefaultLayout() {
        Map<Material, Integer> def = new HashMap<>();
        def.put(Material.DIAMOND_SWORD, 0);
        def.put(Material.WOODEN_SWORD, 1);
        return def;
    }

    public void addKill(Player p) {
        UUID uuid = p.getUniqueId();
        currentStreaks.put(uuid, currentStreaks.getOrDefault(uuid, 0) + 1);
    }

    public int getStreak(UUID uuid) {
        return currentStreaks.getOrDefault(uuid, 0);
    }

    public void resetStreak(Player p) {
        currentStreaks.put(p.getUniqueId(), 0);
    }
}