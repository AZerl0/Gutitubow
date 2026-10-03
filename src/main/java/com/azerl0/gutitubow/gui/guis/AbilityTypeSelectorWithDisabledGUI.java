package com.azerl0.gutitubow.gui.guis;

import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.game.GameSetting;
import com.azerl0.gutitubow.gui.AbstractAbilitiesGUI;
import com.azerl0.gutitubow.gui.guis.record.AbilityTypeSelectorWithDisabledRecord;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

public class AbilityTypeSelectorWithDisabledGUI extends AbstractAbilitiesGUI {
    private final GameSetting gameSetting;
    private final Consumer<AbilityTypeSelectorWithDisabledRecord> onSelect;

    public AbilityTypeSelectorWithDisabledGUI(GameSetting gameSetting, Consumer<AbilityTypeSelectorWithDisabledRecord> onSelect) {
        super(Component.text("能力選択"));
        this.gameSetting = gameSetting;
        this.onSelect = onSelect;
    }

    @Override
    public void addAbilityButton(AbilityType abilityType) {
        ItemStack itemStack = abilityType.getDisplayItem();
        boolean isDisabled = gameSetting.isDisabledAbility(abilityType);
        if (isDisabled) {
            itemStack = new ItemStack(Material.BARRIER);
            ItemMeta meta = itemStack.getItemMeta();
            ItemMeta originMeta = abilityType.getDisplayItem().getItemMeta();
            meta.displayName(originMeta.displayName());
            meta.lore(originMeta.lore());
            itemStack.setItemMeta(meta);
        }
        addAbilityButtonItem(itemStack, action -> onSelect.accept(new AbilityTypeSelectorWithDisabledRecord(
                abilityType,
                isDisabled
        )));
    }

    @Override
    public void open(Player player) {
        updateGUI();
        super.open(player);
    }
}
