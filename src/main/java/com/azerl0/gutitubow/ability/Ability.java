package com.azerl0.gutitubow.ability;


import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;

public interface Ability {
    AbilityType getType();

    default void init(Player player) {}

    default void reset(Player player) {}

    default void onArrowLaunch(EntityShootBowEvent event) {}

    default void onArrowHit(ProjectileHitEvent event) {}

    default void onPlayerRightClickBow(PlayerInteractEvent event) {}

    default void onPlayerLeftClickBow(PlayerInteractEvent event) {}

    default void onMove(PlayerMoveEvent event) {}

    default void onToggleSprint(PlayerToggleSprintEvent event) {}

    default void onJump(PlayerJumpEvent event) {}

    default void onPressJumpKey(PlayerInputEvent event) {}

    default void onToggleFlight(PlayerToggleFlightEvent event) {}
}

