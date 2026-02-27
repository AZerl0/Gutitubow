package com.azerl0.gutitubow.ability;


import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public interface Ability {
    AbilityType getType();

    default void onArrowLaunch(EntityShootBowEvent event) {}

    default void onArrowHit(ProjectileHitEvent event) {}

    default void onPlayerRightClickBow(PlayerInteractEvent event) {}

    default void onPlayerLeftClickBow(PlayerInteractEvent event) {}

    default void onMove(PlayerMoveEvent event) {}

    default void onToggleSprint(PlayerToggleSprintEvent event) {}

    default void onJump(PlayerJumpEvent event) {}

    default void onPressJumpKey(PlayerInputEvent event) {}
}

