package com.azerl0.gutitubow.gui.guis;

import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.game.GameManager;
import com.azerl0.gutitubow.game.GutitubowGameMode;
import com.azerl0.gutitubow.gui.AbstractGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.List;

public class GameGUI extends AbstractGUI {
    private final GameManager gameManager;
    private final Game game;

    final int GAME_START_BUTTON_SLOT = 0;
    final int TOGGLE_GAME_MODE_BUTTON_SLOT = 2;
    final int GAME_RESET_BUTTON_SLOT = 8;
    final int ABILITY_SWITCH_BUTTON_SLOT = 18;
    final int DEFAULT_HEALTH_BUTTON_SLOT = 19;
    final int PHASE0_SECOND_BUTTON_SLOT = 20;
    final int CAREPACKAGE_AMOUNT_BUTTON_SLOT = 21;
    final int MAP_SELECTOR_BUTTON_SLOT = 22;
    final int GAME_EVENT_SETTING_BUTTON_SLOT = 23;
    final int SPECTATOR_SETTING_BUTTON_SLOT = 25;


    public GameGUI(GameManager gameManager, Game game) {
        super(27, Component.text("Game menu"));
        this.gameManager = gameManager;
        this.game = game;




        addButton(GAME_START_BUTTON_SLOT, getAbstractSimpleButtonItem(Material.BOW, Component.text("ゲーム開始")), event -> {
            gameManager.startGame(game);
        });
        addButton(
                GAME_RESET_BUTTON_SLOT,
                getAbstractSimpleButtonItem(Material.BOW, Component.text("リセット")), event -> {
                    gameManager.resetGame(game);
                });
        addButton(ABILITY_SWITCH_BUTTON_SLOT, getAbstractSimpleButtonItem(Material.NETHER_STAR, Component.text("能力スイッチ")), event -> {
            if (!(event.getWhoClicked() instanceof Player clicker)) return;
            game.getGameSetting().getAbilityDisableGUI().open(clicker);
        });
        addButton(SPECTATOR_SETTING_BUTTON_SLOT,
                getAbstractSimpleButtonItem(Material.ENDER_EYE, Component.text("観戦者設定")),
                event -> {
                    if (!(event.getWhoClicked() instanceof Player clicker)) return;
                    game.getSpectatorManager().getSpectatorsGUI().open(clicker);
                });
        updateToggleGameModeButton();
        updateSetDefaultHealthButton();
        updatePhase0SecondsButton();
        updateCarePackageAmountButton();
        addButton(
                MAP_SELECTOR_BUTTON_SLOT,
                getAbstractSimpleButtonItem(Material.FILLED_MAP, Component.text("マップ選択")),
                event -> {
                    if (!(event.getWhoClicked() instanceof Player clicker)) return;
                    game.getGameMapSelectorGUI().open(clicker);
                }
        );
        addButton(
                GAME_EVENT_SETTING_BUTTON_SLOT,
                getAbstractSimpleButtonItem(Material.FIREWORK_ROCKET, Component.text("ゲームイベント設定")),
                event -> {
                    if (!(event.getWhoClicked() instanceof Player clicker)) return;
                    game.getGameSetting().getGameEventController().openGameEventGUI(clicker);
                }
        );
    }


    private void updateToggleGameModeButton() {
        addButton(TOGGLE_GAME_MODE_BUTTON_SLOT, getToggleGameModeButtonItem(), event -> toggleGameMode());
        GutitubowGameMode gameMode = game.getGameSetting().getGameMode();
        if (gameMode == GutitubowGameMode.DUO || gameMode == GutitubowGameMode.TRIO) {
            addButton(3, getAbstractSimpleButtonItem(Material.BOW, Component.text("チーム選択")), event -> {
                TeamGUI teamGUI = game.getTeamManager().getTeamGUI();
                if (event.getWhoClicked() instanceof Player player) {
                    teamGUI.open(player);
                }
            });
            addButton(4, getAbstractSimpleButtonItem(Material.BOW, Component.text("ランダムチーム")), event -> {
                game.getTeamManager().randomTeam();
            });
            addButton(5, getAbstractSimpleButtonItem(Material.BOW, Component.text("チームリセット")), event -> {
                game.getTeamManager().resetTeams();
            });
        } else {
            removeButton(3);
            removeButton(4);
            removeButton(5);
        }
    }

