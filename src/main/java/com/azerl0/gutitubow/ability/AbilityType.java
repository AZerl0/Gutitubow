package com.azerl0.gutitubow.ability;

import com.azerl0.gutitubow.data.item.GutitubowItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

public enum AbilityType {
    BARRIER(Component.text("障壁"), GutitubowItem.ABILITY_ICON_BARRIER),
    BOMB(Component.text("爆弾"), GutitubowItem.ABILITY_ICON_BOMB),
    CONTROL(Component.text("操弾"), GutitubowItem.ABILITY_ICON_CONTROL),
    QUICKFIRE(Component.text("速射"), GutitubowItem.ABILITY_ICON_QUICKFIRE),
    RICOCHET(Component.text("跳弾"), GutitubowItem.ABILITY_ICON_RICOCHET),
    SNIPE(Component.text("狙撃"), GutitubowItem.ABILITY_ICON_SNIPE),
    DASH(Component.text("疾走"), GutitubowItem.ABILITY_ICON_DASH),
    JUMP(Component.text("跳躍"), GutitubowItem.ABILITY_ICON_JUMP),
    TELEPORT(Component.text("転移"), GutitubowItem.ABILITY_ICON_TELEPORT),
    ESCAPE(Component.text("脱出"), GutitubowItem.ABILITY_ICON_ESCAPE),
    FLOAT(Component.text("浮遊"), GutitubowItem.ABILITY_ICON_FLOAT),
    SHRINK(Component.text("縮身"), GutitubowItem.ABILITY_ICON_SHRINK),
    TOUGH(Component.text("強靭"), GutitubowItem.ABILITY_ICON_TOUGH),
    INVISIBLE(Component.text("隠密"), GutitubowItem.ABILITY_ICON_INVISIBLE),
    SHIELD(Component.text("大盾"), GutitubowItem.ABILITY_ICON_SHIELD),
    FLASH(Component.text("閃光"), GutitubowItem.ABILITY_ICON_FLASH),
    GIANT(Component.text("巨体"), GutitubowItem.ABILITY_ICON_GIANT),
    PREDATION(Component.text("捕食"), GutitubowItem.ABILITY_ICON_PREDATION),
    BALLOON(Component.text("風船"), GutitubowItem.ABILITY_ICON_BALLOON),
    TRACKING(Component.text("追跡"), GutitubowItem.ABILITY_ICON_TRACKING),
    LASTSTAND(Component.text("背水"), GutitubowItem.ABILITY_ICON_LAST_STAND),
    DETECTION(Component.text("探知"), GutitubowItem.ABILITY_ICON_DETECTION),
    INVINCIBLE(Component.text("無敵"), GutitubowItem.ABILITY_ICON_INVINCIBLE),
    MEDUSA(Component.text("魔眼"), GutitubowItem.ABILITY_ICON_MEDUSA),
    RAPIDFIRE(Component.text("連射"), GutitubowItem.ABILITY_ICON_RAPIDFIRE),
    ECHO(Component.text("反響"), GutitubowItem.ABILITY_ICON_ECHO),
    CLONE(Component.text("分身"), GutitubowItem.ABILITY_ICON_CLONE),
    CHANGE(Component.text("変化"), GutitubowItem.ABILITY_ICON_CHANGE),
    COPY(Component.text("複製"), GutitubowItem.ABILITY_ICON_COPY),
    APPRAISAL(Component.text("鑑定"), GutitubowItem.ABILITY_ICON_APPRAISAL),
    SMASH(Component.text("必殺"), GutitubowItem.ABILITY_ICON_SMASH);

    private final Component name;
    private final ItemStack displayItem;

    AbilityType(@NotNull Component name, @NotNull ItemStack displayItem) {
            this.name = name;
            this.displayItem = displayItem;
    }
    @NotNull
    public Component getName() {
        return name;
    }

    public  @NotNull ItemStack getDisplayItem() {
        ItemStack itemStack = displayItem;
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));

        itemStack.setItemMeta(meta);
        return itemStack.clone();
    }

    public  @NotNull ItemStack getDisplayItem(Player player) {
        ItemStack itemStack = getDisplayItem();
        if (itemStack.getType() == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            skullMeta.setOwningPlayer(player);
            itemStack.setItemMeta(skullMeta);
        }
        return itemStack;
    }
}
