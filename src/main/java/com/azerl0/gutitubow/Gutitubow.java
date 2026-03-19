package com.azerl0.gutitubow;

import com.azerl0.gutitubow.carepackage.CarePackageManager;
import com.azerl0.gutitubow.carepackage.CarePackageListener;
import com.azerl0.gutitubow.command.*;
import com.azerl0.gutitubow.debug.DebugManager;
import com.azerl0.gutitubow.game.*;
import com.azerl0.gutitubow.gui.AllGUI;
import com.azerl0.gutitubow.gui.GUIListener;
import com.azerl0.gutitubow.gui.guis.GameGUI;
import com.azerl0.gutitubow.map.MapManager;
import com.azerl0.gutitubow.item.ItemListener;
import com.azerl0.gutitubow.packet.EntityFlagManager;
import com.azerl0.gutitubow.packet.EntityGlowManager;
import com.azerl0.gutitubow.packet.PacketListener;
import com.azerl0.gutitubow.util.EntityUtil;
import com.azerl0.gutitubow.util.ItemUtil;
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
    private CarePackageManager carePackageManager;
    private MapManager mapManager;
    private GameManager gameManager;
    private EntityFlagManager flagManager;
    private EntityGlowManager glowManager;


    private Game game;

    @Override
    public void onEnable() {

        ItemUtil.init(this);

        // Managers
        this.flagManager = new EntityFlagManager();
        this.glowManager = new EntityGlowManager(flagManager);
        this.debugManager = new DebugManager(this);
        this.carePackageManager = new CarePackageManager(this, glowManager);
        this.mapManager = new MapManager(this);
        this.gameManager = new GameManager(this, this.mapManager, this.debugManager, this.carePackageManager);
        // MainGame
        this.game = gameManager.newGame();


        // Ability登録
        AbilityRegistry.register(AbilityType.RICOCHET, RicochetAbility::new);
        AbilityRegistry.register(AbilityType.BOMB, BombAbility::new);
        AbilityRegistry.register(AbilityType.CONTROL, ControlAbility::new);
        AbilityRegistry.register(AbilityType.BARRIER, BarrierAbility::new);
        AbilityRegistry.register(AbilityType.SNIPE, SnipeAbility::new);
        AbilityRegistry.register(AbilityType.RAPIDFIRE, RapidFireAbility::new);
        AbilityRegistry.register(AbilityType.DASH, DashAbility::new);
        AbilityRegistry.register(AbilityType.JUMP, JumpAbility::new);
        AbilityRegistry.register(AbilityType.TELEPORT, TeleportAbility::new);
        AbilityRegistry.register(AbilityType.ESCAPE, EscapeAbility::new);
        AbilityRegistry.register(AbilityType.FLOAT, FloatAbility::new);
        AbilityRegistry.register(AbilityType.SHRINK, ShrinkAbility::new);
        AbilityRegistry.register(AbilityType.TOUGH, ToughAbility::new);

        //GUI
        GameGUI gameGUI = new GameGUI(gameManager, game);

        AllGUI allGUI = new AllGUI(gameGUI);


        // Listener登録
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new AbilityListener(game.getAbilityManager()), this);
        pm.registerEvents(new GamePlayerListener(game), this);
        pm.registerEvents(new CarePackageListener(carePackageManager), this);
        pm.registerEvents(new ItemListener(), this);
        pm.registerEvents(new GameListener(this, game), this);
        pm.registerEvents(new GUIListener(allGUI), this);
        pm.registerEvents(new PacketListener(flagManager), this);

        // Command登録
        getCommand("ability").setExecutor(new AbilityCommand(game.getAbilityManager()));
        getCommand("carepackage").setExecutor(new CarePackageCommand(carePackageManager));
        getCommand("game").setExecutor(new GameCommand(gameManager, game));
        getCommand("gameplayer").setExecutor(new GamePlayerCommand(this, game));
        getCommand("gutitubowdebug").setExecutor(new DebugCommand(debugManager));
        getCommand("gutitubowitem").setExecutor(new ItemCommand());

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
