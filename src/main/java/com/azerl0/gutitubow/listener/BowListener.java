package com.azerl0.gutitubow.listener;

import com.azerl0.gutitubow.ability.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

public class BowListener implements Listener {

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;

        String id = AbilityManager.getAbilityId(player);
        if (id == null) return;

        Ability ability = AbilityRegistry.get(id);
        if (ability == null) return;

        ability.onArrowLaunch(event);
    }
}