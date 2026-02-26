package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.manager.GamePlayerManager;
import com.azerl0.gutitubow.util.EntityUtil;
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
import org.bukkit.util.Vector;

public class BombAbility implements Ability {
    private final AbilityContext context;
    public BombAbility(AbilityContext context) {
        this.context = context;
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


                double damageRadius = 2;
                for (Entity entity : world.getNearbyEntities(loc, damageRadius, damageRadius, damageRadius)) {
                    if (entity instanceof LivingEntity damageEntity) {

                        GamePlayerManager gamePlayerManager = context.gamePlayerManager();
                        GamePlayerManager.GamePlayer gamePlayer = gamePlayerManager.getGamePlayer(damageEntity);

                        if (!shooter.getUniqueId().equals(damageEntity.getUniqueId()) && !EntityUtil.isInvalidHitEntity(damageEntity)) {
                            gamePlayer.damage(0.5, event.getPlayer());
                        }
                    }
                }

                double knockbackRadius = 3;
                for (Entity entity : world.getNearbyEntities(loc, knockbackRadius, knockbackRadius, knockbackRadius)) {
                    if (entity instanceof LivingEntity living) {

                        Vector knockback = living.getLocation().toVector()
                                .subtract(loc.toVector())
                                .normalize()
                                .multiply(1.2);

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
