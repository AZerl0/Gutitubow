package com.azerl0.gutitubow.gameevent.events;

import com.azerl0.gutitubow.event.gameplayer.GamePlayerShootEvent;
import com.azerl0.gutitubow.gameevent.GameEvent;
import com.azerl0.gutitubow.gameevent.GameEventContext;
import com.azerl0.gutitubow.gameevent.GameEventType;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.util.ArrowUtil;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.Set;

public class ShotBowEvent extends GameEvent {

    public ShotBowEvent(GameEventContext context) {
        super(context);
    }

    @Override
    public @NotNull GameEventType getEventType() {
        return GameEventType.SHOT_BOW;
    }

    @Override
    protected void onActivate() {

    }

    @Override
    protected void onDeactivated() {

    }

    @Override
    protected void onGamePlayerShoot(GamePlayerShootEvent event) {
        if (event.isCancelled()) return;
        Set<GamePlayerShootEvent.ShootReason> bypassReasons = Set.of(
                GamePlayerShootEvent.ShootReason.SHOT_BOW_EVENT,
                GamePlayerShootEvent.ShootReason.FLASH,
                GamePlayerShootEvent.ShootReason.GRAPPLING_HOOK,
                GamePlayerShootEvent.ShootReason.INVERSION_BALL
        );
        if (bypassReasons.contains(event.getShootReason())) return;

        Player player = event.getPlayer();
        GamePlayer gamePlayer = event.getGamePlayer();
        Vector baseVelocity = event.getDirection().clone();
        if (event.getProjectile() != null) {
            baseVelocity = event.getProjectile().getVelocity();
        }
        double offsetSize = 0.3;
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            Location location = player.getEyeLocation().clone().add(
                    random.nextDouble(-offsetSize, offsetSize),
                    random.nextDouble(-offsetSize, offsetSize),
                    random.nextDouble(-offsetSize, offsetSize)
            );
            Vector velocity = addSpread(baseVelocity.clone());
            Arrow arrow = player.getWorld().spawn(location, Arrow.class);
            arrow.setShooter(player);
            arrow.setVelocity(velocity);
            ArrowUtil.setFixRotation(arrow, velocity);
            boolean result = gamePlayer.shoot(GamePlayerShootEvent.ShootReason.SHOT_BOW_EVENT, location, velocity, event.getForce(), arrow);
            if (!result) arrow.remove();
        }
        if (event.getProjectile() != null) event.getProjectile().remove();
        event.setCancelled(true);
    }

    public static Vector addSpread(Vector vector) {
        Random random = new Random();
        double noiseSize = 0.1;
        return vector.clone().add(new Vector(
                random.nextGaussian() * noiseSize,
                random.nextGaussian() * noiseSize,
                random.nextGaussian() * noiseSize
        ));
    }
}
