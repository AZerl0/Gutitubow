package com.azerl0.gutitubow.gui.guis;

import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.game.GameManager;
import com.azerl0.gutitubow.gui.AbstractGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GameGUI extends AbstractGUI {
    private final GameManager gameManager;
    private final Game game;

    public GameGUI(GameManager gameManager, Game game) {
        super(27, Component.text("Game menu"));
        this.gameManager = gameManager;
        this.game = game;


        addButton(0, getGameStartButtonItem(), event -> {
            gameManager.startGame(game);
        });
        addButton(8, getGameResetButtonItem(), event -> {
            gameManager.resetGame(game);
        });
    }

    private ItemStack getGameStartButtonItem() {
        ItemStack itemStack = new ItemStack(Material.BOW);

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("ゲーム開始").decoration(TextDecoration.ITALIC, false));
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private ItemStack getGameResetButtonItem() {
        ItemStack itemStack = new ItemStack(Material.REDSTONE_BLOCK);

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("リセット").decoration(TextDecoration.ITALIC, false));
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

}
