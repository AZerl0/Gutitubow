package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.data.damage.DamageSource;
import com.azerl0.gutitubow.data.damage.GutitubowDamageType;
import com.azerl0.gutitubow.game.GamePlayer;
import com.azerl0.gutitubow.util.EntityUtil;
import com.azerl0.gutitubow.util.MathUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class BombAbility extends Ability {
    public BombAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public final @NotNull AbilityType getType() {
        return AbilityType.BOMB;
    }

    @Override
    public void onShoot(EntityShootBowEvent event) {
        Arrow arrow = (Arrow) event.getProjectile();
        EntityUtil.setGlow(arrow, NamedTextColor.RED);
        arrow.setGlowing(true);
        arrow.getWorld().playSound(arrow.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);
    }

    @Override
    public void onPlayerLeftClickBow(PlayerInteractEvent event) {

        for (World world : Bukkit.getWorlds()) {
            for (Arrow arrow : world.getEntitiesByClass(Arrow.class)) {
                if (!(arrow.getShooter() instanceof Player shooter)) continue;
                if (!shooter.getUniqueId().equals(event.getPlayer().getUniqueId())) continue;
                explode(arrow);

            }
        }

    }

    private void explode(Arrow arrow) {
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        World world = arrow.getWorld();
        Location loc = arrow.getLocation().clone();

        double knockbackRadius = 3;
        for (Entity entity : world.getNearbyEntities(loc.add(0, -0.2, 0), knockbackRadius, knockbackRadius, knockbackRadius)) {
            if (entity instanceof LivingEntity living) {

                Vector knockback = living.getLocation().toVector()
                        .subtract(loc.toVector())
                        .normalize()
                        .multiply(1.5);

                living.setVelocity(knockback);
            }
        }

        for (Player damagePlayer : loc.getNearbyPlayers(2)) {

            GamePlayer gamePlayer = context.game().getGamePlayer(damagePlayer);
            if (gamePlayer.isTeammate(playerUUID)) continue;
            if (context.game().getPhase() == 0 && context.game().isStarted()) continue;

            if (!shooter.getUniqueId().equals(damagePlayer.getUniqueId()) && !EntityUtil.isInvalidHitEntity(damagePlayer)) {
                DamageSource damageSource = DamageSource.builder()
                        .damageType(GutitubowDamageType.BOMB_BLAST)
                        .directPlayer(shooter)
                        .damageLocation(MathUtil.rayTraceEntityHit(loc, damagePlayer))
                        .distance(loc.distance(damagePlayer.getLocation()))
                        .build();
                gamePlayer.damage(damagePlayer, 0.5, damageSource, true);
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
}
