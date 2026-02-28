package com.azerl0.gutitubow.data.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GutitubowItem {

    public static final ItemStack RECOVERY_POTION;
    static {
        ItemStack item = new ItemStack(Material.POTION);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("回復のポーション").decoration(TextDecoration.ITALIC, false));
            item.setItemMeta(meta);
        }
        RECOVERY_POTION = item;
    }
}
