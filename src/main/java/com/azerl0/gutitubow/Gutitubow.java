package com.azerl0.gutitubow;

import com.azerl0.gutitubow.command.*;
import com.azerl0.gutitubow.listener.*;
import com.azerl0.gutitubow.manager.*;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.azerl0.gutitubow.ability.*;
import com.azerl0.gutitubow.ability.abilities.*;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class Gutitubow extends JavaPlugin {
    private DebugManager debugManager;
    private AbilityManager abilityManager;
    private CarePackageManager carePackageManager;
    private GamePlayerManager gamePlayerManager;
    private MapManager mapManager;
    private GameManager gameManager;


    private GameManager.Game game;

    @Override
    public void onEnable() {

        // Managers
        this.debugManager = new DebugManager(this);
        this.abilityManager = new AbilityManager(this);
        this.carePackageManager = new CarePackageManager(this);
        this.gamePlayerManager = new GamePlayerManager(this);
        this.mapManager = new MapManager(this);
        this.gameManager = new GameManager(this, this.abilityManager, this.mapManager, this.debugManager, this.gamePlayerManager, this.carePackageManager);

        // MainGame
        this.game = gameManager.newGame();



        // Ability登録
        AbilityContext abilityContext = new AbilityContext(this, gamePlayerManager);
        AbilityRegistry.register(new RicochetAbility(abilityContext));
        AbilityRegistry.register(new BombAbility(abilityContext));
        AbilityRegistry.register(new ControlAbility(abilityContext));
        AbilityRegistry.register(new BarrierAbility(abilityContext));
        AbilityRegistry.register(new SnipeAbility(abilityContext));
        AbilityRegistry.register(new QuickShotAbility(abilityContext));
        AbilityRegistry.register(new DashAbility(abilityContext));

        // Listener登録
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ProjectileHitListener(this, abilityManager), this);
        pm.registerEvents(new BowListener(abilityManager), this);
        pm.registerEvents(new GamePlayerListener(gamePlayerManager, abilityManager), this);
        pm.registerEvents(new EntityListener(carePackageManager), this);
        pm.registerEvents(new ItemListener(), this);

        // Command登録
        getCommand("ability").setExecutor(new AbilityCommand(abilityManager));
        getCommand("carepackage").setExecutor(new CarePackageCommand(carePackageManager));
        getCommand("game").setExecutor(new GameCommand(game));
        getCommand("gameplayer").setExecutor(new GamePlayerCommand(gamePlayerManager));
        getCommand("gutitubowdebug").setExecutor(new DebugCommand(debugManager));

        getLogger().info("Loaded abilities: " + AbilityRegistry.getAllIds());


    }

    @Override
    public void onDisable() {
        // グローバル発光カラー用スコアボードの削除
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        for (NamedTextColor color : NamedTextColor.NAMES.values()) {

            Team team = board.getTeam("glow_" + color.toString().toLowerCase());
            if (team != null) {
                team.unregister();
            }
        }

        game.reset();
    }
}
