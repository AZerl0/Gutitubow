package com.azerl0.gutitubow.gui.guis;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityManager;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.gui.AbstractAbilitiesGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

public class AbilitySetGUI extends AbstractAbilitiesGUI {
    private final AbilityManager abilityManager;

    public AbilitySetGUI(AbilityManager abilityManager) {
        super(Component.text("能力選択"));
        this.abilityManager = abilityManager;
        updateGUI();
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
                Component abilityTypeMessage = abilityType.getName().color(NamedTextColor.GOLD).hoverEvent(abilityType.getDisplayItem().asHoverEvent());
                player.sendMessage(Component.text("能力を ").append(abilityTypeMessage).append(Component.text(" に設定しました！").color(NamedTextColor.WHITE)));
            }
            player.closeInventory();
        });
    }
}
