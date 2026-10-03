package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.gameevent.GameEventController;
import com.azerl0.gutitubow.gui.guis.AbilityDisableGUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class GameSetting {
    private GutitubowGameMode gameMode = GutitubowGameMode.SOLO;
    private double defaultHealth = 6;
    private int phase0seconds = 15;
    private int carePackageAmount = 3;
    private Set<AbilityType> disabledAbilities = new HashSet<>();
    private final GameEventController gameEventController = new GameEventController();
    private @Nullable AbilityType singleAbility;

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

    public int getPhase0seconds() {
        return phase0seconds;
    }

    public void setPhase0seconds(int seconds) {
        this.phase0seconds = seconds;
    }

    public int getCarePackageAmount() {
        return carePackageAmount;
    }

    public void setCarePackageAmount(int carePackageAmount) {
        this.carePackageAmount = carePackageAmount;
    }

    public @NotNull Set<AbilityType> getDisabledAbilities() {
        return new HashSet<>(disabledAbilities);
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

    public @NotNull GameEventController getGameEventController() {
        return gameEventController;
    }

    public @Nullable AbilityType getSingleAbility() {
        return singleAbility;
    }

    public void setSingleAbility(@Nullable AbilityType singleAbility) {
        this.singleAbility = singleAbility;
    }

    public void resetSingleAbility() {
        singleAbility = null;
    }
}
