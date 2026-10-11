package com.azerl0.gutitubow.gameevent;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.data.item.GutitubowItem;
import com.azerl0.gutitubow.gameevent.events.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
    ),
    BOMB(
            Component.text("PARTY TIME!"),
            List.of(
                    Component.text("支給される矢に爆薬が塗布されたようだ！"),
                    Component.text("着弾した場所で大きな爆発を起こすぞ！")
            ),
            Component.text("支給の矢に爆薬が塗られた！"),
            ItemStack.of(Material.TNT),
            BomboEvent::new
    ),
    WANTED(
            Component.text("バウンティ・ハンター"),
            List.of(
                    Component.text("暴れすぎたヤツらが賞金首に指定されたようだ。"),
                    Component.text("賞金首を倒して報酬を手に入れろ！")
            ),
            Component.text("賞金首を討ち取れ！"),
            ItemStack.of(Material.GOAT_HORN),
            WantedEvent::new
    ),
    SKIN(
            Component.text("かわいいお顔"),
            List.of(
                    Component.text("全員のスキンが同じになってしまった！")
            ),
            Component.text(""),
            ItemStack.of(Material.PIGLIN_HEAD),
            SkinEvent::new
    ),
    CAREPACKAGE(
            Component.text("救援物資到着"),
            List.of(
                    Component.text("このフェーズ中、大量のケアパッケージが投下される。"),
                    Component.text("ただし、中には粗悪品も含まれているので気をつけるように。")
            ),
            Component.text("大量の救援物資が投下された！"),
            GutitubowItem.RED_POTION,
            CarePackageEvent::new
    ),
    DOPPELGANGER(
            Component.text("ドッペルゲンガー出現"),
            List.of(
                    Component.text("君の偽物がこのワールドのどこかに出現したようだ。"),
                    Component.text("フェーズ終了時までに偽物を倒せないと君は消滅してしまう。")
            ),
            Component.text("ドッペルゲンガーが現れた！"),
            ItemStack.of(Material.PLAYER_HEAD),
            DoppelgangerEvent::new
    );


    private final Component name;
    private final List<Component> lore;
    private final Component about;
    private final ItemStack baseDisplayItem;
    private final Function<GameEventContext, ? extends GameEvent> function;


    /**
     * :
     * @param name イベントの名前、チャットの説明欄やGUI上の名前として表示される
     * @param lore イベントの詳細、チャット欄やGUI上のLoreで表示される
     * @param about イベントの概要、イベント発動時のサブタイトルに表示される
     * @param baseDisplayItem イベントのアイテムアイコン
     * @param function イベントのsupplier
     */
    GameEventType(Component name, List<Component> lore, Component about, ItemStack baseDisplayItem, Function<GameEventContext, GameEvent> function) {
        this.name = name;
        this.lore = lore;
        this.about = about;
        this.baseDisplayItem = baseDisplayItem;
        this.function = function;
    }

    public Component getName() {
        return name.decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
    }

    public List<Component> getLore() {
        List<Component> loreList = new ArrayList<>();
        lore.forEach(component -> loreList.add(component.decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE)));
        return loreList;
    }

    public Component getAbout() {
        return about;
    }

    public ItemStack getDisplayItem() {
        ItemStack itemStack = baseDisplayItem.clone();
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.displayName(getName());
            meta.lore(getLore());
        }
        itemStack.setItemMeta(meta);
        return itemStack.clone();

    }

    public GameEvent create() {
        GameEventContext context = new GameEventContext(
                Gutitubow.get(),
                Gutitubow.get().getGame()
        );
        return function.apply(context);
    }
}
