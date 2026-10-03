package com.azerl0.gutitubow.gui.guis.record;

import com.azerl0.gutitubow.ability.AbilityType;

public record AbilityTypeSelectorWithDisabledRecord(
        AbilityType abilityType,
        boolean isDisabled
) {
}
