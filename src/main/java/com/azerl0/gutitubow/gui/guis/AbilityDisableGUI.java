package com.azerl0.gutitubow.gui.guis;

import com.azerl0.gutitubow.ability.AbilitySelector;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.game.GameSetting;
import com.azerl0.gutitubow.gui.AbstractAbilitiesGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AbilityDisableGUI extends AbstractAbilitiesGUI {
    private final GameSetting gameSetting;

    public AbilityDisableGUI(GameSetting gameSetting) {
        super(Component.text("能力スイッチ"));
        this.gameSetting = gameSetting;
    }

    @Override
    public void addAbilityButton(AbilityType abilityType) {
        ItemStack itemStack = abilityType.getDisplayItem();
        if (gameSetting.isDisabledAbility(abilityType)) {
            itemStack = new ItemStack(Material.BARRIER);
            ItemMeta meta = itemStack.getItemMeta();
            ItemMeta originMeta = abilityType.getDisplayItem().getItemMeta();
            meta.displayName(originMeta.displayName());
            meta.lore(originMeta.lore());
            itemStack.setItemMeta(meta);
        }
        addAbilityButtonItem(itemStack, action -> {
            gameSetting.setDisabledAbility(abilityType, !gameSetting.isDisabledAbility(abilityType));
            updateGUI();
        });
    }

    @Override
    public void open(Player player) {
        updateGUI();
        super.open(player);
    }

}
