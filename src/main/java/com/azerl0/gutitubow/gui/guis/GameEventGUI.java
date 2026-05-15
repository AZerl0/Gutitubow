package com.azerl0.gutitubow.gui.guis;

import com.azerl0.gutitubow.gameevent.GameEvent;
import com.azerl0.gutitubow.gameevent.GameEventController;
import com.azerl0.gutitubow.gameevent.GameEventControllerGameEventType;
import com.azerl0.gutitubow.gameevent.GameEventType;
import com.azerl0.gutitubow.gui.AbstractGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GameEventGUI extends AbstractGUI {
    private final GameEventController gameEventController;

    public GameEventGUI(GameEventController gameEventController) {
        super(9, Component.text("ゲームイベント設定"));
        this.gameEventController = gameEventController;
    }

    @Override
    public void updateGUI() {
        clear();
        addGameEventButton(0, GameEventControllerGameEventType.PHASE2);
        addGameEventButton(1, GameEventControllerGameEventType.PHASE3);
        addGameEventButton(2, GameEventControllerGameEventType.PHASE4);
        addGameEventButton(3, GameEventControllerGameEventType.TEST);
    }

    @Override
    public void open(Player player) {
        updateGUI();
        super.open(player);
    }

    private interface PhaseEventGetter {
        GameEvent get();
    }

    private interface PhaseEventSetter {
        void set(GameEventType eventType);
    }

    private void addGameEventButton(
            int slot,
            GameEventControllerGameEventType controllerGameEventType
    ) {
        GameEvent event = gameEventController.getEvent(controllerGameEventType);
        if (event == null) {
            ItemStack itemStack = setPhaseEventButton(emptyPhaseEventItem(controllerGameEventType), true);
            itemStack.setAmount(switch (controllerGameEventType) {
                case PHASE2 -> 2;
                case PHASE3 -> 3;
                case PHASE4 -> 4;
                default -> 1;
            });
            addButton(
                    slot,
                    itemStack,
                    click -> {
                        if (!(click.getWhoClicked() instanceof Player clicker)) return;
                        GameEventSelectorGUI gameEventSelectorGUI = new GameEventSelectorGUI(select -> {
                            gameEventController.setEvent(controllerGameEventType, select);
                            this.open(clicker);
                        });
                        gameEventSelectorGUI.open(clicker);
                    }
            );
        } else {
            GameEventType eventType = event.getEventType();
            ItemStack itemStack = setPhaseEventButton(eventType.getDisplayItem(), false);
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(controllerGameEventType + "のイベント").decoration(TextDecoration.ITALIC, false)
                        .append(event.isActivated() ?
                                Component.text(" (起動中)").color(NamedTextColor.GREEN) :
                                Component.text(" (停止中)").color(NamedTextColor.RED)
                        ));
                List<Component> itemLore = new ArrayList<>(List.of(eventType.getName().color(NamedTextColor.AQUA)));
                itemLore.addAll(eventType.getLore());
                if (meta.lore() != null) itemLore.addAll(meta.lore());
                Component actionMessage = event.isActivated() ?
                        Component.text("終了").color(NamedTextColor.RED) : Component.text("起動").color(NamedTextColor.GREEN);
                itemLore.add(Component.text("[SHIFT + 左クリック] このイベントを").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE).append(actionMessage));
                meta.lore(itemLore);
            }
            itemStack.setItemMeta(meta);
            itemStack.setAmount(switch (controllerGameEventType) {
                case PHASE2 -> 2;
                case PHASE3 -> 3;
                case PHASE4 -> 4;
                default -> 1;
            });
            addButton(
                    slot,
                    itemStack,
                    click -> {
                        if (!(click.getWhoClicked() instanceof Player clicker)) return;
                        if (click.isRightClick()) {
                            GameEventSelectorGUI gameEventSelectorGUI = new GameEventSelectorGUI(select -> {
                                gameEventController.setEvent(controllerGameEventType, select);
                                this.open(clicker);
                            });
                            gameEventSelectorGUI.open(clicker);
                        } else {
                            if (click.isShiftClick()) {
                                if (event.isActivated()) {
                                    event.deactivate(true);
                                } else  {
                                    event.activate();
                                }
                            } else {
                                if (event.isActivated()) event.deactivate(false);
                                gameEventController.removeEvent(controllerGameEventType);
                            }
                            updateGUI();
                        }
                    }
            );
        }
    }

    private ItemStack emptyPhaseEventItem(GameEventControllerGameEventType controllerGameEventType) {
        ItemStack itemStack = ItemStack.of(Material.BARRIER);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(controllerGameEventType.toString() + "のイベント").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));
            meta.lore(List.of(
                    Component.text("イベント無し").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE)
            ));
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private ItemStack setPhaseEventButton(ItemStack buttonItem, boolean isEmpty) {
        ItemMeta meta = buttonItem.getItemMeta();
        if (meta != null) {
            List<Component> addLore = new ArrayList<>(List.of(
                    Component.empty()
            ));
            if (!isEmpty) {
                addLore.add(Component.text("[左クリック] このフェーズのイベントを削除").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));
            }
            addLore.add(Component.text("[" + (isEmpty ? "" : "右")  + "クリック] このフェーズのイベントを設定").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));
            List<Component> itemLore = meta.lore();
            if (itemLore == null) {
                meta.lore(addLore);
            } else {
                itemLore.addAll(addLore);
                meta.lore(itemLore);
            }
        }
        buttonItem.setItemMeta(meta);
        return buttonItem;
    }
}
