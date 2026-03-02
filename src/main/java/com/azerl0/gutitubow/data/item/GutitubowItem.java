package com.azerl0.gutitubow.data.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

public class GutitubowItem {

    public static final ItemStack RECOVERY_POTION;
    static {
        ItemStack item = new ItemStack(Material.POTION);

        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("回復のポーション").decoration(TextDecoration.ITALIC, false));
            meta.setColor(Color.RED);
            item.setItemMeta(meta);
        }
        RECOVERY_POTION = item;
    }
}
