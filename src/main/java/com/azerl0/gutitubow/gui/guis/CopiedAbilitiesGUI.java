package com.azerl0.gutitubow.gui.guis;

import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.ability.abilities.CopyAbility;
import com.azerl0.gutitubow.event.ability.copy.UseCopiedAbilityEvent;
import com.azerl0.gutitubow.gui.AbstractGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Iterator;

public class CopiedAbilitiesGUI extends AbstractGUI {
    private final CopyAbility copyAbility;

    public CopiedAbilitiesGUI(CopyAbility copyAbility) {
        super(27, Component.text("ストックした能力"));
        this.copyAbility = copyAbility;
    }

    @Override
    public void updateGUI() {
        clear();
        Iterator<AbilityType> iterator = copyAbility.getStockAbilities().iterator();
        while (iterator.hasNext() && hasEmptySlot()) {
            AbilityType abilityType = iterator.next();
            int slot = getEmptySlot();
            addButton(
                    slot,
                    abilityType.getDisplayItem(copyAbility.getPlayer()),
                    event -> {
                        if (!(event.getWhoClicked() instanceof Player clicker)) return;
                        UseCopiedAbilityEvent useEvent = new UseCopiedAbilityEvent(clicker, abilityType);
                        Bukkit.getPluginManager().callEvent(useEvent);
                        copyAbility.getStockAbilities().remove(slot);
                        copyAbility.setAbility(abilityType);
                        clicker.closeInventory();
                    }
            );
        }
    }

    @Override
    public void open(Player player) {
        updateGUI();
        super.open(player);
    }
}
