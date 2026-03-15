package com.azerl0.gutitubow.gui;

import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class GUIManager {
    private static final Map<Inventory, AbstractGUI> guis = new HashMap<>();

    public static void register(Inventory inventory, AbstractGUI gui) {
        guis.put(inventory, gui);
    }

    public static AbstractGUI get(Inventory inventory) {
        return guis.get(inventory);
    }
}