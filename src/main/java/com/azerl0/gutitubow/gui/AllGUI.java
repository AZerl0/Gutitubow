package com.azerl0.gutitubow.gui;

import com.azerl0.gutitubow.gui.guis.GameGUI;

public class AllGUI {
    private final GameGUI gameGUI;

    public AllGUI(GameGUI gameGUI) {
        this.gameGUI = gameGUI;
    }

    public GameGUI gameGUI() { return gameGUI; }
}
