package com.azerl0.gutitubow.event.gameplayer;

import com.azerl0.gutitubow.data.damage.DamageSource;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GamePlayerKillEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Player target;
    private final DamageSource damageSource;
    private Component killMessage;

    public GamePlayerKillEvent(@NotNull Player player,@NotNull Player target, @NotNull DamageSource damageSource, @NotNull Component killMessage) {
        this.player = player;
        this.target = target;
        this.damageSource = damageSource;
        this.killMessage = killMessage;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Player getTarget() {
        return target;
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
