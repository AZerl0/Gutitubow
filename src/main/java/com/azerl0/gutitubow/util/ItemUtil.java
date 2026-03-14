package com.azerl0.gutitubow.util;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemUtil {
    private static NamespacedKey cantDropKey;
    private static NamespacedKey cantMoveKey;

    public static void init(JavaPlugin plugin) {
        cantDropKey = new NamespacedKey(plugin, "item.cant_drop");
        cantMoveKey = new NamespacedKey(plugin, "item.cant_move");
    }

    public static boolean isCantDropItem(ItemStack itemStack) {
        return itemStack.getPersistentDataContainer().getOrDefault(cantDropKey, PersistentDataType.BOOLEAN, false);
    }

    public static void setCantDrop(ItemStack itemStack, boolean cantDrop) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            if (cantDrop) {
                meta.getPersistentDataContainer().set(cantDropKey, PersistentDataType.BOOLEAN, true);
            } else meta.getPersistentDataContainer().remove(cantDropKey);
        }
        itemStack.setItemMeta(meta);
    }

    public static boolean isCantMoveItem(ItemStack itemStack) {
        return itemStack.getPersistentDataContainer().getOrDefault(cantMoveKey, PersistentDataType.BOOLEAN, false);
    }

    public static ItemStack setCantMove(ItemStack itemStack, boolean cantMove) {
        ItemMeta meta = itemStack.getItemMeta();
        if (cantMove) {
            meta.getPersistentDataContainer().set(cantMoveKey, PersistentDataType.BOOLEAN, true);
        } else meta.getPersistentDataContainer().remove(cantMoveKey);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
