package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.traits.GuardTrait;
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

import java.util.Iterator;

public class NPCManager {
    private final Main plugin;

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
        setupModularNPC("§a§lStandard Arena", loc, "sSkaito", "HAPPY_VILLAGER", 0.45, 1);
    }

    public void createSpeedArenaNPC() {
        World world = Bukkit.getWorld("spawn");
        if (world == null) {
            plugin.getLogger().warning("World 'spawn' not found!");
            return;
        }

        Location loc = new Location(world, 48.5, -2.0, 2.5, 90, 0);
        setupModularNPC("§a§lSpeed Arena", loc, "SAM1_SY", "FLAME", 0.45, 1);
    }

    public void createBeastArenaNPC() {
        World world = Bukkit.getWorld("spawn");
        if (world == null) {
            plugin.getLogger().warning("World 'spawn' not found!");
            return;
        }

        Location loc = new Location(world, 48.5, -2.0, 0.5, 90, 0);
        setupModularNPC("§a§lBeast Arena", loc, "Technoblade", "BEAST", 0.45, 1);
    }

    public void createKitEditorNPC() {
        World world = Bukkit.getWorld("spawn");
        if (world == null) return;

        Location loc = new Location(world, 34.5, -3.5, -4.5, 65, 0);
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        String npcName = "§6§lKit Editor";

        Iterator<NPC> iter = registry.iterator();
        while (iter.hasNext()) {
            NPC existing = iter.next();
            if (ChatColor.stripColor(existing.getName()).equalsIgnoreCase("Kit Editor")) {
                existing.destroy();
            }
        }

        NPC npc = registry.createNPC(EntityType.PLAYER, npcName);

        npc.data().setPersistent("pushable", false);
        npc.data().setPersistent("gravity", false);
        npc.data().setPersistent("collidable", false);

        npc.spawn(loc);

        npc.getOrAddTrait(SkinTrait.class).setSkinName("meDany");

        Equipment equip = npc.getOrAddTrait(Equipment.class);
        for (Equipment.EquipmentSlot slot : Equipment.EquipmentSlot.values()) {
            equip.set(slot, null);
        }

        GuardTrait guard = npc.getOrAddTrait(GuardTrait.class);
        guard.particleName = "NONE";
        guard.pushStrength = 1.0;
        guard.radius = 1.0;
    }

    private void setupModularNPC(String name, Location loc, String skin, String particle, double push, double radius) {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();

        String strippedName = ChatColor.stripColor(name);
        Iterator<NPC> iter = registry.iterator();
        while (iter.hasNext()) {
            NPC existing = iter.next();
            if (ChatColor.stripColor(existing.getName()).equalsIgnoreCase(strippedName)) {
                existing.destroy();
            }
        }

        NPC npc = registry.createNPC(EntityType.PLAYER, name);
        npc.data().setPersistent("pushable", false);
        npc.data().setPersistent("gravity", false);
        npc.data().setPersistent("collidable", false);
        npc.data().setPersistent("keep-chunk-loaded", true);

        npc.spawn(loc);
        npc.teleport(loc, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
        npc.getOrAddTrait(SkinTrait.class).setSkinName(skin);

        Equipment equip = npc.getOrAddTrait(Equipment.class);
        equip.set(Equipment.EquipmentSlot.HAND, getEnchanted(Material.DIAMOND_SWORD));
        equip.set(Equipment.EquipmentSlot.HELMET, getEnchanted(Material.DIAMOND_HELMET));
        equip.set(Equipment.EquipmentSlot.CHESTPLATE, getEnchanted(Material.DIAMOND_CHESTPLATE));
        equip.set(Equipment.EquipmentSlot.LEGGINGS, getEnchanted(Material.DIAMOND_LEGGINGS));
        equip.set(Equipment.EquipmentSlot.BOOTS, getEnchanted(Material.DIAMOND_BOOTS));

        GuardTrait guard = npc.getOrAddTrait(GuardTrait.class);
        guard.particleName = particle;
        guard.pushStrength = push;
        guard.radius = radius;

        plugin.getLogger().info("NPC " + name + " forced to: " + loc.getX() + ", " + loc.getY());
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