    private void toggleGameMode() {
        switch (game.getGameSetting().getGameMode()) {
            case SOLO -> game.setGameMode(GutitubowGameMode.DUO);
            case DUO -> game.setGameMode(GutitubowGameMode.TRIO);
            case TRIO -> game.setGameMode(GutitubowGameMode.SOLO);
            default -> game.setGameMode(GutitubowGameMode.SOLO);
        }
        updateToggleGameModeButton();
    }

    private void updateSetDefaultHealthButton() {
        ItemStack itemStack = getAbstractSimpleButtonItem(Material.APPLE, Component.text("デフォルトの体力"));
        double health = game.getGameSetting().getDefaultHealth();
        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(
                Component.text("§r§7現在の体力 - §c♥§7x" + (int) health / 2),
                Component.text("§r§7[左クリック] デフォルトの体力を §c♥§7x1 増やす"),
                Component.text("§r§7[右クリック] デフォルトの体力を §c♥§7x1 減らす")
        ));
        itemStack.setAmount((int) health / 2);
        itemStack.setItemMeta(meta);

        addButton(DEFAULT_HEALTH_BUTTON_SLOT, itemStack, this::clickDefaultHealthButton);
    }

    private void clickDefaultHealthButton(InventoryClickEvent event) {
        double currentHealth = game.getGameSetting().getDefaultHealth();
        double add = event.getClick() == ClickType.LEFT ? 2 : -2;
        double newHealth = Math.max(2, currentHealth + add);
        game.getGameSetting().setDefaultHealth(newHealth);
        updateSetDefaultHealthButton();
    }

    private void updatePhase0SecondsButton() {
        ItemStack itemStack = getAbstractSimpleButtonItem(Material.BELL, Component.text("フェーズ0 待機時間"));
        ItemMeta meta = itemStack.getItemMeta();
        int currentSeconds = game.getGameSetting().getPhase0seconds();
        meta.lore(List.of(
                Component.text("§r§7現在の時間 - §b" + currentSeconds + "秒"),
                Component.text("§r§7[左クリック] 時間を §b5秒§7 増やす"),
                Component.text("§r§7[右クリック] 時間を §b5秒§7 減らす")
        ));
        itemStack.setAmount(currentSeconds);
        itemStack.setItemMeta(meta);

        addButton(PHASE0_SECOND_BUTTON_SLOT, itemStack, this::clickPhase0secondsButton);
    }

    private void clickPhase0secondsButton(InventoryClickEvent event) {
        int currentSeconds = game.getGameSetting().getPhase0seconds();
        int add = event.getClick() == ClickType.LEFT ? 5 : -5;
        int newSeconds = Math.max(5, currentSeconds + add);
        game.getGameSetting().setPhase0seconds(newSeconds);
        updatePhase0SecondsButton();
    }

    private void updateCarePackageAmountButton() {
        ItemStack itemStack = getAbstractSimpleButtonItem(Material.POTION, Component.text("投下されるケアパッケージ数"));
        PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
        int currentAmount = game.getGameSetting().getCarePackageAmount();
        meta.lore(List.of(
                Component.text("§r§7現在の数 - §b" + currentAmount),
                Component.text("§r§7[左クリック] 数を §b1§7 増やす"),
                Component.text("§r§7[右クリック] 数を §b1§7 減らす")
        ));
        meta.setColor(Color.RED);
        meta.setMaxStackSize(99);
        itemStack.setAmount(currentAmount == 0 ? 1 : currentAmount);
        itemStack.setItemMeta(meta);

        addButton(CAREPACKAGE_AMOUNT_BUTTON_SLOT, itemStack, this::clickCarePackageAmountButton);
    }

    private void clickCarePackageAmountButton(InventoryClickEvent event) {
        int current = game.getGameSetting().getCarePackageAmount();
        int add = event.getClick() == ClickType.LEFT ? 1 : -1;
        int newAmount = Math.max(0, current + add);
        game.getGameSetting().setCarePackageAmount(newAmount);
        updateCarePackageAmountButton();
    }


    private ItemStack getToggleGameModeButtonItem() {
        String displayName = "NULL";
        switch (game.getGameSetting().getGameMode()) {
            case SOLO -> displayName = "SOLOモード";
            case DUO -> displayName = "DUOモード";
            case TRIO -> displayName = "TRIOモード";
        }
        return getAbstractSimpleButtonItem(Material.BOW, Component.text(displayName));
    }
}
