package com.azerl0.gutitubow.ability;

import com.azerl0.gutitubow.fakeplayer.FakePlayerManager;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.game.scoreboard.ScoreboardManager;
import com.azerl0.gutitubow.packet.EntityGlowManager;
import com.azerl0.gutitubow.player.PlayerSkinChanger;
import org.bukkit.plugin.java.JavaPlugin;

public class AbilityContext {
    private final JavaPlugin plugin;
    private final Game game;
    private final EntityGlowManager glowManager;
    private final PlayerSkinChanger playerSkinChanger;
    private final FakePlayerManager fakePlayerManager;
    private final ScoreboardManager scoreboardManager;

    public AbilityContext(JavaPlugin plugin, Game game, EntityGlowManager glowManager, PlayerSkinChanger playerSkinChanger, FakePlayerManager fakePlayerManager, ScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.game = game;
        this.glowManager = glowManager;
        this.playerSkinChanger = playerSkinChanger;
        this.fakePlayerManager = fakePlayerManager;
        this.scoreboardManager = scoreboardManager;
    }

    public JavaPlugin plugin() { return plugin; }
    public Game game() { return game; }
    public EntityGlowManager glowManager() { return glowManager; }
    public PlayerSkinChanger skinChanger() { return playerSkinChanger; }
    public FakePlayerManager fakePlayerManager() { return fakePlayerManager; }
    public ScoreboardManager scoreboardManager() { return scoreboardManager; }
}
