package com.azerl0.gutitubow;

import com.azerl0.gutitubow.carepackage.CarePackageManager;
import com.azerl0.gutitubow.carepackage.CarePackageListener;
import com.azerl0.gutitubow.command.*;
import com.azerl0.gutitubow.debug.DebugManager;
import com.azerl0.gutitubow.game.*;
import com.azerl0.gutitubow.listener.*;
import com.azerl0.gutitubow.map.MapManager;
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


    private Game game;

    @Override
    public void onEnable() {

        // Managers
        this.debugManager = new DebugManager(this);
        this.abilityManager = new AbilityManager(this);
        this.carePackageManager = new CarePackageManager(this);
        this.mapManager = new MapManager(this);
        this.gameManager = new GameManager(this, this.abilityManager, this.mapManager, this.debugManager, this.carePackageManager);
        // MainGame
        this.game = gameManager.newGame();

        this.gamePlayerManager = new GamePlayerManager(this, game);




        // Ability登録
        AbilityContext abilityContext = new AbilityContext(this, game);
        AbilityRegistry.register(new RicochetAbility(abilityContext));
        AbilityRegistry.register(new BombAbility(abilityContext));
        AbilityRegistry.register(new ControlAbility(abilityContext));
        AbilityRegistry.register(new BarrierAbility(abilityContext));
        AbilityRegistry.register(new SnipeAbility(abilityContext));
        AbilityRegistry.register(new RapidFireAbility(abilityContext));
        AbilityRegistry.register(new DashAbility(abilityContext));
        AbilityRegistry.register(new JumpAbility(abilityContext));
        AbilityRegistry.register(new TeleportAbility(abilityContext));

        // Listener登録
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new AbilityListener(abilityManager), this);
        pm.registerEvents(new GamePlayerListener(game), this);
        pm.registerEvents(new CarePackageListener(carePackageManager), this);
        pm.registerEvents(new ItemListener(), this);
        pm.registerEvents(new GameListener(this, game), this);

        // Command登録
        getCommand("ability").setExecutor(new AbilityCommand(abilityManager));
        getCommand("carepackage").setExecutor(new CarePackageCommand(carePackageManager));
        getCommand("game").setExecutor(new GameCommand(gameManager, game));
        getCommand("gameplayer").setExecutor(new GamePlayerCommand(game));
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
            world.setGameRule(GameRules.MOB_DROPS, false);
            world.setGameRule(GameRules.PLAYER_MOVEMENT_CHECK, false);

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
