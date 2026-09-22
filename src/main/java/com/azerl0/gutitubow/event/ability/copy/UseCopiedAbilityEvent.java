package com.azerl0.gutitubow.event.ability.copy;

import com.azerl0.gutitubow.ability.AbilityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class UseCopiedAbilityEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private AbilityType abilityType;

    public UseCopiedAbilityEvent(Player player, AbilityType abilityType) {
        this.player = player;
        this.abilityType = abilityType;
    }

    public Player getPlayer() {
        return player;
    }

    public AbilityType getAbilityType() {
        return abilityType;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
