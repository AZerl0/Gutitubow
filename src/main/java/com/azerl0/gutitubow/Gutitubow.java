package com.azerl0.gutitubow;

import com.azerl0.gutitubow.carepackage.CarePackageListener;
import com.azerl0.gutitubow.command.*;
import com.azerl0.gutitubow.debug.DebugManager;
import com.azerl0.gutitubow.game.*;
import com.azerl0.gutitubow.game.scoreboard.ScoreboardManager;
import com.azerl0.gutitubow.gui.AllGUI;
import com.azerl0.gutitubow.gui.GUIListener;
import com.azerl0.gutitubow.gui.guis.AbilitySelectorGUI;
import com.azerl0.gutitubow.gui.guis.GameGUI;
import com.azerl0.gutitubow.map.MapManager;
import com.azerl0.gutitubow.item.ItemListener;
import com.azerl0.gutitubow.packet.*;
import com.azerl0.gutitubow.player.FixLeftClickListener;
import com.azerl0.gutitubow.player.FixLeftClickManager;
import com.azerl0.gutitubow.player.PlayerSkinChanger;
import com.azerl0.gutitubow.util.EntityUtil;
import com.azerl0.gutitubow.util.ItemUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.EventManager;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.azerl0.gutitubow.ability.*;
import com.azerl0.gutitubow.ability.abilities.*;

public final class Gutitubow extends JavaPlugin {
    private ScoreboardManager scoreboardManager;
    private final EntityFlagManager flagManager = new EntityFlagManager();
    private EntityGlowManager glowManager;
    private FixLeftClickManager fixLeftClickManager;
    private DebugManager debugManager;
    private MapManager mapManager;
    private GameManager gameManager;
    private final PlayerSkinChanger playerSkinChanger = new PlayerSkinChanger(this);

    private PacketListenerCommon packetListener;


    private Game game;

    @Override
    public void onEnable() {

        ItemUtil.init(this);

        // Ability登録
        AbilityRegistry.register(AbilityType.RICOCHET, RicochetAbility::new);
        AbilityRegistry.register(AbilityType.BOMB, BombAbility::new);
        AbilityRegistry.register(AbilityType.CONTROL, ControlAbility::new);
        AbilityRegistry.register(AbilityType.BARRIER, BarrierAbility::new);
        AbilityRegistry.register(AbilityType.SNIPE, SnipeAbility::new);
        AbilityRegistry.register(AbilityType.QUICKFIRE, QuickFireAbility::new);
        AbilityRegistry.register(AbilityType.DASH, DashAbility::new);
        AbilityRegistry.register(AbilityType.JUMP, JumpAbility::new);
        AbilityRegistry.register(AbilityType.TELEPORT, TeleportAbility::new);
        AbilityRegistry.register(AbilityType.ESCAPE, EscapeAbility::new);
        AbilityRegistry.register(AbilityType.FLOAT, FloatAbility::new);
        AbilityRegistry.register(AbilityType.SHRINK, ShrinkAbility::new);
        AbilityRegistry.register(AbilityType.TOUGH, ToughAbility::new);
        AbilityRegistry.register(AbilityType.INVISIBLE, InvisibleAbility::new);
        AbilityRegistry.register(AbilityType.SHIELD, ShieldAbility::new);
        AbilityRegistry.register(AbilityType.FLASH, FlashAbility::new);
        AbilityRegistry.register(AbilityType.GIANT, GiantAbility::new);
        AbilityRegistry.register(AbilityType.PREDATION, PredationAbility::new);
        AbilityRegistry.register(AbilityType.BALLOON, BalloonAbility::new);
        AbilityRegistry.register(AbilityType.TRACKING, TrackingAbility::new);
        AbilityRegistry.register(AbilityType.LASTSTAND, LastStandAbility::new);
        AbilityRegistry.register(AbilityType.DETECTION, DetectionAbility::new);
        AbilityRegistry.register(AbilityType.INVINCIBLE, InvincibleAbility::new);
        AbilityRegistry.register(AbilityType.MEDUSA, MedusaAbility::new);
        AbilityRegistry.register(AbilityType.RAPIDFIRE, RapidFireAbility::new);

        // Managers
        this.scoreboardManager = new ScoreboardManager();
        this.glowManager = new EntityGlowManager(flagManager, scoreboardManager);
        this.fixLeftClickManager = new FixLeftClickManager(this);
        this.debugManager = new DebugManager(this);
        this.mapManager = new MapManager(this);
        this.gameManager = new GameManager(this, this.mapManager, this.debugManager, this.glowManager, this.scoreboardManager, playerSkinChanger);
        // MainGame
        this.game = gameManager.newGame();

        //GUI
        GameGUI gameGUI = new GameGUI(gameManager, game);
        AbilitySelectorGUI abilitySelectorGUI = new AbilitySelectorGUI(game.getAbilityManager());

        AllGUI allGUI = new AllGUI(gameGUI, abilitySelectorGUI);


        // Listener登録
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new AbilityListener(game.getAbilityManager()), this);
        pluginManager.registerEvents(new GamePlayerListener(game), this);
        pluginManager.registerEvents(new CarePackageListener(game.getCarePackageManager()), this);
        pluginManager.registerEvents(new ItemListener(), this);
        pluginManager.registerEvents(new GameListener(this, game), this);
        pluginManager.registerEvents(new GUIListener(allGUI), this);
        pluginManager.registerEvents(new PacketListener(flagManager), this);
        pluginManager.registerEvents(new FixLeftClickListener(fixLeftClickManager), this);

        // packetListener登録
        EventManager eventManager = PacketEvents.getAPI().getEventManager();
        packetListener = eventManager.registerListener(new PacketEventListener(this, flagManager), PacketListenerPriority.NORMAL);

        // Command登録
        getCommand("ability").setExecutor(new AbilityCommand(game.getAbilityManager(), abilitySelectorGUI));
        getCommand("carepackage").setExecutor(new CarePackageCommand(game.getCarePackageManager()));
        getCommand("game").setExecutor(new GameCommand(gameManager, game));
        getCommand("gameplayer").setExecutor(new GamePlayerCommand(this, game));
        getCommand("gutitubowdebug").setExecutor(new DebugCommand(debugManager));
        getCommand("gutitubowitem").setExecutor(new ItemCommand());
        getCommand("gameteam").setExecutor(new GameTeamCommand(game.getTeamManager(), game.getSpectatorManager()));
        getCommand("setskin").setExecutor(new SetSkinCommand(playerSkinChanger));
        getCommand("setname").setExecutor(new SetNameCommand());

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
        PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
        EntityUtil.removeAllGlowTeam();

        game.onDisablePlugin();
    }
}
