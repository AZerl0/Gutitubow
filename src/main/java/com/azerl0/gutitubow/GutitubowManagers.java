package com.azerl0.gutitubow;

import com.azerl0.gutitubow.ability.zipline.ZiplineManager;
import com.azerl0.gutitubow.fakeplayer.FakePlayerManager;
import com.azerl0.gutitubow.scoreboard.ScoreboardManager;
import com.azerl0.gutitubow.manager.AdvancementManager;
import com.azerl0.gutitubow.packet.EntityGlowManager;
import com.azerl0.gutitubow.player.PlayerSkinChanger;

public class GutitubowManagers {
    private final EntityGlowManager glowManager;
    private final ScoreboardManager scoreboardManager;
    private final PlayerSkinChanger playerSkinChanger;
    private final FakePlayerManager fakePlayerManager;
    private final ZiplineManager ziplineManager;
    private final AdvancementManager advancementManager;

    public GutitubowManagers(
            EntityGlowManager glowManager,
            ScoreboardManager scoreboardManager,
            PlayerSkinChanger playerSkinChanger,
            FakePlayerManager fakePlayerManager,
            ZiplineManager ziplineManager,
            AdvancementManager advancementManager
    ) {
        this.glowManager = glowManager;
        this.scoreboardManager = scoreboardManager;
        this.playerSkinChanger = playerSkinChanger;
        this.fakePlayerManager = fakePlayerManager;
        this.ziplineManager = ziplineManager;
        this.advancementManager = advancementManager;
    }

    public EntityGlowManager getGlowManager() { return glowManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public PlayerSkinChanger getPlayerSkinChanger() { return playerSkinChanger; }
    public FakePlayerManager getFakePlayerManager() { return fakePlayerManager; }
    public ZiplineManager getZiplineManager() { return ziplineManager; }
    public AdvancementManager getAdvancementManager() { return advancementManager; }
}
