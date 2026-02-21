package com.azerl0.gutitubow.listener;

import com.azerl0.gutitubow.ability.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

public class BowListener implements Listener {

    private final AbilityRegistry registry;
    private final AbilityManager manager;

    public BowListener(AbilityRegistry registry, AbilityManager manager) {
        this.registry = registry;
        this.manager = manager;
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;

        String id = manager.getAbilityId(player);
        if (id == null) return;

        Ability ability = registry.get(id);
        if (ability == null) return;

        ability.onArrowLaunch(event);
    }
}