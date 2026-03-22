package com.azerl0.gutitubow.gui;

import com.azerl0.gutitubow.gui.guis.AbilitySelectorGUI;
import com.azerl0.gutitubow.gui.guis.GameGUI;

public class AllGUI {
    private final GameGUI gameGUI;
    private final AbilitySelectorGUI abilitySelectorGUI;

    public AllGUI(GameGUI gameGUI, AbilitySelectorGUI abilitySelectorGUI) {
        this.gameGUI = gameGUI;
        this.abilitySelectorGUI = abilitySelectorGUI;
    }

    public GameGUI gameGUI() { return gameGUI; }

    public AbilitySelectorGUI abilitySelectorGUI() { return abilitySelectorGUI; }
}
