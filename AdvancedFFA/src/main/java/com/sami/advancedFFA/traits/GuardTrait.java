package com.sami.advancedFFA.traits;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class GuardTrait extends Trait {

    @Persist("particle") public String particleName = "HAPPY_VILLAGER";
    @Persist("push-strength") public double pushStrength = 1.8;
    @Persist("radius") public double radius = 1.5;

    private double angle = 0;

    public GuardTrait() {
        super("guard");
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || npc.getEntity() == null) return;

        Location loc = npc.getEntity().getLocation();

        double x = 0.8 * Math.cos(angle);
        double z = 0.8 * Math.sin(angle);
        double y = Math.sin(angle * 0.5) + 1.1;
        loc.add(x, y, z);

        try {
            loc.getWorld().spawnParticle(Particle.valueOf(particleName), loc, 1, 0, 0, 0, 0);
        } catch (Exception e) {
            loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 1, 0, 0, 0, 0);
        }

        loc.subtract(x, y, z);
        angle += 0.2;

        checkPush(loc);
    }

    private void checkPush(Location loc) {
        for (Player player : loc.getWorld().getPlayers()) {

            if (player.hasMetadata("NPC") || player.equals(npc.getEntity())) {
                continue;
            }

            double distSq = player.getLocation().distanceSquared(loc);

            if (distSq < (radius * radius)) {
                if (distSq < 0.01) {
                    player.setVelocity(new Vector(0.4, 0.2, 0));
                    continue;
                }

                Vector push = player.getLocation().toVector().subtract(loc.toVector()).normalize();
                push.multiply(0.45).setY(0.25);

                if (Double.isFinite(push.getX())) {
                    player.setVelocity(push);
                    player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.5f, 1.5f);
                }
            }
        }
    }
}