package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.util.ArrowUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;

public class RicochetAbility implements Ability {
    private final AbilityContext context;
    private final NamespacedKey ricochetCountKey;

    public RicochetAbility(AbilityContext context) {
        this.context = context;
        this.ricochetCountKey = new NamespacedKey(context.plugin(), "ricochetCount");
    }

    @Override
    public String getId() {
        return "ricochet";
    }

    public void onArrowLaunch(EntityShootBowEvent event) {
        event.getProjectile().setGlowing(true);
        setRicochetCount(event.getProjectile(), 7);
    }

    @Override
    public void onArrowHit(ProjectileHitEvent event) {
        Block block = event.getHitBlock();

        int ricochetCount = getRicochetCount(event.getEntity());
        if (ricochetCount > 0) {
            ArrowUtil.RicochetEvent(context.plugin(), event);
            setRicochetCount(event.getEntity(), ricochetCount - 1);
            event.getEntity().getWorld().spawnParticle(
                    Particle.EXPLOSION,
                    event.getEntity().getLocation(),
                    1,
                    0, 0, 0,
                    0,
                    null,
                    true
            );

        }

    }

    private int getRicochetCount(Entity entity) {
        return entity.getPersistentDataContainer().getOrDefault(ricochetCountKey, PersistentDataType.INTEGER, 0);
    }

    private void setRicochetCount(Entity entity, int count) {
        entity.getPersistentDataContainer().set(ricochetCountKey, PersistentDataType.INTEGER, count);
    }
}
