package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.data.damage.DamageSource;
import com.azerl0.gutitubow.data.damage.GutitubowDamageType;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.util.ArrowUtil;
import com.azerl0.gutitubow.util.EntityUtil;
import com.azerl0.gutitubow.util.MathUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class RicochetAbility extends Ability {
    private final NamespacedKey ricochetCountKey;
    private final NamespacedKey ricochetDamageCountKey;

    public RicochetAbility(AbilityContext context) {
        super(context);
        this.ricochetCountKey = new NamespacedKey(context.plugin(), "ricochetCount");
        this.ricochetDamageCountKey = new NamespacedKey(context.plugin(), "ricochetDamageCount");
    }

    @Override
    public final @NotNull AbilityType getType() {
        return AbilityType.RICOCHET;
    }

    @Override
    public void onShoot(EntityShootBowEvent event) {
        Arrow arrow = (Arrow) event.getProjectile();

        EntityUtil.setGlow(arrow, NamedTextColor.AQUA);
        arrow.setGlowing(true);
        setRicochetCount(arrow, 6);
        setRicochetDamageCount(arrow, 0);
    }

    @Override
    public void onArrowHit(ProjectileHitEvent event) {
        Arrow arrow = (Arrow) event.getEntity();
        World world = arrow.getWorld();

        if (!(arrow.getShooter() instanceof Player shooterPlayer)) return;

        int ricochetCount = getRicochetCount(arrow);
        if (ricochetCount > 0) {
            arrow.setHitSound(Sound.ENTITY_SLIME_JUMP);
            ArrowUtil.RicochetEvent(context.plugin(), event, 0.8);

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
            for (Player damagePlayer : world.getNearbyPlayers(loc, damageRadius)) {

                GamePlayer gamePlayer = context.game().getGamePlayer(damagePlayer);
                if (gamePlayer.isTeammate(playerUUID)) continue;

                if (context.game().getPhase() == 0 && context.game().isStarted()) continue;

                if (!damagePlayer.getUniqueId().equals(shooterPlayer.getUniqueId()) && !EntityUtil.isInvalidHitEntity(damagePlayer)) {
                    DamageSource damageSource = DamageSource.builder()
                            .damageType(GutitubowDamageType.RICOCHET_BLAST)
                            .directPlayer(shooterPlayer)
                            .damageLocation(MathUtil.rayTraceEntityHit(loc, damagePlayer))
                            .distance(shooterPlayer.getLocation()
                                    .distance(damagePlayer.getLocation()))
                            .build();
                    gamePlayer.damage(damagePlayer, 0.5, damageSource, true);
                    setRicochetDamageCount(arrow, ricochetDamageCount + 1);
                }
            }
            if (getRicochetDamageCount(arrow) >= 2) {
                arrow.remove();
            }
        } else {
            arrow.setLifetimeTicks(1000);
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
