package com.azerl0.gutitubow.ability;

import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.packet.EntityGlowManager;
import com.azerl0.gutitubow.player.PlayerSkinChanger;
import org.bukkit.plugin.java.JavaPlugin;

public class AbilityContext {
    private final JavaPlugin plugin;
    private final Game game;
    private final EntityGlowManager glowManager;
    private final PlayerSkinChanger playerSkinChanger;

    public AbilityContext(JavaPlugin plugin, Game game, EntityGlowManager glowManager, PlayerSkinChanger playerSkinChanger) {
        this.plugin = plugin;
        this.game = game;
        this.glowManager = glowManager;
        this.playerSkinChanger = playerSkinChanger;
    }

    public JavaPlugin plugin() { return plugin; }
    public Game game() { return game; }
    public EntityGlowManager glowManager() { return glowManager; }
    public PlayerSkinChanger skinChanger() { return playerSkinChanger; }
}
