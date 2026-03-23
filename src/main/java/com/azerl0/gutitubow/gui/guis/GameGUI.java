package com.azerl0.gutitubow.gui.guis;

import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.game.GameManager;
import com.azerl0.gutitubow.game.GutitubowGameMode;
import com.azerl0.gutitubow.gui.AbstractGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
        addButton(
                8,
                getAbstractSimpleButtonItem(Material.BOW, Component.text("リセット")), event -> {
                    gameManager.resetGame(game);
                });
        updateToggleGameModeButton();
    }


    private void updateToggleGameModeButton() {
        addButton(2, getToggleGameModeButtonItem(), event -> toggleGameMode());
        if (game.getGameMode() == GutitubowGameMode.DUO || game.getGameMode() == GutitubowGameMode.TRIO) {
            addButton(3, getAbstractSimpleButtonItem(Material.BOW, Component.text("チームセレクター")), event -> {
                TeamGUI teamGUI = game.getTeamManager().getTeamGUI();
                if (event.getWhoClicked() instanceof Player player) {
                    teamGUI.open(player);
                }
            });
        } else {
            removeButton(3);
            removeButton(4);
        }
    }

    private void toggleGameMode() {
        switch (game.getGameMode()) {
            case SOLO -> game.setGameMode(GutitubowGameMode.DUO);
            case DUO -> game.setGameMode(GutitubowGameMode.TRIO);
            case TRIO -> game.setGameMode(GutitubowGameMode.SOLO);
            case null, default -> game.setGameMode(GutitubowGameMode.SOLO);
        }
        updateToggleGameModeButton();
    }

    private ItemStack getGameStartButtonItem() {
        return getAbstractSimpleButtonItem(Material.BOW, Component.text("ゲーム開始"));
    }


    private ItemStack getToggleGameModeButtonItem() {
        String displayName = "NULL";
        switch (game.getGameMode()) {
            case SOLO -> displayName = "SOLOモード";
            case DUO -> displayName = "DUOモード";
            case TRIO -> displayName = "TRIOモード";
        }
        return getAbstractSimpleButtonItem(Material.BOW, Component.text(displayName));
    }
}
