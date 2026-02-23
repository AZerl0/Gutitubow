package com.azerl0.gutitubow.listener;

import com.azerl0.gutitubow.manager.LogManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class GutitubowPlayerListener implements Listener {

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        Entity shooter = (Entity) event.getEntity().getShooter();
        Entity tar = event.getHitEntity();

        if (shooter == null || tar == null) return;

        if (shooter == tar) return;
        if (tar instanceof Enderman) return;

        double distance = shooter.getLocation().distance(tar.getLocation());

        LogManager.broadcastHitLog(shooter, tar, distance);


    }
}
