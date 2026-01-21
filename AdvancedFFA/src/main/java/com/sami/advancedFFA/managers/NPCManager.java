package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class NPCManager {

    private final Main plugin;

    public NPCManager(Main plugin) {
        this.plugin = plugin;
    }

    public void createStandardNPC() {
        Location loc = new Location(Bukkit.getWorld("spawn"), 48.5, -2.5, -1.5, 90, 0);

        ArmorStand npc = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);

        npc.setCustomName("§a§lStandard Arena");
        npc.setCustomNameVisible(true);
        npc.setArms(true);
        npc.setBasePlate(false);
        npc.setGravity(false);
        npc.setInvulnerable(true);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            npc.addDisabledSlots(slot);
        }

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {

            OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString("ca1dde6d-9b02-4bf0-b75e-ac0c6ae23391"));
            meta.setOwningPlayer(op);
            head.setItemMeta(meta);
        }
        npc.getEquipment().setHelmet(head);

        npc.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        npc.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        npc.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        npc.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
    }
}