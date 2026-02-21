package com.azerl0.gutitubow.listener;

import com.azerl0.gutitubow.ability.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BowListener implements Listener {

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;

        Ability ability = AbilityManager.getAbility(player);
        if (ability == null) return;

        ability.onArrowLaunch(event);
    }

    @EventHandler
    public void onPlayerRightClickBow(PlayerInteractEvent event) {
        if (!(event.getItem() != null &&
                event.getItem().getType() == Material.BOW)) return;

        Action action = event.getAction();
        if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return;

        Ability ability = AbilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onPlayerRightClickBow(event);

    }

    @EventHandler
    public void onPlayerLeftClickBow(PlayerInteractEvent event) {
        if (!(event.getItem() != null &&
                event.getItem().getType() == Material.BOW)) return;

        Action action = event.getAction();
        if (!(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) return;

        Ability ability = AbilityManager.getAbility(event.getPlayer());
        if(ability != null) ability.onPlayerLeftClickBow(event);

    }
}