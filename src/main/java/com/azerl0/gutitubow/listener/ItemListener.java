package com.azerl0.gutitubow.listener;

import com.azerl0.gutitubow.data.item.GutitubowItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class ItemListener implements Listener {

    @EventHandler
    public void usePotion(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() != Material.POTION) return;

        Player player = event.getPlayer();
        event.setReplacement(ItemStack.of(Material.AIR));
        player.setHealth(player.getHealth() + 2.0);
    }
}
