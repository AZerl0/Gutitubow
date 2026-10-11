package com.azerl0.gutitubow.event.gameplayer;

import com.azerl0.gutitubow.damage.DamageSource;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GamePlayerKillEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Player target;
    private final GamePlayer gamePlayer;
    private final GamePlayer targetGamePlayer;
    private final DamageSource damageSource;
    private Component killMessage;

    public GamePlayerKillEvent(
            @NotNull Player player,
            @NotNull Player target,
            @NotNull GamePlayer gamePlayer,
            @NotNull GamePlayer targetGamePlayer,
            @NotNull DamageSource damageSource,
            @NotNull Component killMessage
    ) {
        this.player = player;
        this.target = target;
        this.gamePlayer = gamePlayer;
        this.targetGamePlayer = targetGamePlayer;
        this.damageSource = damageSource;
        this.killMessage = killMessage;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Player getTarget() {
        return target;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public GamePlayer getTargetGamePlayer() {
        return targetGamePlayer;
    }

    public @NotNull DamageSource getDamageSource() {
        return damageSource;
    }

    public @NotNull Component getKillMessage() {
        return killMessage;
    }

    public void setKillMessage(Component killMessage) {
        this.killMessage = killMessage;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
