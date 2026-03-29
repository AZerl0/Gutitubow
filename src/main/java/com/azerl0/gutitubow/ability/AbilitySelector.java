package com.azerl0.gutitubow.ability;

import com.azerl0.gutitubow.gui.guis.AbilityDisableGUI;

import java.util.*;

public class AbilitySelector {
    Set<AbilityType> disabledAbilities = new HashSet<>();
    List<AbilityType> currentAbilities = new ArrayList<>();
    AbilityDisableGUI abilityDisableGUI = new AbilityDisableGUI(this);

    public void resetCurrentAbilities() {
        currentAbilities.clear();
    }

    public AbilityType nextAbility() {
        if (currentAbilities.isEmpty()) {
            currentAbilities.addAll(AbilityRegistry.getTypes());
            currentAbilities.removeAll(disabledAbilities);
            Collections.shuffle(currentAbilities);
        }
        AbilityType type = currentAbilities.getFirst();
        currentAbilities.removeFirst();
        return type;

    }

    public void setDisabledAbility(AbilityType abilityType, boolean disable) {
        if (disable) {
            disabledAbilities.add(abilityType);
        } else disabledAbilities.remove(abilityType);
    }

    public boolean isDisabled(AbilityType abilityType) {
        return disabledAbilities.contains(abilityType);
    }

    public AbilityDisableGUI getAbilityDisableGUI() {
        return abilityDisableGUI;
    }
}
