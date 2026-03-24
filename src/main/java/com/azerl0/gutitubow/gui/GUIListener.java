package com.azerl0.gutitubow.gui;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class GUIListener implements Listener {
    private final AllGUI allGUI;
    private final NamespacedKey gamemasterKey = new NamespacedKey("gutitubow", "item.gamemaster");

    public GUIListener(AllGUI allGUI) {
        this.allGUI = allGUI;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        if (event.getClickedInventory() == event.getView().getBottomInventory() && event.getClick() == ClickType.SHIFT_LEFT) {
            event.setCancelled(true);
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof AbstractGUI gui) {
            gui.click(event);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack clickItem = event.getItem();
        if (clickItem!= null && clickItem.getItemMeta().getPersistentDataContainer().getOrDefault(gamemasterKey, PersistentDataType.BOOLEAN, false) && player.isOp()) {
            allGUI.gameGUI().open(player);
        }
    }
}
