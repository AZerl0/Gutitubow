package com.azerl0.gutitubow;

import com.azerl0.gutitubow.ability.AbilityManager;
import com.azerl0.gutitubow.ability.zipline.ZiplineManager;
import com.azerl0.gutitubow.arena.ArenaManager;
import com.azerl0.gutitubow.debug.DebugManager;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import com.azerl0.gutitubow.fakeplayer.FakePlayerManager;
import com.azerl0.gutitubow.game.GameManager;
import com.azerl0.gutitubow.gamemap.GameMapManager;
import com.azerl0.gutitubow.profiler.ProfileManager;
import com.azerl0.gutitubow.profiler.SkinDataManager;
import com.azerl0.gutitubow.scoreboard.ScoreboardManager;
import com.azerl0.gutitubow.packet.EntityGlowManager;

public class GutitubowManagers {
    private final AbilityManager abilityManager;
    private final EntityGlowManager glowManager;
    private final ScoreboardManager scoreboardManager;
    private final FakePlayerManager fakePlayerManager;
    private final ZiplineManager ziplineManager;
    private final GutitubowEntityManager gutitubowEntityManager;
    private final ArenaManager arenaManager;
    private final SkinDataManager skinDataManager;
    private final ProfileManager profileManager;
    private final DebugManager debugManager;
    private final GameMapManager gameMapManager;
    private final GameManager gameManager;

    public GutitubowManagers(
            AbilityManager abilityManager,
            EntityGlowManager glowManager,
            ScoreboardManager scoreboardManager,
            FakePlayerManager fakePlayerManager,
            ZiplineManager ziplineManager,
            GutitubowEntityManager gutitubowEntityManager,
            ArenaManager arenaManager,
            SkinDataManager skinDataManager,
            ProfileManager profileManager,
            DebugManager debugManager,
            GameMapManager gameMapManager,
            GameManager gameManager
    ) {
        this.abilityManager = abilityManager;
        this.glowManager = glowManager;
        this.scoreboardManager = scoreboardManager;
        this.fakePlayerManager = fakePlayerManager;
        this.ziplineManager = ziplineManager;
        this.gutitubowEntityManager = gutitubowEntityManager;
        this.arenaManager = arenaManager;
        this.skinDataManager = skinDataManager;
        this.profileManager = profileManager;
        this.debugManager = debugManager;
        this.gameMapManager = gameMapManager;
        this.gameManager = gameManager;
    }

    public AbilityManager getAbilityManager() { return abilityManager; }
    public EntityGlowManager getGlowManager() { return glowManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public FakePlayerManager getFakePlayerManager() { return fakePlayerManager; }
    public ZiplineManager getZiplineManager() { return ziplineManager; }
    public GutitubowEntityManager getGutitubowEntityManager() { return gutitubowEntityManager; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public SkinDataManager getSkinDataManager() { return skinDataManager; }
    public ProfileManager getProfileManager() { return profileManager; }
    public DebugManager getDebugManager() { return debugManager; }
    public GameMapManager getGameMapManager() { return gameMapManager; }
    public GameManager gameManager() { return gameManager; }
}
