package com.azerl0.gutitubow.ability;


import com.azerl0.gutitubow.event.gameplayer.GamePlayerDamageEvent;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class Ability {
    protected UUID playerUUID;
    protected final AbilityContext context;

    public Ability(AbilityContext context) {
        this.context = context;
    }

    @NotNull
    public abstract AbilityType getType();

    public Player getPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }

    public void init(Player player) {}

    public void reset() {}

    public void onArrowLaunch(EntityShootBowEvent event) {}

    public void onArrowHit(ProjectileHitEvent event) {}

    public void onPlayerRightClickBow(PlayerInteractEvent event) {}

    public void onPlayerLeftClickBow(PlayerInteractEvent event) {}

    public void onMove(PlayerMoveEvent event) {}

    public void onToggleSneaking(PlayerToggleSneakEvent event) {}

    public void onToggleSprint(PlayerToggleSprintEvent event) {}

    public void onToggleFlight(PlayerToggleFlightEvent event) {}

    public void onJump(PlayerJumpEvent event) {}

    public void onPressJumpKey(PlayerInputEvent event) {}

    public void onInteract(PlayerInteractEvent event) {}

    public void onDamage(GamePlayerDamageEvent event) {}

}