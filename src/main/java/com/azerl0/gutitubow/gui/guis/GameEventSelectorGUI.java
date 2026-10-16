package com.azerl0.gutitubow.gui.guis;

import com.azerl0.gutitubow.gameevent.GameEventType;
import com.azerl0.gutitubow.gui.AbstractGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class GameEventSelectorGUI extends AbstractGUI {
    private final Consumer<GameEventType> onSelect;

    public GameEventSelectorGUI(Consumer<GameEventType> onSelect) {
        super(18, Component.text("イベント選択"));
        this.onSelect = onSelect;
    }

    @Override
    public void updateGUI() {
        clear();
        addEventButton(GameEventType.RICOCHET);
        addEventButton(GameEventType.BOMB);
        addEventButton(GameEventType.PISTOL);
        addEventButton(GameEventType.WANTED);
        addEventButton(GameEventType.SKIN);
        addEventButton(GameEventType.CAREPACKAGE);
        addEventButton(GameEventType.DOPPELGANGER);
        addEventButton(GameEventType.CONTROL);
        addEventButton(GameEventType.SHOT_BOW);
    }

    @Override
    public void open(Player player) {
        updateGUI();
        super.open(player);
    }

    private void addEventButton(GameEventType eventType) {
        if (hasEmptySlot()) {
            ItemStack itemStack = eventType.getDisplayItem();
            addButton(
                    getEmptySlot(),
                    itemStack,
                    _ -> onSelect.accept(eventType)
            );
        }
    }
}
