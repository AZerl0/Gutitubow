package com.azerl0.gutitubow.ability;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.GutitubowManagers;
import com.azerl0.gutitubow.ability.zipline.ZiplineManager;
import com.azerl0.gutitubow.api.game.Game;
import com.azerl0.gutitubow.fakeplayer.FakePlayerManager;
import com.azerl0.gutitubow.scoreboard.ScoreboardManager;
import com.azerl0.gutitubow.packet.EntityGlowManager;
import org.jetbrains.annotations.NotNull;

public class AbilityContext {
    private final Gutitubow gutitubow;

    public AbilityContext(
            @NotNull Gutitubow gutitubow
    ) {
        this.gutitubow = gutitubow;
    }

    public Gutitubow gutitubow() { return gutitubow; }
    public Game game() { return gutitubow.getGame(); }
    public GutitubowManagers getGutitubowManagers() { return gutitubow.getGutitubowManagers(); }
    public AbilityManager getAbilityManager() { return getGutitubowManagers().getAbilityManager(); }
    public EntityGlowManager glowManager() { return getGutitubowManagers().getGlowManager(); }
    public FakePlayerManager fakePlayerManager() { return getGutitubowManagers().getFakePlayerManager(); }
    public ScoreboardManager scoreboardManager() { return getGutitubowManagers().getScoreboardManager(); }
    public ZiplineManager ziplineManager() { return getGutitubowManagers().getZiplineManager(); }
}
