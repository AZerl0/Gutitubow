package com.azerl0.gutitubow.gameevent;

import com.azerl0.gutitubow.gameevent.events.PistolEvent;
import com.azerl0.gutitubow.gameevent.events.RicochetEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.function.Supplier;

public enum GameEventType {

    RICOCHET(
            Component.text("Let`s Bounce!"),
            List.of(
                    Component.text("矢の反発係数がおかしくなってしまった！"),
                    Component.text("壁に当たった矢がとんでもなく跳ね回るぞ！")
            ),
            Component.text("矢が縦横無尽に跳ね回る！"),
            ItemStack.of(Material.SLIME_BALL),
            RicochetEvent::new
    ),
    PISTOL(
            Component.text("ピストルラウンド"),
            List.of(
                    Component.text("このフェーズ中、発射される矢の勢いがとんでもなくなる。"),
                    Component.text("時代遅れの矢とはおさらばだ！")
            ),
            Component.text("矢の弾速が超高速になった！"),
            ItemStack.of(Material.WIND_CHARGE),
            PistolEvent::new
    );


    private final Component name;
    private final List<Component> lore;
    private final Component about;
    private final ItemStack baseDisplayItem;
    private final Supplier<? extends GameEvent> supplier;


    GameEventType(Component name, List<Component> lore, Component about, ItemStack baseDisplayItem, Supplier<? extends GameEvent> supplier) {
        this.name = name;
        this.lore = lore;
        this.about = about;
        this.baseDisplayItem = baseDisplayItem;
        this.supplier = supplier;
    }

    public Component getName() {
        return name;
    }

    public List<Component> getLore() {
        return lore;
    }

    public Component getAbout() {
        return about;
    }

    public ItemStack getDisplayItem() {
        ItemStack itemStack = baseDisplayItem.clone();
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            meta.lore(lore);
        }
        itemStack.setItemMeta(meta);
        return itemStack;

    }

    public GameEvent create() {
        return supplier.get();
    }
}
