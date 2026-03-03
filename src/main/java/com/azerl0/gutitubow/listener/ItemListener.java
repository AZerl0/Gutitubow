package com.azerl0.gutitubow.listener;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ItemListener implements Listener {

    @EventHandler
    public void usePotion(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() != Material.POTION) return;

        Player player = event.getPlayer();
        event.setReplacement(ItemStack.of(Material.AIR));
        double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getBaseValue();
        if (player.getHealth() + 2.0 > maxHealth) {
            player.setHealth(maxHealth);
        } else player.setHealth(player.getHealth() + 2.0);
    }
}
