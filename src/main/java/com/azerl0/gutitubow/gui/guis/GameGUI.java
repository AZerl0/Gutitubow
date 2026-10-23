package com.azerl0.gutitubow.gui.guis;

import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.api.game.Game;
import com.azerl0.gutitubow.arena.Arena;
import com.azerl0.gutitubow.game.*;
import com.azerl0.gutitubow.gui.AbstractGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;
import java.util.List;

public class GameGUI extends AbstractGUI {
    private final Game game;

    final int GAME_START_BUTTON_SLOT = 0;
    final int TOGGLE_GAME_MODE_BUTTON_SLOT = 2;
    final int GAME_RESET_BUTTON_SLOT = 8;
    final int ORIGINAL_MAP_TELEPORT_BUTTON_SLOT = 15;
    final int ARENA_TELEPORT_BUTTON_SLOT = 16;
    final int ARENA_RESET_BUTTON_SLOT = 17;
    final int ABILITY_SWITCH_BUTTON_SLOT = 18;
    final int DEFAULT_HEALTH_BUTTON_SLOT = 19;
    final int PHASE0_SECOND_BUTTON_SLOT = 20;
    final int CAREPACKAGE_AMOUNT_BUTTON_SLOT = 21;
    final int MAP_SELECTOR_BUTTON_SLOT = 22;
    final int GAME_EVENT_SETTING_BUTTON_SLOT = 23;
    final int SINGLE_ABILITY_BUTTON_SLOT = 24;
    final int SPECTATOR_SETTING_BUTTON_SLOT = 26;


    public GameGUI(Game game) {
        super(27, Component.text("Game menu"));
        this.game = game;

        updateGUI();
    }

    @Override
    public void updateGUI() {
        addButton(GAME_START_BUTTON_SLOT, getAbstractSimpleButtonItem(Material.BOW, Component.text("ゲーム開始")), event -> {
            GameStartResult result = game.start();
            if (result == GameStartResult.MAP_IS_NULL) {
                event.getWhoClicked().sendMessage("マップが設定されていません");
            }
        });
        addButton(
                GAME_RESET_BUTTON_SLOT,
                getAbstractSimpleButtonItem(Material.BOW, Component.text("リセット")), event -> {
                    game.reset();
                });
        addButton(ORIGINAL_MAP_TELEPORT_BUTTON_SLOT, getAbstractSimpleButtonItem(Material.CARTOGRAPHY_TABLE, Component.text("アリーナの元のマップにテレポート")), event -> {
            if (!(event.getWhoClicked() instanceof Player clicker)) return;
            if (game.getGameMap() != null) clicker.teleport(game.getGameMap().getLobby());
        });
        addButton(ARENA_TELEPORT_BUTTON_SLOT, getAbstractSimpleButtonItem(Material.LODESTONE, Component.text("アリーナにテレポート")), event -> {
            if (!(event.getWhoClicked() instanceof Player clicker)) return;
            Arena arena = game.getArena();
            if (arena != null) {
                Location lobby = arena.getGameMap().getLobby().clone();
                lobby.setWorld(arena.getWorld());
                clicker.teleport(lobby);

            }
        });
        addButton(ARENA_RESET_BUTTON_SLOT, getAbstractSimpleButtonItem(Material.REDSTONE_BLOCK, Component.text("アリーナをリセット")), event -> {
            game.resetArena();
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
        updateSingleAbilityTypeButton();
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

    private void updateSingleAbilityTypeButton() {
        AbilityType singleAbility = getGameSetting().getSingleAbility();
        ItemStack itemStack;
        if (singleAbility == null) {
            itemStack = getAbstractSimpleButtonItem(Material.BARRIER, Component.text("全員同じする能力は選択されていません").color(NamedTextColor.WHITE), List.of(
                    Component.text("[クリック] 全員同じにする能力を選択").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY)
            ));
            addButton(SINGLE_ABILITY_BUTTON_SLOT, itemStack, event -> {
                if (!(event.getWhoClicked() instanceof Player clicker)) return;
                AbilityTypeSelectorWithDisabledGUI abilityTypeGUI = new AbilityTypeSelectorWithDisabledGUI(
                        getGameSetting(),
                        select -> {
                            if (select.isDisabled()) {
                                clicker.playSound(clicker, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                            } else {
                                getGameSetting().setSingleAbility(select.abilityType());
                                updateSingleAbilityTypeButton();
                                open(clicker);
                            }
                        }
                );
                abilityTypeGUI.open(clicker);
            });
        } else {
            itemStack = singleAbility.getDisplayItem();
            List<Component> lore = new ArrayList<>(List.of(
                    Component.text("§7[左クリック] 全員同じ能力をリセット").decoration(TextDecoration.ITALIC, false),
                    Component.text("§7[右クリック] 全員同じにする能力を選択").decoration(TextDecoration.ITALIC, false)
            ));
            List<Component> abilityLore = singleAbility.getDisplayItem().lore();
            if (abilityLore != null) {
                lore.addFirst(Component.empty());
                lore.addFirst(abilityLore.getFirst());
            }
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.lore(lore);
                meta.displayName(Component.text("全員").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false).append(singleAbility.getName()));
                itemStack.setItemMeta(meta);
            }

            addButton(SINGLE_ABILITY_BUTTON_SLOT, itemStack, event -> {
                if (!(event.getWhoClicked() instanceof Player clicker)) return;
                if (event.isLeftClick()) {
                    getGameSetting().resetSingleAbility();
                    updateSingleAbilityTypeButton();
                } else {
                    AbilityTypeSelectorWithDisabledGUI abilityTypeGUI = new AbilityTypeSelectorWithDisabledGUI(
                            game.getGameSetting(),
                            select -> {
                                if (select.isDisabled()) {
                                    clicker.playSound(clicker, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                                } else {
                                    getGameSetting().setSingleAbility(select.abilityType());
                                    updateSingleAbilityTypeButton();
                                    open(clicker);
                                }
                            }
                    );
                    abilityTypeGUI.open(clicker);
                }
            });
        }

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

    private GameSetting getGameSetting() {
        return game.getGameSetting();
    }
}
