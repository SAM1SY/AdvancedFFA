package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KitManager {
    private final Main plugin;
    private final Map<UUID, Map<String, Map<Material, Integer>>> customLayouts = new HashMap<>();

    public KitManager(Main plugin) {
        this.plugin = plugin;
    }

    public void giveKit(Player p, String mode) {
        p.getInventory().clear();
        p.clearActivePotionEffects();

        if (mode.equalsIgnoreCase("Beast")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, false, false));
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 1f, 0.8f);
            p.sendMessage("§c§lBEAST MODE §8» §7Strength I and Protection IV active!");

        } else if (mode.equalsIgnoreCase("Speed")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
            p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 2.0f);
            p.sendMessage("§b§lSPEED MODE §8» §7Speed I and Protection III active!");

        } else {
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            p.sendMessage("§a§lSTANDARD MODE §8» §7Standard kit active!");
        }

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

    public void removeLayout(UUID uuid, String mode) {
        if (customLayouts.containsKey(uuid)) {
            customLayouts.get(uuid).remove(mode);
        }
    }

    public Map<Material, Integer> getLayout(UUID uuid, String mode) {
        Map<String, Map<Material, Integer>> playerModes = customLayouts.get(uuid);

        if (playerModes != null && playerModes.containsKey(mode)) {
            return playerModes.get(mode);
        }

        Map<Material, Integer> dbLayout = plugin.getDatabaseManager().loadKitLayout(uuid, mode);

        if (!dbLayout.isEmpty()) {
            setLayout(uuid, mode, dbLayout);
            return dbLayout;
        }

        return getDefaultLayout();
    }

    public ItemStack getPreviewItem(Material mat, String mode) {
        if (mat == Material.WOODEN_SWORD) return createWoodSword();

        boolean isSword = mat.toString().contains("SWORD");
        Enchantment ench = isSword ? Enchantment.SHARPNESS : Enchantment.PROTECTION;

        int level;
        if (isSword) {
            level = mode.equalsIgnoreCase("Beast") ? 2 : 1;
        } else {
            level = getProtForMode(mode);
        }

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
            if (ench != null) {
                meta.addEnchant(ench, lvl, true);
            }
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
}