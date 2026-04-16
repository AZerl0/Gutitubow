package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.gui.guis.AbilityDisableGUI;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class GameSetting {
    private GutitubowGameMode gameMode = GutitubowGameMode.SOLO;
    private double defaultHealth = 6;
    private Set<AbilityType> disabledAbilities = new HashSet<>();
    private final AbilityDisableGUI abilityDisableGUI;

    public GameSetting() {
        this.abilityDisableGUI = new AbilityDisableGUI(this);
    }

    public @NotNull GutitubowGameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(@NotNull GutitubowGameMode gameMode) {
        if (this.gameMode == gameMode) return;
        this.gameMode = gameMode;
    }

    public double getDefaultHealth() {
        return defaultHealth;
    }

    public void setDefaultHealth(double health) {
        this.defaultHealth = health;
    }

    public @NotNull Set<AbilityType> getDisabledAbilities() {
        return disabledAbilities;
    }

    public void setDisabledAbilities(@NotNull Set<AbilityType> disabledAbilities) {
        this.disabledAbilities = disabledAbilities;
    }

    public void setDisabledAbility(@NotNull AbilityType abilityType, boolean disable) {
        if (disable) {
            disabledAbilities.add(abilityType);
        } else disabledAbilities.remove(abilityType);
    }

    public boolean isDisabledAbility(@NotNull AbilityType abilityType) {
        return disabledAbilities.contains(abilityType);
    }

    public @NotNull AbilityDisableGUI getAbilityDisableGUI() {
        return abilityDisableGUI;
    }

}
