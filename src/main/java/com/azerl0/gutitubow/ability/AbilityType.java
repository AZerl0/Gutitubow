package com.azerl0.gutitubow.ability;

import com.azerl0.gutitubow.data.item.GutitubowItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum AbilityType {
    BARRIER(Component.text("障壁"), GutitubowItem.ABILITY_ICON_BARRIER),
    BOMB(Component.text("爆弾"), GutitubowItem.ABILITY_ICON_BOMB),
    CONTROL(Component.text("操弾"), GutitubowItem.ABILITY_ICON_CONTROL),
    RAPIDFIRE(Component.text("速射"), GutitubowItem.ABILITY_ICON_RAPIDFIRE),
    RICOCHET(Component.text("跳弾"), GutitubowItem.ABILITY_ICON_RICOCHET),
    SNIPE(Component.text("狙撃"), GutitubowItem.ABILITY_ICON_SNIPE),
    DASH(Component.text("疾走"), GutitubowItem.ABILITY_ICON_DASH),
    JUMP(Component.text("跳躍"), GutitubowItem.ABILITY_ICON_JUMP),
    TELEPORT(Component.text("転移"), GutitubowItem.ABILITY_ICON_TELEPORT),
    ESCAPE(Component.text("脱出"), GutitubowItem.ABILITY_ICON_ESCAPE),
    FLOAT(Component.text("浮遊"), GutitubowItem.ABILITY_ICON_FLOAT),
    SHRINK(Component.text("縮身"), GutitubowItem.ABILITY_ICON_SHRINK),
    TOUGH(Component.text("強靭"), GutitubowItem.ABILITY_ICON_TOUGH),
    INVISIBLE(Component.text("隠密"), GutitubowItem.ABILITY_ICON_INVISIBLE);

    private final Component name;
    private final ItemStack displayItem;

    AbilityType(Component name, ItemStack displayItem) {
            this.name = name;
            this.displayItem = displayItem;
    }

    public Component getName() {
        return name;
    }

    public ItemStack getDisplayItem() {
        ItemStack itemStack = displayItem;
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));

        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
