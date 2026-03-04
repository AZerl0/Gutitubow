package com.azerl0.gutitubow;

import com.azerl0.gutitubow.command.*;
import com.azerl0.gutitubow.listener.*;
import com.azerl0.gutitubow.manager.*;
import com.azerl0.gutitubow.util.EntityUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.azerl0.gutitubow.ability.*;
import com.azerl0.gutitubow.ability.abilities.*;

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
        this.gamePlayerManager = new GamePlayerManager(this, this.debugManager);
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
        pm.registerEvents(new GameListener(), this);

        // Command登録
        getCommand("ability").setExecutor(new AbilityCommand(abilityManager));
        getCommand("carepackage").setExecutor(new CarePackageCommand(carePackageManager));
        getCommand("game").setExecutor(new GameCommand(game));
        getCommand("gameplayer").setExecutor(new GamePlayerCommand(gamePlayerManager));
        getCommand("gutitubowdebug").setExecutor(new DebugCommand(debugManager));

        getLogger().info("Loaded abilities: " + AbilityRegistry.getAllIds());

        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRules.NATURAL_HEALTH_REGENERATION, false);
            world.setGameRule(GameRules.LOCATOR_BAR, false);
            world.setGameRule(GameRules.ADVANCE_TIME, false);
            world.setGameRule(GameRules.ADVANCE_WEATHER, false);
            world.setGameRule(GameRules.SPREAD_VINES, false);
            world.setGameRule(GameRules.MOB_GRIEFING, false);
            world.setGameRule(GameRules.SPAWN_MOBS, false);

            for (Arrow arrow : world.getEntitiesByClass(Arrow.class)) {
                arrow.remove();
            }
        }



    }

    @Override
    public void onDisable() {
        EntityUtil.removeAllGlowTeam();

        game.reset();
    }
}
