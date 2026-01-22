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
        World world = Bukkit.getWorld("spawn");
        if (world == null) {
            plugin.getLogger().warning("World 'spawn' not found!");
            return;
        }

        Location loc = new Location(world, 48.5, -2.0, -1.5, 90, 0);
        NPCRegistry registry = CitizensAPI.getNPCRegistry();

        Iterator<NPC> iter = registry.iterator();
        while (iter.hasNext()) {
            NPC existing = iter.next();
            if (ChatColor.stripColor(existing.getName()).equalsIgnoreCase("Standard Arena")) {
                existing.destroy();
            }
        }

        NPC npc = registry.createNPC(EntityType.PLAYER, NPC_NAME);
        npc.data().setPersistent("gravity", false);

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
            boolean rising = true;

            @Override
            public void run() {
                if (npc == null || !npc.isSpawned() || npc.getEntity() == null) {
                    this.cancel();
                    return;
                }

                Location loc = npc.getEntity().getLocation();

                double x = 0.8 * Math.cos(angle);
                double z = 0.8 * Math.sin(angle);

                double y = Math.sin(angle * 0.5) + 1.0;

                // 3. Spawn the spiral particle
                loc.add(x, y, z);
                loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 1, 0, 0, 0, 0);

                loc.subtract(x, y, z);

                if (Math.random() < 0.15) {
                    loc.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(0, 1, 0), 1, 0.2, 0.5, 0.2, 0.02);
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