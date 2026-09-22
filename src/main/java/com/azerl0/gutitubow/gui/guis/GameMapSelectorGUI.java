package com.azerl0.gutitubow.gui.guis;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.gamemap.GameMap;
import com.azerl0.gutitubow.gui.AbstractGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GameMapSelectorGUI extends AbstractGUI {
    private final Game game;

    public GameMapSelectorGUI(Game game) {
        super(9, Component.text("マップ選択"));
        this.game = game;
        updateGUI();
    }

    @Override
    public void open(Player player) {
        updateGUI();
        super.open(player);
    }

    @Override
    public void updateGUI() {
        super.updateGUI();
        clear();
        for (GameMap gameMap : Gutitubow.get().getGameMapManager().getGameMaps()) {
            if (!hasEmptySlot()) return;
            ItemStack itemStack = ItemStack.of(Material.FILLED_MAP);
            ItemMeta meta = itemStack.getItemMeta();
            if (game.getGameMap() != null && game.getGameMap().getId().equals(gameMap.getId())) {
                meta.setEnchantmentGlintOverride(true);
                meta.lore(List.of(Component.text("選択中").color(NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)));
            } else {
                meta.lore(List.of(Component.text("クリックして選択").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
            }
            meta.displayName(gameMap.getName().decoration(TextDecoration.ITALIC, false));
            itemStack.setItemMeta(meta);
            addButton(getEmptySlot(), itemStack, event -> {
                game.setGameMap(gameMap);
                updateGUI();
            });
        }
    }
}
