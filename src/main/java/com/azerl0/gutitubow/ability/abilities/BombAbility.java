package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.damage.DamageSource;
import com.azerl0.gutitubow.damage.GutitubowDamageType;
import com.azerl0.gutitubow.event.entity.mannequin.MannequinGameDamageEvent;
import com.azerl0.gutitubow.event.gameevent.control.AdvanceProjectileSetControlEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerShootEvent;
import com.azerl0.gutitubow.game.GamePhase;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.util.EntityUtil;
import com.azerl0.gutitubow.util.MathUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BombAbility extends Ability {
    private @Nullable Arrow lastShootArrow;
    public BombAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public final @NotNull AbilityType getType() {
        return AbilityType.BOMB;
    }

    @Override
    public void onGamePlayerShoot(GamePlayerShootEvent event) {
        if (!(event.getProjectile() != null && event.getProjectile() instanceof Arrow arrow)) return;
        EntityUtil.setGlow(arrow, NamedTextColor.RED);
        arrow.setGlowing(true);
        lastShootArrow = arrow;
    }

    @Override
    public void onPlayerLeftClickBow(PlayerInteractEvent event) {

        if (lastShootArrow == null) return;
        if (isErasing()) {
            onErasureEffect();
            return;
        }
        if (lastShootArrow.isValid()) {
            explode(lastShootArrow);
        }
        lastShootArrow = null;
    }

    @Override
    public void onArrowHit(ProjectileHitEvent event) {
        if (event.isCancelled()) return;
        if (event.getHitEntity() != null) return;
        if (!(event.getEntity() instanceof Arrow arrow)) return;

        event.getEntity().getWorld().playSound(arrow.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0f, 1.75f);
        Bukkit.getScheduler().runTaskLater(context.gutitubow(), () -> {
            if (!arrow.isValid()) return;

            explode(arrow);

            arrow.remove();
        }, 20L);
    }

    private void explode(Arrow arrow) {
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        World world = arrow.getWorld();
        Location loc = arrow.getLocation().clone();

        double knockbackRadius = 3;
        for (Entity entity : world.getNearbyEntities(loc.add(0, -0.2, 0), knockbackRadius, knockbackRadius, knockbackRadius)) {
            if (EntityUtil.isInvalidHitEntity(entity)) continue;
            if (entity instanceof LivingEntity living) {


                Vector knockback = living.getLocation().toVector()
                        .subtract(loc.toVector())
                        .normalize()
                        .multiply(1.5);

                living.setVelocity(knockback);
            }
        }

        for (Player damagePlayer : loc.getNearbyPlayers(2)) {
            if (EntityUtil.isInvalidHitEntity(damagePlayer)) continue;
            GamePlayer gamePlayer = context.game().getGamePlayer(damagePlayer);
            if (gamePlayer.isTeammate(playerUUID)) continue;
            if (context.game().getPhase() == GamePhase.PHASE0 && context.game().isStarted()) continue;

            if (!shooter.getUniqueId().equals(damagePlayer.getUniqueId()) && !EntityUtil.isInvalidHitEntity(damagePlayer)) {
                DamageSource damageSource = DamageSource.builder(GutitubowDamageType.BOMB_BLAST)
                        .directPlayer(shooter)
                        .damageLocation(MathUtil.rayTraceEntityHit(loc, damagePlayer))
                        .distance(loc.distance(damagePlayer.getLocation()))
                        .build();
                gamePlayer.damage(damageSource, 1);
            }
        }

        double r = 2;
        for (Entity nearEntity : loc.getNearbyEntities(r, r, r)) {
            if (nearEntity instanceof Player nearPlayer) {
                if (EntityUtil.isInvalidHitEntity(nearPlayer)) continue;
                GamePlayer gamePlayer = context.game().getGamePlayer(nearPlayer);
                if (gamePlayer.isTeammate(playerUUID)) continue;
                if (context.game().getPhase() == GamePhase.PHASE0 && context.game().isStarted()) continue;

                if (!shooter.getUniqueId().equals(nearPlayer.getUniqueId())) {
                    DamageSource damageSource = DamageSource.builder(GutitubowDamageType.BOMB_BLAST)
                            .directPlayer(shooter)
                            .damageLocation(MathUtil.rayTraceEntityHit(loc, nearPlayer))
                            .distance(loc.distance(nearPlayer.getLocation()))
                            .build();
                    gamePlayer.damage(damageSource, 1);
                }
            } else if (nearEntity instanceof Mannequin mannequin) {
                DamageSource damageSource = DamageSource.builder(GutitubowDamageType.BOMB_BLAST)
                        .directPlayer(shooter)
                        .damageLocation(MathUtil.rayTraceEntityHit(loc, mannequin))
                        .distance(loc.distance(mannequin.getLocation()))
                        .build();
                MannequinGameDamageEvent mannequinGameDamageEvent = new MannequinGameDamageEvent(mannequin, 1, damageSource);
                Bukkit.getPluginManager().callEvent(mannequinGameDamageEvent);
            }
        }
        arrow.getWorld().spawnParticle(
                Particle.EXPLOSION,
                arrow.getLocation(),
                1,
                0, 0, 0,
                0,
                null,
                true);

        arrow.getWorld().playSound(arrow.getLocation(), Sound.ENTITY_PUFFER_FISH_STING, 1.0f, 1.0f);

        arrow.remove();
    }

    @Override
    public void onAdvanceProjectileSetControl(AdvanceProjectileSetControlEvent event) {
        event.setOverrideGlowColor(false);
    }
}
