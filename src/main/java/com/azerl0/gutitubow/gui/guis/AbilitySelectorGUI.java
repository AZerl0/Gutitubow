package com.azerl0.gutitubow.gui.guis;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityManager;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.gui.AbstractAbilitiesGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

public class AbilitySelectorGUI extends AbstractAbilitiesGUI {
    private final AbilityManager abilityManager;

    public AbilitySelectorGUI(AbilityManager abilityManager) {
        super(Component.text("能力選択"));
        this.abilityManager = abilityManager;
    }

    @Override
    public void addAbilityButton(AbilityType abilityType) {
        super.addAbilityButtonItem(abilityType.getDisplayItem(), action ->{
            HumanEntity clicker = action.getWhoClicked();
            if (!(clicker instanceof Player player)) return;
            Ability ability = abilityManager.getAbility(player);

            if (ability != null && ability.getType() == abilityType) {
                player.sendMessage(Component.text("既にその能力を選んでいます").color(NamedTextColor.RED));
            } else {
                abilityManager.setAbility(player, abilityType);
                player.sendMessage(Component.text("能力を ").append(abilityType.getName()).append(Component.text(" に設定しました！")));
            }
            player.closeInventory();
        });
    }
}
