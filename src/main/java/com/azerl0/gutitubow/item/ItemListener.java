package com.azerl0.gutitubow.item;

import com.azerl0.gutitubow.util.ItemUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Set;

public class ItemListener implements Listener {

    @EventHandler
    public void usePotion(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() != Material.POTION) return;

        Player player = event.getPlayer();
        event.setReplacement(ItemStack.of(Material.AIR));
        double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getBaseValue();
        player.setHealth(Math.min(player.getHealth() + 2.0, maxHealth));
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;
        ItemStack itemStack = event.getItemDrop().getItemStack();
        if (ItemUtil.isCantDropItem(itemStack)) event.setCancelled(true);

    }

    @EventHandler
    public void onItemMove(InventoryClickEvent event) {
        if (event.isCancelled()) return;

        if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE) return;

        ItemStack item;

        switch (event.getAction()) {
            case DROP_ALL_SLOT:
            case DROP_ONE_SLOT:
                item = event.getCurrentItem();
                break;

            case DROP_ALL_CURSOR:
            case DROP_ONE_CURSOR:
                item = event.getCursor();
                break;

            default:
                item = event.getCurrentItem();
        }

        if (item != null && ItemUtil.isCantDropItem(item) && dropAction.contains(event.getAction())) {
            event.setCancelled(true);
        }

        if (item != null && ItemUtil.isCantMoveItem(item)) {
            event.setCancelled(true);
        }
    }

    private final Set<InventoryAction> dropAction = Set.of(
            InventoryAction.DROP_ALL_CURSOR,
            InventoryAction.DROP_ALL_SLOT,
            InventoryAction.DROP_ONE_CURSOR,
            InventoryAction.DROP_ONE_SLOT
    );
}
