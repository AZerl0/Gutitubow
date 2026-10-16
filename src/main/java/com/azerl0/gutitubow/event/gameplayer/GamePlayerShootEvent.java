package com.azerl0.gutitubow.event.gameplayer;

import com.azerl0.gutitubow.gameplayer.GamePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class GamePlayerShootEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final GamePlayer gamePlayer;
    private final Player player;
    private final ShootReason shootReason;
    private final Location projectileLocation;
    private final Vector direction;
    private final float force;
    private @Nullable final Projectile projectile;
    private boolean cancelled;


    public GamePlayerShootEvent(GamePlayer gamePlayer, Player player, ShootReason shootReason, Location projectileLocation, Vector direction, float force, @Nullable Projectile projectile) {
        this.gamePlayer = gamePlayer;
        this.player = player;
        this.shootReason = shootReason;
        this.projectileLocation = projectileLocation;
        this.direction = direction;
        this.force = force;
        this.projectile = projectile;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public Player getPlayer() {
        return player;
    }

    public ShootReason getShootReason() {
        return shootReason;
    }

    public Location getProjectileLocation() {
        return projectileLocation;
    }

    public Vector getDirection() {
        return direction;
    }

    public float getForce() {
        return force;
    }

    public @Nullable Projectile getProjectile() {
        return projectile;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public enum ShootReason{
        DEFAULT,
        QUICK_FIRE,
        RAPID_FIRE,
        SNIPE,
        FLASH,
        GRAPPLING_HOOK,
        INVERSION_BALL,
        CONTROL_EVENT,
        SHOT_BOW_EVENT
    }
}
