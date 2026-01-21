package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;

public class NPCManager {
    private final Main plugin;
    private final String NPC_NAME = "§a§lStandard Arena";

    public NPCManager(Main plugin) {
        this.plugin = plugin;
    }

    public void createStandardArenaNPC() {
        // Changed Y to 64.0 to ensure NPC isn't underground
        Location loc = new Location(Bukkit.getWorld("spawn"), 48.5, 64.0, -1.5, 90, 0);

        if (loc.getWorld() == null) {
            plugin.getLogger().warning("World 'spawn' not found!");
            return;
        }

        NPCRegistry registry = CitizensAPI.getNPCRegistry();

        Iterator<NPC> iter = registry.iterator();
        while (iter.hasNext()) {
            NPC existing = iter.next();
            if (ChatColor.stripColor(existing.getName()).equalsIgnoreCase("Standard Arena")) {
                existing.destroy();
            }
        }

        NPC npc = registry.createNPC(EntityType.PLAYER, NPC_NAME);
        npc.getOrAddTrait(SkinTrait.class).setSkinName("sSkaito");

        Equipment equip = npc.getOrAddTrait(Equipment.class);
        equip.set(Equipment.EquipmentSlot.HAND, getEnchanted(Material.DIAMOND_SWORD));
        equip.set(Equipment.EquipmentSlot.HELMET, getEnchanted(Material.DIAMOND_HELMET));
        equip.set(Equipment.EquipmentSlot.CHESTPLATE, getEnchanted(Material.DIAMOND_CHESTPLATE));
        equip.set(Equipment.EquipmentSlot.LEGGINGS, getEnchanted(Material.DIAMOND_LEGGINGS));
        equip.set(Equipment.EquipmentSlot.BOOTS, getEnchanted(Material.DIAMOND_BOOTS));

        npc.getOrAddTrait(LookClose.class).lookClose(true);
        npc.data().setPersistent("collidable", false);

        npc.spawn(loc);
        startParticles(npc);
    }

    private void startParticles(NPC npc) {
        new BukkitRunnable() {
            double angle = 0;
            @Override
            public void run() {
                if (npc == null || !npc.isSpawned() || npc.getEntity() == null) {
                    this.cancel();
                    return;
                }
                Location l = npc.getEntity().getLocation().add(0, 0.1, 0);

                double x = 0.8 * Math.cos(angle);
                double z = 0.8 * Math.sin(angle);
                l.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, l.clone().add(x, 0, z), 1, 0, 0, 0, 0);

                if (Math.random() < 0.15) {
                    l.getWorld().spawnParticle(Particle.END_ROD, l.clone().add(0, 1, 0), 1, 0.2, 0.5, 0.2, 0.02);
                }
                angle += 0.2;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private ItemStack getEnchanted(Material m) {
        ItemStack item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.PROTECTION, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }
}