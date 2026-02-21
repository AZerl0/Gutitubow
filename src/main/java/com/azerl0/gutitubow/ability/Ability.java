package com.azerl0.gutitubow.ability;


import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public interface Ability {
    String getId();

    default void onArrowLaunch(EntityShootBowEvent event) {}

    default void onArrowHit(ProjectileHitEvent event) {}

    default void onRightClick(PlayerInteractEvent event) {}
}
