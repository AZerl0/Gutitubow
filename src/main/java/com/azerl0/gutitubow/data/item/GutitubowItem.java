package com.azerl0.gutitubow.data.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.ToolComponent;

import java.util.ArrayList;

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

    public static final ItemStack GUTITUBOW;
    static {
        ItemStack item = new ItemStack(Material.BOW);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.INFINITY, 1, true);
            meta.displayName(Component.text("ぐちつボウ").decoration(TextDecoration.ITALIC, false));
            meta.setUnbreakable(true);
            meta.setEnchantmentGlintOverride(false);
            ToolComponent tool = meta.getTool();
            tool.setRules(new ArrayList<>());
//            tool.setCanDestroyBlocksInCreative();
            tool.setDamagePerBlock(0);
            tool.setDefaultMiningSpeed(0.0f);
            meta.setTool(tool);

            item.setItemMeta(meta);
        }
        GUTITUBOW = item;
    }
}
