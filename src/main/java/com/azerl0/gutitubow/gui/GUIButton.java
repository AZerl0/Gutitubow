package com.azerl0.gutitubow.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class GUIButton {
    private final ItemStack itemStack;
    private final Consumer<InventoryClickEvent> action;

    public GUIButton(ItemStack itemStack, Consumer<InventoryClickEvent> action) {
        this.itemStack = itemStack;
        this.action = action;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void click(InventoryClickEvent event) {
        action.accept(event);
    }
}
