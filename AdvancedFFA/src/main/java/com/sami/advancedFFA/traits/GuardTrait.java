package com.sami.advancedFFA.traits;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuardTrait extends Trait {

    @Persist("particle") public String particleName = "HAPPY_VILLAGER";
    @Persist("push-strength") public double pushStrength = 3;
    @Persist("radius") public double radius = 1.8;

    private double angle = 0;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public GuardTrait() {
        super("guard");
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || npc.getEntity() == null) return;
        Location baseLoc = npc.getEntity().getLocation();

        handleParticles(baseLoc);
        checkPush(baseLoc);
    }

    private void checkPush(Location loc) {
        long now = System.currentTimeMillis();

        for (Player player : loc.getWorld().getPlayers()) {
            if (player.hasMetadata("NPC") || player.equals(npc.getEntity())) continue;

            if (!player.getWorld().equals(loc.getWorld())) continue;

            double distSq = player.getLocation().distanceSquared(loc);

            if (distSq < (radius * radius)) {
                if (cooldowns.containsKey(player.getUniqueId()) && now < cooldowns.get(player.getUniqueId())) {
                    continue;
                }

                Vector push = player.getLocation().toVector().subtract(loc.toVector()).normalize();

                push.multiply(pushStrength).setY(0.35);

                if (Double.isFinite(push.getX())) {
                    player.setVelocity(push);
                    player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.5f, 1.5f);

                    cooldowns.put(player.getUniqueId(), now + 800L);
                }
            } else {
                cooldowns.remove(player.getUniqueId());
            }
        }
    }

    private void handleParticles(Location baseLoc) {
        if (particleName == null || particleName.equalsIgnoreCase("NONE")) return;

        if (particleName.equalsIgnoreCase("FLAME")) {
            baseLoc.getWorld().spawnParticle(Particle.FLAME, baseLoc.clone().add(0, 0.1, 0), 2, 0.2, 0, 0.2, 0.02);
        } else if (particleName.equalsIgnoreCase("BEAST")) {
            for (int i = 0; i < 2; i++) {
                double rad = Math.random() * 2 * Math.PI;
                Location aura = baseLoc.clone().add(Math.cos(rad) * 0.2, 0.2, Math.sin(rad) * 0.2);
                aura.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, aura, 0, Math.cos(rad), 1.0, Math.sin(rad), 0.15);
            }
        } else if (particleName.equalsIgnoreCase("HAPPY_VILLAGER")) {
            Location pLoc = baseLoc.clone().add(0.8 * Math.cos(angle), Math.sin(angle * 0.1) + 1.1, 0.8 * Math.sin(angle));
            pLoc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, pLoc, 1, 0, 0, 0, 0);
            angle += 0.3;
        }
    }
}