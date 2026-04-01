package com.azerl0.gutitubow.gui;

import com.azerl0.gutitubow.ability.AbilityType;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class AbstractAbilitiesGUI extends AbstractGUI {
    public AbstractAbilitiesGUI(Component title) {
        super(27, title);
    }

    @Override
    public void updateGUI() {
        clear();
        addAbilityButton(AbilityType.DASH);
        addAbilityButton(AbilityType.JUMP);
        addAbilityButton(AbilityType.TRACKING);
        addAbilityButton(AbilityType.ESCAPE);
        addAbilityButton(AbilityType.SNIPE);
        addAbilityButton(AbilityType.TOUGH);
        addAbilityButton(AbilityType.QUICKFIRE);
        addAbilityButton(AbilityType.DETECTION);
        addAbilityButton(AbilityType.PREDATION);
        addAbilityButton(AbilityType.INVISIBLE);
        addAbilityButton(AbilityType.LASTSTAND);
        addAbilityButton(AbilityType.TELEPORT);
        addAbilityButton(AbilityType.CONTROL);
        addAbilityButton(AbilityType.RICOCHET);
        addAbilityButton(AbilityType.BARRIER);
        addAbilityButton(AbilityType.INVINCIBLE);
        addAbilityButton(AbilityType.FLOAT);
        addAbilityButton(AbilityType.MEDUSA);
        addAbilityButton(AbilityType.BOMB);
        addAbilityButton(AbilityType.SHIELD);
        addAbilityButton(AbilityType.FLASH);
        addAbilityButton(AbilityType.RAPIDFIRE);
        addAbilityButton(AbilityType.SHRINK);
        addAbilityButton(AbilityType.GIANT);
        addAbilityButton(AbilityType.BALLOON);
    }

    public void addAbilityButton(AbilityType type) {
        addAbilityButtonItem(type.getDisplayItem(), action -> {});
    }

    public void addAbilityButtonItem(ItemStack item, Consumer<InventoryClickEvent> action) {
        if (!hasEmptySlot()) return;
        addButton(getEmptySlot(), item, action);
    }
}
