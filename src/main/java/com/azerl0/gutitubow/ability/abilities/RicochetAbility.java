package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.manager.GamePlayerManager;
import com.azerl0.gutitubow.util.ArrowUtil;
import com.azerl0.gutitubow.util.EntityUtil;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;

public class RicochetAbility implements Ability {
    private final AbilityContext context;
    private final NamespacedKey ricochetCountKey;
    private final NamespacedKey ricochetDamageCountKey;

    public RicochetAbility(AbilityContext context) {
        this.context = context;
        this.ricochetCountKey = new NamespacedKey(context.plugin(), "ricochetCount");
        this.ricochetDamageCountKey = new NamespacedKey(context.plugin(), "ricochetDamageCount");
    }

    @Override
    public String getId() {
        return "ricochet";
    }

    public void onArrowLaunch(EntityShootBowEvent event) {
        event.getProjectile().setGlowing(true);
        setRicochetCount(event.getProjectile(), event.getEntity().isSneaking() ? 5 : 1);
        setRicochetDamageCount(event.getProjectile(), 0);
    }

    @Override
    public void onArrowHit(ProjectileHitEvent event) {
        Arrow arrow = (Arrow) event.getEntity();
        World world = arrow.getWorld();
        Entity shooter = (Entity) arrow.getShooter();

        int ricochetCount = getRicochetCount(arrow);
        if (ricochetCount > 0) {
            ArrowUtil.RicochetEvent(context.plugin(), event);
            setRicochetCount(arrow, ricochetCount - 1);
            world.spawnParticle(
                    Particle.EXPLOSION,
                    arrow.getLocation(),
                    1,
                    0, 0, 0,
                    0,
                    null,
                    true
            );

            int ricochetDamageCount = getRicochetDamageCount(arrow);
            Location loc = arrow.getLocation();
            double damageRadius = 2.5;
            for (Entity entity : world.getNearbyEntities(loc, damageRadius, damageRadius, damageRadius)) {
                if (entity instanceof LivingEntity damageEntity) {

                    GamePlayerManager gamePlayerManager = context.gamePlayerManager();
                    GamePlayerManager.GamePlayer gamePlayer = gamePlayerManager.getGamePlayer(damageEntity);

                    if (shooter != null && !damageEntity.getUniqueId().equals(shooter.getUniqueId()) && !EntityUtil.isInvalidHitEntity(damageEntity)) {
                        gamePlayer.damage(0.5, shooter);
                        setRicochetDamageCount(arrow, ricochetDamageCount + 1);
                    }
                }
            }
            if (getRicochetDamageCount(arrow) >= 2) {
                arrow.remove();
            }
        } else {
            arrow.setLifetimeTicks(1000);
            arrow.setGlowing(false);
        }

    }

    private int getRicochetCount(Entity entity) {
        return entity.getPersistentDataContainer().getOrDefault(ricochetCountKey, PersistentDataType.INTEGER, 0);
    }

    private void setRicochetCount(Entity entity, int count) {
        entity.getPersistentDataContainer().set(ricochetCountKey, PersistentDataType.INTEGER, count);
    }

    private int getRicochetDamageCount(Entity entity) {
        return entity.getPersistentDataContainer().getOrDefault(ricochetDamageCountKey, PersistentDataType.INTEGER, 0);
    }

    private void setRicochetDamageCount(Entity entity, int count) {
        entity.getPersistentDataContainer().set(ricochetDamageCountKey, PersistentDataType.INTEGER, count);
    }
}
