package com.azerl0.gutitubow.ability;

import java.util.*;

public class AbilitySelector {
    Set<AbilityType> disabledAbilities = new HashSet<>();
    List<AbilityType> currentAbilities = new ArrayList<>();

    public AbilitySelector() {}

    public AbilitySelector(Set<AbilityType> disabledAbilities) {
        this.disabledAbilities = disabledAbilities;
    }

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
}
