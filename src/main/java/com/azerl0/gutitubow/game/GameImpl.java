package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.GutitubowManagers;
import com.azerl0.gutitubow.ability.*;
import com.azerl0.gutitubow.api.game.Game;
import com.azerl0.gutitubow.arena.Arena;
import com.azerl0.gutitubow.damage.DamageSource;
import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.carepackage.CarePackage;
import com.azerl0.gutitubow.data.item.GutitubowItem;
import com.azerl0.gutitubow.debug.DebugManager;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerDeathEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerWinEvent;
import com.azerl0.gutitubow.gui.guis.GameGUI;
import com.azerl0.gutitubow.gui.guis.GameMapSelectorGUI;
import com.azerl0.gutitubow.manager.LogManager;
import com.azerl0.gutitubow.scoreboard.PlayerBoard;
import com.azerl0.gutitubow.scoreboard.PlayerBoardManager;
import com.azerl0.gutitubow.scoreboard.ScoreboardManager;
import com.azerl0.gutitubow.gameevent.GameEvent;
import com.azerl0.gutitubow.gameevent.GameEventControllerGameEventType;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.gameplayer.GamePlayerManager;
import com.azerl0.gutitubow.manager.GlassBreakManager;
import com.azerl0.gutitubow.gamemap.GameMap;
import com.azerl0.gutitubow.gamemap.MapRepairManager;
import com.azerl0.gutitubow.team.GameTeam;
import com.azerl0.gutitubow.team.TeamManager;
import com.azerl0.gutitubow.util.MathUtil;
import com.azerl0.gutitubow.util.PlayerUtil;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class GameImpl implements Game {
    public final int LAST_PHASE_CYCLE_TICK = 15 * 20;

    private final Gutitubow gutitubow;
    private final GameSetting gameSetting = new GameSetting();
    private boolean started;
    private int nextTick;
    private int currentTicks;
    private GamePhase gamePhase;
    private Set<GamePlayer> players;
    private final Set<GamePlayer> alivePlayers = new HashSet<>();
    private final List<GamePlayer> deathOrder = new ArrayList<>();
    private final BossBar bossBar = BossBar.bossBar(Component.text(""), 0.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
    private Arena arena;
    private GameMap gameMap;
    private Location safeArea;
    private Location oldSafeArea;
    private final GameGUI gameGUI;
    private final GameMapSelectorGUI gameMapSelectorGUI;
    private final GutitubowManagers gutitubowManagers;
    private final GamePlayerManager gamePlayerManager;
    private final DebugManager debugManager;
    private final MapRepairManager mapRepairManager = new MapRepairManager();
    private final GlassBreakManager glassBreakManager = new GlassBreakManager(mapRepairManager);
    private final ScoreboardManager scoreboardManager;
    private final PlayerBoardManager playerBoardManager;
    private final TeamManager teamManager;
    private final SpectatorManager spectatorManager = new SpectatorManager();

    public GameImpl(
            Gutitubow gutitubow
    ) {
        this.gutitubow = gutitubow;
        this.gutitubowManagers = gutitubow.getGutitubowManagers();
        this.debugManager = gutitubow.getGutitubowManagers().getDebugManager();
        this.gamePlayerManager = new GamePlayerManager(gutitubow, this);
        this.scoreboardManager = gutitubowManagers.getScoreboardManager();
        this.playerBoardManager = new PlayerBoardManager(gutitubow, scoreboardManager);
        this.teamManager = new TeamManager(this);
        gameMapSelectorGUI = new GameMapSelectorGUI(this);
        reset();
        gameGUI = new GameGUI(this);
    }

    @Override
    public @NotNull GameSetting getGameSetting() {
        return gameSetting;
    }

    @Override
    public @NotNull GamePlayer getGamePlayer(@NotNull Player player) {
        return gamePlayerManager.getGamePlayer(player);
    }

    @Override
    public @Nullable GamePlayer getGamePlayer(@NotNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;
        return getGamePlayer(player);
    }

    @Override
    public @Nullable GameTeam getGameTeam(@NotNull Player player) {
        return teamManager.getPlayerTeam(player);
    }

    @Override
    public @Nullable GameTeam getGameTeam(@NotNull UUID uuid) {
        return teamManager.getPlayerTeam(uuid);
    }

    @Override
    public boolean isThisGamePlaying(@NotNull Player player) {
        return getPlayers().contains(getGamePlayer(player));
    }

    @Override
    public boolean isTeammate(@NotNull Player player, @NotNull Player target) {
        if (player.getUniqueId() == target.getUniqueId()) return true;
        GameTeam gameTeam = getGameTeam(player);
        if (gameTeam == null) return false;
        return gameTeam.isTeammate(target);
    }

    @Override
    public boolean isTeammate(@NotNull UUID player, @NotNull UUID target) {
        if (player == target) return true;
        GameTeam gameTeam = getGameTeam(player);
        if (gameTeam == null) return false;
        return gameTeam.isTeammate(target);

    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public @NotNull GameStartResult start() {
        if (gameMap == null || arena == null) return GameStartResult.MAP_IS_NULL;
        Bukkit.getServer().broadcast(Component.text("start"));
        started = true;
        gamePhase = GamePhase.STARTING;
        players.clear();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (getSpectatorManager().isSpectator(player)) continue;
            players.add(getGamePlayer(player));
        }

        return GameStartResult.SUCCESS;
    }


    @Override
    public @NotNull GamePhase getPhase() {
        return gamePhase;
    }

    @Override
    public boolean isFinalPhase() {
        return getPhase() == GamePhase.FINAL_PHASE;
    }

    @Override
    public @NotNull Location getSafeArea() {
        return safeArea.clone();
    }

    @Override
    public boolean isShrinking() {
        return gamePhase.isShrinking();
    }

    @Override
    public float getPhaseProgress() {
        return (float) this.currentTicks / (float) this.nextTick;
    }

    @Override
    public @NotNull Set<GamePlayer> getPlayers() {
        return players;
    }

    @Override
    public @NotNull Set<GamePlayer> getAllGamePlayers() {
        return gamePlayerManager.getGamePlayers();
    }

    @Override
    public @NotNull Set<GamePlayer> getAlivePlayers() {
        return alivePlayers;
    }

    @Override
    public @NotNull List<GamePlayer> getDeadPlayers() {
        return new ArrayList<>(deathOrder);
    }

    @Override
    public void setGameMode(GutitubowGameMode gameMode) {
        if (getGameSetting().getGameMode() != gameMode) {
            if (gameMode == GutitubowGameMode.DUO || gameMode == GutitubowGameMode.TRIO) {
                teamManager.resetTeams();
            }
            getGameSetting().setGameMode(gameMode);
            teamManager.initTeams();
        }
    }

    @Override
    public void reset() {
        this.started = false;
        this.nextTick = 11 * 20;
        this.currentTicks = 0;
        gamePhase = GamePhase.UNPLAYED;
        players = new java.util.HashSet<>();
        alivePlayers.clear();
        deathOrder.clear();
        this.bossBar.progress(0.0f);
        this.bossBar.color(BossBar.Color.BLUE);
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.hideBossBar(this.bossBar);
            GamePlayer gamePlayer = gamePlayerManager.getGamePlayer(player);
            gamePlayer.resetFirstElytra();
            gamePlayer.updatePlayerBoard();
            GameTeam gameTeam = gamePlayer.getTeam();
            if (gameTeam != null) {
                for (GamePlayer otherGamePlayer : gameTeam.getPlayersWithout(player.getUniqueId())) {
                    Player teammate = otherGamePlayer.getPlayer();
                    if (teammate != null) gutitubowManagers.getGlowManager().removeGlow(player, teammate);
                }
            }
            Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(20);
            player.setHealth(20.0);
            for (Player otherPlayer : Bukkit.getServer().getOnlinePlayers()) {
                if (player.getUniqueId() != otherPlayer.getUniqueId()) {
                    player.showPlayer(gutitubow, otherPlayer);
                    Scoreboard scoreboard = scoreboardManager.getScoreboard(player);
                    Team team = scoreboardManager.getTeam(scoreboard, otherPlayer);
                    team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
                }
            }
        }
        resetSoloPlayerTeams();
        if (arena != null) {
            arena.getBorder().setCenter(0.0, 0.0);
            arena.getBorder().setSize(59999968);
            arena.getBorder().setDamageBuffer(0.0);
            safeArea = arena.getGameMap().getCenter();
            safeArea.setWorld(arena.getWorld());
        }

        for (GutitubowEntity gutitubowEntity : gutitubow.getGutitubowEntityManager().getGutitubowEntities()) {
            if (gutitubowEntity instanceof CarePackage carePackage) {
                carePackage.remove();
            }
        }

        List<EntityType> removeEntityType = List.of(EntityType.ARROW, EntityType.ITEM);
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (removeEntityType.contains(entity.getType())) entity.remove();
            }
        }

        this.mapRepairManager.repairBrokeBlocks();
        this.mapRepairManager.repairPlacedBlocks();
        this.mapRepairManager.reset();
    }


    private void init() {
        nextTick = gameSetting.getPhase0seconds() * 20;
        arena.getBorder().setCenter(arena.getGameMap().getCenter());
        arena.getBorder().setSize(arena.getGameMap().getSize());
        safeArea = arena.getGameMap().getRandomSafeArea();
        safeArea.setWorld(arena.getWorld());
        deathOrder.clear();
        alivePlayers.clear();
        AbilitySelector abilitySelector = new AbilitySelector(getGameSetting().getDisabledAbilities());
        abilitySelector.resetCurrentAbilities();
        resetSoloPlayerTeams();

        for (Player player : Bukkit.getOnlinePlayers()) {
            showBossBar(player);
            if (!players.contains(getGamePlayer(player))) {
                player.setGameMode(GameMode.SPECTATOR);
            }
        }

        // ゲームプレイヤーの処理
        for (GamePlayer gamePlayer : players) {
            Player player = gamePlayer.getPlayer();
            if (player == null || !player.isOnline()) continue;
            alivePlayers.add(gamePlayer);
            Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(gameSetting.getDefaultHealth());
            player.setHealth(gameSetting.getDefaultHealth());
            if (!debugManager.isNoStartTeleport()) {
                Location location = MathUtil.randomLocation(gameMap.getCenter(), gameMap.getSize() - 30 * 2);
                location.setWorld(arena.getWorld());
                location.setY(gameMap.getSpawnHeight());
                location.getDirection().setY(0);
                player.teleport(location);
            }

            gamePlayer.resetMaxArrowStackSize();
            gamePlayer.resetArrowReloadTick();
            gamePlayer.resetTotalHit();
            gamePlayer.setTotalKill(0);

            // インベントリの初期化
            PlayerInventory inventory = player.getInventory();
            inventory.clear();
            for (int i = 9; i <= 26; i++) inventory.setItem(i, GutitubowItem.INVALID_SLOT_BLACK);
            inventory.setItem(0, GutitubowItem.GUTITUBOW);
            inventory.setItem(6, GutitubowItem.AREA_COMPASS);
            inventory.setItem(7, GutitubowItem.SPYGLASS);

            player.setCompassTarget(safeArea);
            gamePlayer.setArrowAmount(0);

            // 能力を割り当てる
            AbilityType ability = abilitySelector.nextAbility();
            if (gameSetting.getSingleAbility() != null) ability = gameSetting.getSingleAbility();

            AbilityManager abilityManager = gutitubowManagers.getAbilityManager();
            abilityManager.setAbility(player, ability);
            abilityManager.setDisplayItem(player, ability, 13);

            Component abilityMessage = Component.text("あなたの能力は「").color(NamedTextColor.GOLD).append(ability.getName()).append(Component.text("」です"));
            Title title = Title.title(
                    Component.text("GAME START").color(NamedTextColor.GOLD),
                    abilityMessage,
                    Title.Times.times(
                            java.time.Duration.ofMillis(100),
                            java.time.Duration.ofSeconds(1),
                            java.time.Duration.ofMillis(500)
                    )
            );
            player.showTitle(title);
            player.sendMessage(abilityMessage);

            if (!debugManager.isNoStartTeleport()) gamePlayer.firstElytra();

            // Team
            GutitubowGameMode gameMode = getGameSetting().getGameMode();
            if (gameMode == GutitubowGameMode.DUO || gameMode == GutitubowGameMode.TRIO) {
                GameTeam gameTeam = teamManager.getPlayerTeam(player);
                if (gameTeam == null) continue;
                if (teamManager.isJumpMaster(player)) {
                    Player topPlayer = player;
                    for (GamePlayer teamPlayer : gameTeam.getPlayers()) {
                        if (teamPlayer.getPlayerUUID().equals(player.getUniqueId())) continue;
                        Player teammate = teamPlayer.getPlayer();
                        if (teammate == null || !teammate.isOnline()) continue;
                        if (!debugManager.isNoStartTeleport()) topPlayer.addPassenger(teammate);
                        topPlayer = teammate;
                        gutitubowManagers.getGlowManager().addGlow(player, teammate);
                    }
                }
            }

            // 他のプレイヤーへの処理
            for (GamePlayer otherGamePlayer : players) {
                Player otherPlayer = otherGamePlayer.getPlayer();
                if (otherPlayer != null && player.getUniqueId() != otherPlayer.getUniqueId()) {
                    if (!teamManager.isTeammate(player, otherPlayer)) {
                        player.hidePlayer(gutitubow, otherPlayer);

                        Scoreboard scoreboard = scoreboardManager.getScoreboard(player);
                        Team otherPlayerTeam = scoreboardManager.getTeam(scoreboard, otherPlayer);
                        otherPlayerTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

                    }
                }
            }
        }
    }

    private void resetSoloPlayerTeams() {
//        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
//            Scoreboard board = scoreboardManager.getScoreboard(onlinePlayer);
//            for (Team team : board.getTeams()) {
//                if (team.getName().startsWith("Self-") || team.getName().startsWith("Enemy-")) {
//                    team.unregister();
//                }
//            }
//        }
    }

    protected void update() {
        if (!isStarted()) return;
        this.currentTicks++;

        if (this.currentTicks > this.nextTick && !isFinalPhase()) this.phaseUpdate();

        // ケアパッケージの生成
        switch (gamePhase) {
            case PHASE1 -> {
                if (this.currentTicks == 0 || this.currentTicks == 60 * 20 || this.currentTicks == 120 * 20) {
                    randomCarePackageSpawn(gameSetting.getCarePackageAmount());
                }
            }
            case PHASE2, PHASE3 -> {
                if (this.currentTicks == 0 || this.currentTicks == 60 * 20) {
                    randomCarePackageSpawn(gameSetting.getCarePackageAmount());
                }
            }
        }

        // 範囲外ケアパッケージ削除
        if (isShrinking()) {
            for (GutitubowEntity gutitubowEntity : gutitubow.getGutitubowEntityManager().getGutitubowEntities()) {
                if (gutitubowEntity instanceof CarePackage carePackage) {
                    if (!arena.getBorder().isInside(carePackage.getLocation())) carePackage.remove();
                }
            }
        }

        // ボスバーの表示更新
        this.bossBar.progress((float) (isFinalPhase() ? 0.0 : 1.0 - this.getPhaseProgress()));

        int remainedSeconds = (this.nextTick - this.currentTicks) / 20;
        String remainedText = gamePhase == GamePhase.PHASE0 ? "セーフエリア決定まで後 " : isShrinking() ? "次のフェーズまで後 " : "縮小開始まで後 ";
        if (gamePhase != GamePhase.PRE_FINAL_PHASE  && !isFinalPhase()) {
            bossBar.name(Component.text("現在のフェーズ " + getPhase().getPhaseNumber() + ": " + remainedText + remainedSeconds + "秒"));
        }

        //  最終フェーズカウントダウン表示
        Component lastPhaseRemainedText = null;
        if (currentTicks % 20 == 0 && gamePhase == GamePhase.PRE_FINAL_PHASE) {
            lastPhaseRemainedText =
                    Component.text("あと").color(NamedTextColor.GOLD)
                            .append(Component.text(remainedSeconds).color(NamedTextColor.RED)
                                    .append(Component.text("秒で").color(NamedTextColor.GOLD)
                                            .append(Component.text("最終フェーズ").color(NamedTextColor.RED)
                                                    .append(Component.text("が始まります").color(NamedTextColor.GOLD)
                                                    ))));
            bossBar.name(lastPhaseRemainedText);
        }


        // プレイヤーへの処理
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {

            if (remainedSeconds <= 10 && this.currentTicks % 20 == 1 && gamePhase == GamePhase.STARTING) {
                Title title = Title.title(
                        Component.text("後").color(NamedTextColor.GOLD)
                                .append(Component.text(remainedSeconds).color(NamedTextColor.RED)
                                        .append(Component.text("秒で始まります").color(NamedTextColor.GOLD))
                                ),
                        Component.empty(),
                        Title.Times.times(
                                java.time.Duration.ofMillis(0),
                                java.time.Duration.ofMillis(1100),
                                java.time.Duration.ofMillis(0)
                        )
                );
                player.showTitle(title);
            }

            if (gamePhase == GamePhase.PHASE0 && (currentTicks + 10) % 20 == 0) {
                for (GamePlayer gamePlayer : alivePlayers) {
                    Player target = gamePlayer.getPlayer();
                    if (target != null && !player.getUniqueId().equals(gamePlayer.getPlayerUUID()) && !teamManager.isTeammate(player, target)) {
                        Location base = target.getLocation();
                        player.spawnParticle(
                                Particle.SMOKE,
                                base.clone().add(0.0, 1.0, 0.0),
                                30,
                                1.25, 1.5, 1.25,
                                0
                        );
                    }
                }
            }

            if (gamePhase.getPhaseNumber() >= 1 && gamePhase.getPhaseNumber() <= 5 && isShrinking()) {
                Location endLocation = arena.getGameMap().getCenter();
                endLocation.setWorld(arena.getWorld());
                arena.getBorder().setCenter(MathUtil.getLocationByProgress(endLocation, safeArea, getShrinkProgress()));
            }

            if (remainedSeconds <= 10 && this.currentTicks % 20 == 0 && !isFinalPhase()) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }

            if (gamePhase == GamePhase.PRE_FINAL_PHASE && remainedSeconds <= 10 && this.currentTicks % 20 == 0 && lastPhaseRemainedText != null) {
                showSubtitle(player, lastPhaseRemainedText);
            }

        }

        // 最終フェーズ処理
        if (isFinalPhase()) {
            int lastPhaseCycleCurrentTick = currentTicks % LAST_PHASE_CYCLE_TICK;
            if (lastPhaseCycleCurrentTick == 0) {
                oldSafeArea = safeArea.clone();
                Location newSafeArea = arena.getGameMap().getRandomSafeArea();
                newSafeArea.setWorld(arena.getWorld());

                double maxDistance = 15.0;

                Vector diff = newSafeArea.clone().toVector().subtract(safeArea.clone().toVector());
                double distance = diff.length();

                if (distance > maxDistance) {
                    diff.normalize().multiply(maxDistance);
                    newSafeArea = safeArea.clone().add(diff);
                }

                safeArea = newSafeArea.clone();
            }
            if (oldSafeArea == null) {
                oldSafeArea = safeArea.clone();
            }
            float progress = (float) lastPhaseCycleCurrentTick / (float) LAST_PHASE_CYCLE_TICK;
            arena.getBorder().setCenter(MathUtil.getLocationByProgress(oldSafeArea, safeArea, progress));
        }

    }

    private float getShrinkProgress() {
        return getShrinkProgress(gamePhase);
    }

    private float getShrinkProgress(GamePhase gamePhase) {
        if (gamePhase.getPhaseNumber() >= 1 && gamePhase.getPhaseNumber() <= 5) {
            float phaseTick = (30 * 20 + 20);
            float base = phaseTick * 5;
            float current = phaseTick * (gamePhase.getPhaseNumber() - 1) + currentTicks;
            return current / base;
        }
        return 0.0f;
    }

    private void randomCarePackageSpawn(int count) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("[!] ケアパッケージが投下されました！").color(NamedTextColor.RED));
            player.sendMessage(Component.empty());
            NamespacedKey recipeKey = new NamespacedKey("minecraft", "chest");
            player.undiscoverRecipe(recipeKey);
            player.discoverRecipe(recipeKey);
        }
        for (int i = 0; i < count; i++) {
            Location location = MathUtil.randomLocation(safeArea, getNextSafeAreaSize() - 10);
            location.setY(175);
            Gutitubow.get().getGutitubowEntityManager().spawnGutitubowEntity(
                    location,
                    CarePackage.class
            );
        }
    }

    private int getNextPhaseTick(GamePhase gamePhase) {
        switch (gamePhase) {
            case PHASE1 -> {
                return 180 * 20;
            }
            case PHASE2, PHASE3 -> {
                return 120 * 20;
            }
            case PHASE4, PHASE5 -> {
                return 60 * 20;
            }
            case PRE_FINAL_PHASE -> {
                return 15 * 20;
            }
            case null, default -> {
                return 30 * 20;
            }
        }
    }

    private void phaseUpdate() {
        gamePhase = gamePhase.getNextPhase();
        if (isShrinking()) {
            bossBar.color(BossBar.Color.RED);
            nextTick = 30 * 20;
            int borderChangeTicks = 31 * 20;
            arena.getBorder().changeSize(getNextSafeAreaSize(), borderChangeTicks);

        } else {
            if (gamePhase == GamePhase.PHASE0) init();
            bossBar.color(gamePhase == GamePhase.FINAL_PHASE || gamePhase == GamePhase.PRE_FINAL_PHASE ? BossBar.Color.RED : BossBar.Color.BLUE);

            if (gamePhase != GamePhase.PHASE0) {
                if (debugManager.isQuickPhase()) {
                    nextTick = 15 * 20;
                } else nextTick = getNextPhaseTick(gamePhase);
            }
        }

        if (isFinalPhase()) {
            this.bossBar.name(Component.text("最終フェーズ進行中"));
        }

        if (!isShrinking()) {
            GameEventControllerGameEventType oldGameEventControllerGameEventType = getPhaseEventType(gamePhase.getPhaseNumber() - 1);
            if (oldGameEventControllerGameEventType != null) {
                GameEvent oldGameEvent = getGameSetting().getGameEventController().getEvent(oldGameEventControllerGameEventType);
                if (oldGameEvent != null) oldGameEvent.deactivate(true);
            }

            GameEventControllerGameEventType nextGameEventControllerGameEventType = getPhaseEventType(gamePhase.getPhaseNumber());
            if (nextGameEventControllerGameEventType != null) {
                GameEvent nextGameEvent = getGameSetting().getGameEventController().getEvent(nextGameEventControllerGameEventType);
                if (nextGameEvent != null) Bukkit.getScheduler().runTaskLater(Gutitubow.get(), () -> nextGameEvent.activate(true), 60L);
            }

        }

        // Player への処理
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.75f, 1.0f);
            if (isFinalPhase()) {
                showSubtitle(player,
                        Component.text("最終フェーズ").color(NamedTextColor.RED)
                                .append(Component.text("が始まりました").color(NamedTextColor.GOLD))
                );
            }
            if (gamePhase == GamePhase.PHASE1 && !isShrinking()) {
                for (GamePlayer gamePlayer : players) {
                    Player target = gamePlayer.getPlayer();
                    if (target != null) {
                        player.showPlayer(gutitubow, target);
                    }
                }
            }

            if (gamePhase.getPhaseNumber() >= 1 && gamePhase.getPhaseNumber() <= 5 && isShrinking()) {
                if (!isInsideNextSafeArea(player.getLocation())) {
                    Title title = Title.title(
                            Component.empty(),
                            Component.text("セーフエリア外にいる！ コンパスの方向に向かえ！").color(NamedTextColor.DARK_RED),
                            Title.Times.times(
                                    java.time.Duration.ofMillis(500),
                                    java.time.Duration.ofMillis(2000),
                                    java.time.Duration.ofMillis(500)
                            )
                    );
                    player.showTitle(title);
                }
            }

            if (gamePhase.getPhaseNumber() >= 1 && gamePhase.getPhaseNumber() <= 5 && !isShrinking()) {
                if (Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue() - player.getHealth() < 2.0) {
                    player.give(GutitubowItem.RECOVERY_POTION);
                    player.sendMessage(Component.text("\n[!]").color(NamedTextColor.GOLD)
                            .append(Component.text("フェーズを生き延びたため、回復ポーションを入手しました。\n").color(NamedTextColor.GREEN))
                    );
                } else {
                    player.setHealth(player.getHealth() + 2.0);
                    player.sendMessage(Component.text("\n[!]").color(NamedTextColor.GOLD)
                            .append(Component.text("フェーズを生き延びたため、体力が回復しました。\n").color(NamedTextColor.GREEN))
                    );
                }
            }
        }

        this.currentTicks = 0;
        this.nextTick += 20;
    }

    private @Nullable GameEventControllerGameEventType getPhaseEventType(int phase) {
        return switch (phase) {
            case 2 -> GameEventControllerGameEventType.PHASE2;
            case 3 -> GameEventControllerGameEventType.PHASE3;
            case 4 -> GameEventControllerGameEventType.PHASE4;
            default -> null;
        };
    }

    @Override
    public double getNextSafeAreaSize() {
        return switch (gamePhase) {
            case PHASE0, PHASE1, PHASE1_SHRINKING -> 150;
            case PHASE2, PHASE2_SHRINKING -> 105;
            case PHASE3, PHASE3_SHRINKING -> 65;
            case PHASE4, PHASE4_SHRINKING -> 31;
            case PHASE5, PHASE5_SHRINKING, PRE_FINAL_PHASE, FINAL_PHASE -> 10;
            default -> 300;
        };
    }

    @Override
    public boolean kill(@NotNull GamePlayer gamePlayer, @NotNull DamageSource damageSource) {
        GamePlayerDeathEvent deathEvent = new GamePlayerDeathEvent(gamePlayer, damageSource);
        Bukkit.getPluginManager().callEvent(deathEvent);
        if (deathEvent.isCancelled()) return false;

        alivePlayers.remove(gamePlayer);
        if (!deathOrder.contains(gamePlayer)) {
            deathOrder.remove(gamePlayer);
            deathOrder.add(gamePlayer);
        }

        if (!debugManager.isDisableCheckGameOver() && isStarted()) checkGameOver();


        return true;
    }

    @Override
    public void revive(@NotNull final Player player, @NotNull final Player reviver) {
        GamePlayer gamePlayer = getGamePlayer(player);
        GamePlayer reviverGamePlayer = getGamePlayer(reviver);
        World world = player.getWorld();

        alivePlayers.add(gamePlayer);
        deathOrder.remove(gamePlayer);

        player.setGameMode(GameMode.ADVENTURE);
        if (player.getUniqueId() != reviver.getUniqueId()) player.teleport(reviver);
        LogManager.broadcastReviveLog(player, reviver);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f);

        PotionEffect health = new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 255, true, false, false);
        player.addPotionEffect(health);

        // Firework
        Firework firework = world.spawn(player.getLocation().add(0.0, 1.5, 0.0), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(Color.AQUA).with(FireworkEffect.Type.BALL).build());
        meta.setPower(0);
        firework.setFireworkMeta(meta);
    }

    @Override
    public void reviveSilent(@NotNull GamePlayer gamePlayer) {
        alivePlayers.add(gamePlayer);
        deathOrder.remove(gamePlayer);
    }

    @Override
    public void checkGameOver() {
        if (alivePlayers.isEmpty()) {
            gameOver();
        } else {
            GutitubowGameMode gameMode = getGameSetting().getGameMode();
            if (gameMode == GutitubowGameMode.SOLO) {
                if (alivePlayers.size() == 1) gameOver(alivePlayers.iterator().next().getPlayer());
            } else if (gameMode == GutitubowGameMode.DUO || gameMode == GutitubowGameMode.TRIO) {
                if (teamManager.getAliveTeams().size() == 1) {
                    GameTeam winTeam = teamManager.getAliveTeams().stream().toList().getFirst();
                    gameOver(winTeam);
                }
            }
        }
    }

    @Override
    public void showBossBar(@NotNull Player player) {
        player.showBossBar(bossBar);
    }

    @Override
    public void hideBossBar(@NotNull Player player) {
        player.hideBossBar(bossBar);
    }

    private void gameOver() {
        this.started = false;
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.setGameMode(GameMode.ADVENTURE);
            player.stopSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER);
            player.stopSound(Sound.ENTITY_LIGHTNING_BOLT_IMPACT);
            player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    private void gameOver(Player winner) {
        gameOver();
        GamePlayer gamePlayer = getGamePlayer(winner);

        Title title = Title.title(
                Component.text(winner.getName() + " WON!").color(NamedTextColor.GOLD),
                Component.text(gamePlayer.getTotalKill() + "Kill, " + gamePlayer.getTotalHit() + "Hit").color(NamedTextColor.YELLOW),
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofSeconds(3),
                        java.time.Duration.ofMillis(500)
                )
        );

        Component rankingComponent = getRankingComponent(winner);

        Bukkit.getServer().broadcast(Component.text("\n§lGame Set!\n").color(NamedTextColor.LIGHT_PURPLE));
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.showTitle(title);
            player.sendMessage(rankingComponent);
        }
        reset();
        GamePlayerWinEvent event = new GamePlayerWinEvent(winner);
        Bukkit.getPluginManager().callEvent(event);
    }

    private void gameOver(GameTeam team) {
        gameOver();
        Component playersName = Component.empty();

        Iterator<GamePlayer> teamPlayers = team.getPlayers().iterator();
        while (teamPlayers.hasNext()) {
            GamePlayer gamePlayer = teamPlayers.next();
            Player winteamPlayer = gamePlayer.getPlayer();
            if (winteamPlayer != null) {
                playersName = playersName.append(Component.text(winteamPlayer.getName()));
                if (teamPlayers.hasNext()) playersName = playersName.append(Component.text(" & "));
                GamePlayerWinEvent event = new GamePlayerWinEvent(winteamPlayer);
                Bukkit.getPluginManager().callEvent(event);
            }
        }

        Title title = Title.title(
                Component.text("FLAWLESS!").color(NamedTextColor.GOLD),
                playersName.color(NamedTextColor.WHITE),
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofSeconds(3),
                        java.time.Duration.ofMillis(500)
                )
        );

        Component rankingComponent = Component.empty();

        for (GamePlayer gamePlayer : team.getPlayers()) {
            Player teamPlayer = gamePlayer.getPlayer();
            boolean isLast = gamePlayer == team.getPlayers().getLast();
            rankingComponent = rankingComponent.append(getRankingPlayerComponent(teamPlayer, 1, team.getTeamColor()));
            if (!isLast) rankingComponent = rankingComponent.append(Component.text("\n"));
        }

        int rank = 2;
        List<GamePlayer> otherPlayers = new ArrayList<>(deathOrder.reversed());
        otherPlayers.removeAll(team.getPlayers());
        for (GamePlayer otherGamePlayer : otherPlayers) {
            Player player = otherGamePlayer.getPlayer();
            if (player == null) continue;
            rankingComponent = rankingComponent.append(Component.text("\n"));
            rankingComponent = rankingComponent.append(getRankingPlayerComponent(player, rank, team.getTeamColor()));
            rank++;
        }

        Bukkit.getServer().broadcast(Component.text("\n§lGame Set!\n").color(NamedTextColor.LIGHT_PURPLE));
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.showTitle(title);
            player.sendMessage(rankingComponent);
        }
        reset();
    }

    private Component getRankingComponent(Player winner) {
        GamePlayer winnerGamePlayer = getGamePlayer(winner);
        deathOrder.add(winnerGamePlayer);

        Component rankingComponent = Component.text("");

        for (int i = 0; i < deathOrder.size(); i++) {
            Player player = deathOrder.reversed().get(i).getPlayer();
            boolean isLast = i == deathOrder.size() - 1;

            if (player != null) {
                rankingComponent = rankingComponent.append(getRankingPlayerComponent(player, i + 1));
                if (!isLast) rankingComponent = rankingComponent.append(Component.text("\n"));
            }
        }
        return rankingComponent;
    }

    private Component getRankingPlayerComponent(Player player, int rank, NamedTextColor teamColor) {
        GamePlayer gamePlayer = getGamePlayer(player);
        Ability ability = gamePlayer.getAbility();
        Component abilityTypeName = (ability != null) ? ability.getType().getName() : Component.text("なし");
        Component rankComponentSuffix = switch (rank) {
            case (1) -> Component.text("st");
            case (2) -> Component.text("nd");
            case (3) -> Component.text("rd");
            default -> Component.text("th");
        };
        Component playerComponent =
                Component.text(player.getName() + " [")
                        .append(abilityTypeName)
                        .append(Component.text("] - " + gamePlayer.getTotalKill() + "Kill, " + gamePlayer.getTotalHit() + "Hit"))
                        .color((rank == 1) ? NamedTextColor.YELLOW : teamColor)
                        .decoration(TextDecoration.BOLD, false);
        Component rankComponent =
                Component.text(rank)
                        .append(rankComponentSuffix)
                        .append(Component.text(". "))
                        .color((rank == 1) ? NamedTextColor.YELLOW : NamedTextColor.RED)
                        .decoration(TextDecoration.BOLD, true);
        return rankComponent.append(playerComponent);
    }

    private Component getRankingPlayerComponent(Player player, int rank) {
        return getRankingPlayerComponent(player, rank, NamedTextColor.RED);
    }

    private void showSubtitle(Player player, Component subtitle) {
        Title title = Title.title(
                Component.empty(),
                subtitle,
                Title.Times.times(
                        java.time.Duration.ofMillis(0),
                        java.time.Duration.ofMillis(1100),
                        java.time.Duration.ofMillis(0)
                )
        );
        player.showTitle(title);
    }

    @Override
    public @NotNull MapRepairManager getMapRepairManager() {
        return this.mapRepairManager;
    }

    @Override
    public boolean isInsideNextSafeArea(@NotNull Location loc) {

        if (!loc.getWorld().equals(safeArea.getWorld())) return false;
        GamePhase nextGamePhase = switch (gamePhase) {
            case PHASE0, PHASE1, PHASE1_SHRINKING -> GamePhase.PHASE2;
            case PHASE2, PHASE2_SHRINKING -> GamePhase.PHASE3;
            case PHASE3, PHASE3_SHRINKING -> GamePhase.PHASE4;
            case PHASE4, PHASE4_SHRINKING -> GamePhase.PHASE5;
            case PHASE5, PHASE5_SHRINKING -> GamePhase.FINAL_PHASE;
            default -> GamePhase.FINAL_PHASE;
        };
        Location nextPhaseSafeAreaLoc = MathUtil.getLocationByProgress(arena.getGameMap().getCenter(), safeArea, getShrinkProgress(nextGamePhase));
        return Math.abs(loc.getX() - nextPhaseSafeAreaLoc.getX()) <= getNextSafeAreaSize() / 2 && Math.abs(loc.getZ() - nextPhaseSafeAreaLoc.getZ()) <= getNextSafeAreaSize() / 2;
    }

    @Override
    public PlayerBoard getPlayerBoard(@NotNull GamePlayer gamePlayer) {
        return playerBoardManager.getPlayerBoard(gamePlayer);
    }

    @Override
    public void destroy() {
        reset();
        teamManager.removeAllTeam();
        if (arena != null) gutitubowManagers.getArenaManager().destroy(arena);
    }

    @Override
    public GameMap getGameMap() {
        return gameMap;
    }

    @Override
    public void setGameMap(@NotNull GameMap gameMap) {
        this.gameMap = gameMap;
         Location lobby = gameMap.getLobby();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(lobby);
        }
        try {
            if (arena != null) gutitubowManagers.getArenaManager().destroy(arena);
            arena = null;
            arena = gutitubowManagers.getArenaManager().create(gameMap);

        } catch (IOException ignored) {

        }
    }

    @Override
    public @Nullable Arena getArena() {
        return arena;
    }

    @Override
    public void resetArena() {
        if (arena == null) return;

        Location safe = Bukkit.getWorld("world").getSpawnLocation();
        Map<UUID, Location> playerLocationMap = new HashMap<>();

        PlayerUtil.playerConsumerOnWorld(arena.getWorld(), player -> {
            playerLocationMap.put(player.getUniqueId(), player.getLocation().clone());
            player.teleport(safe);
        });

        Bukkit.getScheduler().runTaskLater(gutitubow, () -> {
            try {
                if (arena != null) gutitubowManagers.getArenaManager().destroy(arena);
                arena = null;
                arena = gutitubowManagers.getArenaManager().create(gameMap);

            } catch (IOException ignored) {

            }
        }, 20L);

        Bukkit.getScheduler().runTaskLater(gutitubow, () -> {
            for (UUID playerUUID : playerLocationMap.keySet()) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player == null) continue;

                Location location = playerLocationMap.get(playerUUID);
                location.setWorld(arena.getWorld());
                player.teleport(location);
            }
        },  5 * 20L);

    }

    @Override
    public @NotNull GameGUI getGameGUI() {
        return gameGUI;
    }

    @Override
    public @NotNull GameMapSelectorGUI getGameMapSelectorGUI() {
        return gameMapSelectorGUI;
    }

    @Override
    public @NotNull GlassBreakManager getGlassBreakManager() {
        return this.glassBreakManager;
    }

    @Override
    public @NotNull TeamManager getTeamManager() {
        return teamManager;
    }

    @Override
    public @NotNull SpectatorManager getSpectatorManager() {
        return spectatorManager;
    }

    @Override
    public @NotNull GutitubowManagers getGutitubowManagers() {
        return gutitubowManagers;
    }

}
