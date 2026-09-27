package com.azerl0.gutitubow.gui;

import com.azerl0.gutitubow.gui.guis.AbilitySetGUI;
import com.azerl0.gutitubow.gui.guis.GameGUI;

public class AllGUI {
    private final GameGUI gameGUI;
    private final AbilitySetGUI abilitySetGUI;

    public AllGUI(GameGUI gameGUI, AbilitySetGUI abilitySetGUI) {
        this.gameGUI = gameGUI;
        this.abilitySetGUI = abilitySetGUI;
    }

    public GameGUI gameGUI() { return gameGUI; }

    public AbilitySetGUI abilitySelectorGUI() { return abilitySetGUI; }
}
