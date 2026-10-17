package com.azerl0.gutitubow;

import com.azerl0.gutitubow.api.game.Game;
import com.azerl0.gutitubow.arena.ArenaManager;
import com.azerl0.gutitubow.config.ConfigManager;
import com.azerl0.gutitubow.entity.GutitubowEntityListener;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import com.azerl0.gutitubow.entity.carepackage.CarePackageListener;
import com.azerl0.gutitubow.debug.DebugManager;
import com.azerl0.gutitubow.firstjoin.FirstJoin;
import com.azerl0.gutitubow.game.*;
import com.azerl0.gutitubow.profiler.ProfileListener;
import com.azerl0.gutitubow.profiler.ProfileManager;
import com.azerl0.gutitubow.profiler.SkinDataListener;
import com.azerl0.gutitubow.profiler.SkinDataManager;
import com.azerl0.gutitubow.scoreboard.ScoreboardManager;
import com.azerl0.gutitubow.gameevent.GameEventListener;
import com.azerl0.gutitubow.gameplayer.GamePlayerListener;
import com.azerl0.gutitubow.gui.AllGUI;
import com.azerl0.gutitubow.gui.GUIListener;
import com.azerl0.gutitubow.gui.guis.AbilitySetGUI;
import com.azerl0.gutitubow.gui.guis.GameGUI;
import com.azerl0.gutitubow.gamemap.GameMapManager;
import com.azerl0.gutitubow.item.ItemListener;
import com.azerl0.gutitubow.packet.*;
import com.azerl0.gutitubow.pin.PinListener;
import com.azerl0.gutitubow.pin.PinManager;
import com.azerl0.gutitubow.player.FixLeftClickListener;
import com.azerl0.gutitubow.player.FixLeftClickManager;
import com.azerl0.gutitubow.util.EntityUtil;
import com.azerl0.gutitubow.util.ItemUtil;
import com.azerl0.gutitubow.util.PostEffectUtil;
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
    private static Gutitubow instance;
    private ConfigManager configManager;
    private FirstJoin firstJoin;
    private EntityFlagManager flagManager;
    private PinManager pinManager;
    private FixLeftClickManager fixLeftClickManager;
    private AllGUI allGUI;
    GutitubowManagers gutitubowManagers;

    private PacketListenerCommon packetListener;


    private Game game;
    private AbilitySetGUI abilitySetGUI;

    @Override
    public void onEnable() {
        instance = this;
        ItemUtil.init(this);
        PostEffectUtil.init();

        // Ability登録
        registerAbilities();

        // Managers
        configManager = new ConfigManager(this);
        firstJoin = new FirstJoin();
        this.fixLeftClickManager = new FixLeftClickManager(this);
        gutitubowManagers = createGutitubowManagers();
        flagManager = new EntityFlagManager(this);
        pinManager = new PinManager(this);

        configManager.processConfigs();

        // MainGame
        this.game = gutitubowManagers.gameManager().newGame();

        //GUI
        GameGUI gameGUI = new GameGUI(gutitubowManagers.gameManager(), game);
        this.abilitySetGUI = new AbilitySetGUI(gutitubowManagers.getAbilityManager());

        allGUI = new AllGUI(gameGUI, abilitySetGUI);


        // Listener登録
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new AbilityListener(game.getGutitubowManagers().getAbilityManager()), this);
        pluginManager.registerEvents(new GamePlayerListener(game), this);
        pluginManager.registerEvents(new GutitubowEntityListener(getGutitubowEntityManager()), this);
        pluginManager.registerEvents(new CarePackageListener(getGutitubowEntityManager()), this);
        pluginManager.registerEvents(new ItemListener(), this);
        pluginManager.registerEvents(new GameListener(this, game), this);
        pluginManager.registerEvents(new GameEventListener(), this);
        pluginManager.registerEvents(new GUIListener(allGUI), this);
        pluginManager.registerEvents(new PacketListener(flagManager), this);
        pluginManager.registerEvents(new FixLeftClickListener(fixLeftClickManager), this);
        pluginManager.registerEvents(new PinListener(pinManager), this);
        pluginManager.registerEvents(new FirstJoin(), this);
        pluginManager.registerEvents(new SkinDataListener(gutitubowManagers.getSkinDataManager()), this);
        pluginManager.registerEvents(new ProfileListener(gutitubowManagers.getProfileManager()), this);

        // packetListener登録
        EventManager eventManager = PacketEvents.getAPI().getEventManager();
        packetListener = eventManager.registerListener(new PacketEventListener(this, flagManager), PacketListenerPriority.NORMAL);


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

    public static Gutitubow get() {
        return instance;
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
        EntityUtil.removeAllGlowTeam();

        game.destroy();
        gutitubowManagers.getAbilityManager().allResetAbilities();
        gutitubowManagers.getGutitubowEntityManager().removeAllGutitubowEntities();
    }

    private GutitubowManagers createGutitubowManagers() {
        AbilityContext abilityContext = new AbilityContext(this);
        EntityFlagManager flagManager = new EntityFlagManager(this);
        ScoreboardManager scoreboardManager = new ScoreboardManager();
        SkinDataManager skinDataManager = new SkinDataManager(this);
        ProfileManager profileManager = new ProfileManager(skinDataManager);
        DebugManager debugManager = new DebugManager(this);
        GameMapManager gameMapManager = new GameMapManager();
        GameManager gameManager = new GameManager(this);

        return new GutitubowManagers(
                new AbilityManager(abilityContext),
                new EntityGlowManager(flagManager, scoreboardManager),
                scoreboardManager,
                new GutitubowEntityManager(),
                new ArenaManager(this),
                skinDataManager,
                profileManager,
                debugManager,
                gameMapManager,
                gameManager
        );
    }

    private void registerAbilities() {
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
        AbilityRegistry.register(AbilityType.ECHO, EchoAbility::new);
        AbilityRegistry.register(AbilityType.CLONE, CloneAbility::new);
        AbilityRegistry.register(AbilityType.CHANGE, ChangeAbility::new);
        AbilityRegistry.register(AbilityType.COPY, CopyAbility::new);
        AbilityRegistry.register(AbilityType.APPRAISAL, AppraisalAbility::new);
        AbilityRegistry.register(AbilityType.SMASH, SmashAbility::new);
        AbilityRegistry.register(AbilityType.ZIPLINE, ZiplineAbility::new);
        AbilityRegistry.register(AbilityType.GRAPPLINGHOOK, GrapplingHookAbility::new);
        AbilityRegistry.register(AbilityType.SWAP, SwapAbility::new);
        AbilityRegistry.register(AbilityType.GRAVITY, GravityAbility::new);
        AbilityRegistry.register(AbilityType.INVERSION, InversionAbility::new);
        AbilityRegistry.register(AbilityType.ERASURE, ErasureAbility::new);
    }

    public GutitubowManagers getGutitubowManagers() {
        return gutitubowManagers;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Game getGame() {
        return game;
    }


    public AbilitySetGUI getAbilitySelectorGUI() {
        return abilitySetGUI;
    }


    public GutitubowEntityManager getGutitubowEntityManager() {
        return gutitubowManagers.getGutitubowEntityManager();
    }

    public AllGUI getAllGUI() {
        return allGUI;
    }
}
