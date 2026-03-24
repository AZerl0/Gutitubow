package com.azerl0.gutitubow.gui.guis;

import com.azerl0.gutitubow.gui.AbstractGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.function.Consumer;

public class TeamPlayerSelectorGUI extends AbstractGUI {
    private Consumer<Player> onSelect;

    public TeamPlayerSelectorGUI(Consumer<Player> onSelect) {
        super(54, Component.text("プレイヤーを選択"));
        this.onSelect = onSelect;
    }

    public void update() {
        inventory.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
            meta.setOwningPlayer(player);
            meta.displayName(Component.text(player.getName()).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("[クリック] このプレイヤーを選択").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
            ));
            itemStack.setItemMeta(meta);
            if (hasEmptySlot()) {
                addButton(getEmptySlot(), itemStack, event -> {
                    onSelect.accept(player);
                });
            }
        }
    }

    @Override
    public void open(Player player) {
        update();
        super.open(player);
    }


}
