package com.azerl0.gutitubow.gameevent.events;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.api.game.Game;
import com.azerl0.gutitubow.damage.DamageSource;
import com.azerl0.gutitubow.damage.GutitubowDamageType;
import com.azerl0.gutitubow.game.GamePhase;
import com.azerl0.gutitubow.gameevent.GameEventContext;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.gameevent.GameEvent;
import com.azerl0.gutitubow.gameevent.GameEventType;
import com.azerl0.gutitubow.util.EntityUtil;
import com.azerl0.gutitubow.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.jetbrains.annotations.NotNull;


public class BomboEvent extends GameEvent {
    public BomboEvent(GameEventContext context) {
        super(context);
    }

    @Override
    public @NotNull GameEventType getEventType() {
        return GameEventType.BOMB;
    }

    @Override
    protected void onActivate() {

    }

    @Override
    protected void onDeactivated() {

    }

    @Override
    protected void onArrowHit(ProjectileHitEvent event) {
        Location hitLocation = event.getEntity().getLocation();
        Game game = Gutitubow.get().getGame();

        if (event.getEntity().getShooter() instanceof Player shooter) {
            for (Player nearPlayer : hitLocation.getNearbyPlayers(2)) {

                if (game.isTeammate(nearPlayer, shooter)) continue;
                if (game.getPhase() == GamePhase.PHASE0 && game.isStarted()) continue;
                if (EntityUtil.isInvalidHitEntity(nearPlayer)) continue;

                GamePlayer damageGamePlayer = game.getGamePlayer(nearPlayer);
                DamageSource damageSource = DamageSource.builder(GutitubowDamageType.BOMB_BLAST)
                        .directPlayer(shooter)
                        .damageLocation(MathUtil.rayTraceEntityHit(hitLocation, nearPlayer))
                        .distance(hitLocation.distance(nearPlayer.getLocation()))
                        .build();
                damageGamePlayer.damage(damageSource, 1);
            }
            World world = hitLocation.getWorld();
            
            world.spawnParticle(
                    Particle.EXPLOSION,
                    hitLocation,
                    1,
                    0, 0, 0,
                    0,
                    null,
                    true
            );
            world.playSound(hitLocation, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
        }
    }
}
