package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class BombAbility implements Ability {

    private final JavaPlugin plugin;

    public BombAbility(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "bomb";
    }

    public void onArrowLaunch(EntityShootBowEvent event) {
        event.getProjectile().setGlowing(true);
    }


    public void onPlayerLeftClickBow(PlayerInteractEvent event) {

        for (World world : Bukkit.getWorlds()) {
            for (Arrow arrow : world.getEntitiesByClass(Arrow.class)) {

                if (!(arrow.getShooter() instanceof Player shooter)) continue;
                if (!shooter.getUniqueId().equals(event.getPlayer().getUniqueId())) continue;

                Location loc = arrow.getLocation().add(0, -0.2, 0);


                double radius = 3;

                for (Entity entity : world.getNearbyEntities(loc, radius, radius, radius)) {
                    if (entity instanceof LivingEntity living) {

                        Vector knockback = living.getLocation().toVector()
                                .subtract(loc.toVector())
                                .normalize()
                                .multiply(1.3);

//                        knockback.setY(0.6);
                        living.setVelocity(knockback);

                    }
                }
                arrow.getWorld().spawnParticle(
                        Particle.EXPLOSION,
                        arrow.getLocation(),
                        1,
                        0,0,0,
                        0,
                        null,
                        true);

                arrow.remove();
            }
        }

    }
}
