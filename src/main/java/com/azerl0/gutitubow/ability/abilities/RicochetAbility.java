package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.util.ArrowUtil;
import com.azerl0.gutitubow.util.EntityUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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

    public final AbilityType getType() {
        return AbilityType.RICOCHET;
    }

    public void onArrowLaunch(EntityShootBowEvent event) {
        Arrow arrow = (Arrow) event.getProjectile();

        EntityUtil.setGlow(arrow, NamedTextColor.AQUA);
        arrow.setGlowing(true);
        setRicochetCount(arrow, event.getEntity().isSneaking() ? 5 : 1);
        setRicochetDamageCount(arrow, 0);
    }

    @Override
    public void onArrowHit(ProjectileHitEvent event) {
        Arrow arrow = (Arrow) event.getEntity();
        World world = arrow.getWorld();

        if (!(arrow.getShooter() instanceof Player shooterPlayer)) return;

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
            double damageRadius = 2.0;
            for (Entity entity : world.getNearbyEntities(loc, damageRadius, damageRadius, damageRadius)) {
                if (entity instanceof Player damageEntity) {

                    Game game = context.game();

                    if (!damageEntity.getUniqueId().equals(shooterPlayer.getUniqueId()) && !EntityUtil.isInvalidHitEntity(damageEntity)) {
                        game.damage(damageEntity, 0.5, shooterPlayer);
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
