package com.azerl0.gutitubow.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractGUI implements InventoryHolder {
    protected final Inventory inventory;
    protected final Map<Integer, GUIButton> buttons = new HashMap<>();

    public AbstractGUI(int size, Component title) {
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    public void addButton(int slot, ItemStack itemStack, Consumer<InventoryClickEvent> action) {
        GUIButton button = new GUIButton(itemStack, action);

        buttons.put(slot, button);
        inventory.setItem(slot, itemStack);
    }

    public void click(InventoryClickEvent event) {
        GUIButton button = buttons.get(event.getSlot());

        if (button != null) {
            button.click(event);
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
        GUIManager.register(inventory, this);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public int getEmptySlot() {
        for (int i = 0; i< inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR) return i;
        }
        return -1;
    }

    public boolean hasEmptySlot() {
        return getEmptySlot() != -1;
    }

}
