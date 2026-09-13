package com.azerl0.gutitubow.ability;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.GutitubowManagers;
import com.azerl0.gutitubow.ability.zipline.ZiplineManager;
import com.azerl0.gutitubow.fakeplayer.FakePlayerManager;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.scoreboard.ScoreboardManager;
import com.azerl0.gutitubow.packet.EntityGlowManager;
import com.azerl0.gutitubow.player.PlayerSkinChanger;

public class AbilityContext {
    private final Gutitubow gutitubow;
    private final Game game;
    private final GutitubowManagers gutitubowManagers;
    private final EntityGlowManager glowManager;
    private final PlayerSkinChanger playerSkinChanger;
    private final FakePlayerManager fakePlayerManager;
    private final ScoreboardManager scoreboardManager;
    private final ZiplineManager ziplineManager;

    public AbilityContext(
            Gutitubow gutitubow,
            Game game,
            GutitubowManagers gutitubowManagers
    ) {
        this.gutitubow = gutitubow;
        this.game = game;
        this.gutitubowManagers = gutitubowManagers;
        this.glowManager = gutitubowManagers.getGlowManager();
        this.playerSkinChanger = gutitubowManagers.getPlayerSkinChanger();
        this.fakePlayerManager = gutitubowManagers.getFakePlayerManager();
        this.scoreboardManager = gutitubowManagers.getScoreboardManager();
        this.ziplineManager = gutitubowManagers.getZiplineManager();
    }

    public Gutitubow gutitubow() { return gutitubow; }
    public Game game() { return game; }
    public GutitubowManagers getGutitubowManagers() { return gutitubowManagers; }
    public EntityGlowManager glowManager() { return glowManager; }
    public PlayerSkinChanger skinChanger() { return playerSkinChanger; }
    public FakePlayerManager fakePlayerManager() { return fakePlayerManager; }
    public ScoreboardManager scoreboardManager() { return scoreboardManager; }
    public ZiplineManager ziplineManager() { return ziplineManager; }
}
