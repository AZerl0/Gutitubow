package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.GutitubowManagers;
import com.azerl0.gutitubow.ability.*;
import com.azerl0.gutitubow.arena.Arena;
import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.carepackage.CarePackage;
import com.azerl0.gutitubow.data.item.GutitubowItem;
import com.azerl0.gutitubow.debug.DebugManager;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerWinEvent;
import com.azerl0.gutitubow.gui.guis.GameMapSelectorGUI;
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
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class Game {
    private final Gutitubow gutitubow;
    private final GameSetting gameSetting = new GameSetting();
    private boolean started;
    private int nextTick;
    private int currentTicks;
    private GamePhase gamePhase;
    private Set<UUID> players;
    private final Set<UUID> alivePlayers = new HashSet<>();
    private final List<UUID> deathOrder = new ArrayList<>();
    private final BossBar bossBar = BossBar.bossBar(Component.text(""), 0.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
    private Arena arena;
    private GameMap gameMap;
    private final int LAST_PHASE_CYCLE_TICK = 15 * 20;
    private Location safeArea;
    private Location oldSafeArea;
    private final GameMapSelectorGUI gameMapSelectorGUI;
    private final GutitubowManagers gutitubowManagers;
    private final GamePlayerManager gamePlayerManager;
    private final AbilityManager abilityManager;
    private final DebugManager debugManager;
    private final MapRepairManager mapRepairManager = new MapRepairManager();
    private final GlassBreakManager glassBreakManager = new GlassBreakManager(mapRepairManager);
    private final ScoreboardManager scoreboardManager;
    private final PlayerBoardManager playerBoardManager;
    private final TeamManager teamManager;
    private final SpectatorManager spectatorManager = new SpectatorManager();

    public Game(
            Gutitubow gutitubow,
            DebugManager debugManager,
            GutitubowManagers gutitubowManagers
    ) {
        this.gutitubow = gutitubow;
        this.gutitubowManagers = gutitubowManagers;
        this.debugManager = debugManager;
        AbilityContext abilityContext = new AbilityContext(gutitubow, this, gutitubowManagers);
        this.abilityManager = new AbilityManager(abilityContext);
        this.gamePlayerManager = new GamePlayerManager(gutitubow, this);
        this.scoreboardManager = gutitubowManagers.getScoreboardManager();
        this.playerBoardManager = new PlayerBoardManager(scoreboardManager);
        this.teamManager = new TeamManager(this);
        gameMapSelectorGUI = new GameMapSelectorGUI(this);
        reset();
    }

    public @NotNull GameSetting getGameSetting() {
        return gameSetting;
    }

    public @NotNull GamePlayer getGamePlayer(@NotNull Player player) {
        return gamePlayerManager.getGamePlayer(player);
    }

    public @Nullable GamePlayer getGamePlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;
        return getGamePlayer(player);
    }

    public @Nullable GameTeam getGameTeam(Player player) {
        return teamManager.getPlayerTeam(player);
    }

    public @Nullable GameTeam getGameTeam(UUID uuid) {
        return teamManager.getPlayerTeam(uuid);
    }

    public boolean isGamePlayer(Player player) {
        return getPlayers().contains(player.getUniqueId());
    }

    public boolean isTeammate(Player player, Player target) {
        if (player.getUniqueId() == target.getUniqueId()) return true;
        GameTeam gameTeam = getGameTeam(player);
        if (gameTeam == null) return false;
        return gameTeam.isTeammate(target);
    }

    public boolean isTeammate(UUID player, UUID target) {
        if (player == target) return true;
        GameTeam gameTeam = getGameTeam(player);
        if (gameTeam == null) return false;
        return gameTeam.isTeammate(target);

    }

    public boolean isStarted() {
        return started;
    }

    public void start() {
        started = true;
        gamePhase = GamePhase.STARTING;
        players.clear();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (getSpectatorManager().isSpectator(player)) continue;
            players.add(player.getUniqueId());
        }
    }



    public GamePhase getPhase() {
        return gamePhase;
    }

    public boolean isFinalPhase() {
        return getPhase() == GamePhase.FINAL_PHASE;
    }

    public Location getSafeArea() {
        return safeArea.clone();
    }

    public boolean isShrinking() {
        return gamePhase.isShrinking();
    }

    public double getProgress() {
        return (double) this.currentTicks / (double) this.nextTick;
    }

    public Set<UUID> getPlayers() {
        return this.players;
    }

    public Set<GamePlayer> getCurrentGamePlayers() {
        Set<GamePlayer> gamePlayers = new HashSet<>();
        getPlayers().forEach(uuid -> gamePlayers.add(getGamePlayer(uuid)));
        return gamePlayers;
    }

    public Set<GamePlayer> getAllGamePlayers() {
        return gamePlayerManager.getGamePlayers();
    }

    public Set<UUID> getAlivePlayers() {
        return this.alivePlayers;
    }

    public List<GamePlayer> getDeadPlayer() {
        List<GamePlayer> deadPlayers = new ArrayList<>();
        deathOrder.forEach(uuid -> deadPlayers.add(getGamePlayer(uuid)));
        return deadPlayers;
    }

    public void setGameMode(GutitubowGameMode gameMode) {
        if (getGameSetting().getGameMode() != gameMode) {
            if (gameMode == GutitubowGameMode.DUO || gameMode == GutitubowGameMode.TRIO) {
                teamManager.resetTeams();
            }
            getGameSetting().setGameMode(gameMode);
            teamManager.initTeams();
        }
    }

    public void reset() {
        this.started = false;
        this.nextTick = 11 * 20;
        this.currentTicks = 0;
        gamePhase = GamePhase.UNPLAYED;
        this.players = new java.util.HashSet<>();
        this.alivePlayers.clear();
        this.deathOrder.clear();
        this.bossBar.progress(0.0f);
        this.bossBar.color(BossBar.Color.BLUE);
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.hideBossBar(this.bossBar);
            GamePlayer gamePlayer = gamePlayerManager.getGamePlayer(player);
            gamePlayer.resetFirstElytra();
            gamePlayer.updatePlayerBoard();
            GameTeam gameTeam = gamePlayer.getTeam();
            if (gameTeam != null) {
                for (UUID uuid : gameTeam.getPlayersWithout(player.getUniqueId())) {
                    Player teammate = Bukkit.getPlayer(uuid);
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
            arena.getGameMap().getBorder().setCenter(0.0, 0.0);
            arena.getGameMap().getBorder().setSize(59999968);
            arena.getGameMap().getBorder().setDamageBuffer(0.0);
            safeArea = arena.getGameMap().getCenter();
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
        arena.getGameMap().getBorder().setCenter(arena.getGameMap().getCenter());
        arena.getGameMap().getBorder().setSize(arena.getGameMap().getSize());
        safeArea = arena.getGameMap().getRandomSafeArea();
        deathOrder.clear();
        alivePlayers.clear();
        AbilitySelector abilitySelector = new AbilitySelector(getGameSetting().getDisabledAbilities());
        abilitySelector.resetCurrentAbilities();
        resetSoloPlayerTeams();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showBossBar(this.bossBar);
            if (!players.contains(player.getUniqueId())) {
                player.setGameMode(GameMode.SPECTATOR);
            }
        }

        // ゲームプレイヤーの処理
        for (UUID uuid : this.players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            alivePlayers.add(uuid);
            Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(gameSetting.getDefaultHealth());
            player.setHealth(gameSetting.getDefaultHealth());
            if (!debugManager.isNoStartTeleport()) {
                Location location = MathUtil.randomLocation(safeArea, arena.getGameMap().getSize() - 30 * 2);
                location.setY(200.0);
                location.getDirection().setY(0);
                player.teleport(location);
            }

            GamePlayer gamePlayer = gamePlayerManager.getGamePlayer(player);

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

            if (!debugManager.isNoStartTeleport()) gamePlayer.firstElytra();

            // 能力を割り当てる
            AbilityType ability = abilitySelector.nextAbility();
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


            GutitubowGameMode gameMode = getGameSetting().getGameMode();
            if (gameMode == GutitubowGameMode.DUO || gameMode == GutitubowGameMode.TRIO) {
                GameTeam gameTeam = teamManager.getPlayerTeam(player);
                if (gameTeam == null) continue;
                if (teamManager.isJumpMaster(player)) {
                    Player topPlayer = player;
                    for (UUID teamPlayerUUID : gameTeam.getPlayers()) {
                        if (teamPlayerUUID == player.getUniqueId()) continue;
                        Player teammate = Bukkit.getPlayer(teamPlayerUUID);
                        if (teammate == null || !teammate.isOnline()) continue;
                        if (!debugManager.isNoStartTeleport()) topPlayer.addPassenger(teammate);
                        topPlayer = teammate;
                        gutitubowManagers.getGlowManager().addGlow(player, teammate);
                    }
                }
            }

            // 他のプレイヤーへの処理
            for (UUID otherPlayerUUID : players) {
                Player otherPlayer = Bukkit.getPlayer(otherPlayerUUID);
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

    public void tick() {
        this.update();
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

    private void update() {
        this.currentTicks++;

        if (this.currentTicks > this.nextTick && !isFinalPhase()) this.phaseUpdate();

        // ケアパッケージの生成
        switch (gamePhase) {
            case PHASE1 -> {
                if (this.currentTicks == 0 || this.currentTicks == 60 * 20 || this.currentTicks == 120 * 20) {
                    randomCarePackageSpawn(gameSetting.getCarePackageAmount(), true);
                }
            }
            case PHASE2, PHASE3 -> {
                if (this.currentTicks == 0 || this.currentTicks == 60 * 20) {
                    randomCarePackageSpawn(gameSetting.getCarePackageAmount(), true);
                }
            }
        }

        // 範囲外ケアパッケージ削除
        if (isShrinking()) {
            for (GutitubowEntity gutitubowEntity : gutitubow.getGutitubowEntityManager().getGutitubowEntities()) {
                if (gutitubowEntity instanceof CarePackage carePackage) {
                    if (!arena.getGameMap().getBorder().isInside(carePackage.getLocation())) carePackage.remove();
                }
            }
        }

        // ボスバーの表示更新
        this.bossBar.progress((float) (isFinalPhase() ? 0.0 : 1.0 - this.getProgress()));

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
                for (UUID uuid : alivePlayers) {
                    Player target = Bukkit.getPlayer(uuid);
                    if (target != null && uuid != player.getUniqueId() && !teamManager.isTeammate(player, target)) {
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
                arena.getGameMap().getBorder().setCenter(MathUtil.getLocationByProgress(arena.getGameMap().getCenter(), safeArea, getShrinkProgress()));
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
            arena.getGameMap().getBorder().setCenter(MathUtil.getLocationByProgress(oldSafeArea, safeArea, progress));
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

    private void randomCarePackageSpawn(int count, boolean showLog) {
        if (showLog) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(Component.empty());
                player.sendMessage(Component.text("[!] ケアパッケージが投下されました！").color(NamedTextColor.RED));
                player.sendMessage(Component.empty());
                NamespacedKey recipeKey = new NamespacedKey("minecraft", "chest");
                player.undiscoverRecipe(recipeKey);
                player.discoverRecipe(recipeKey);
            }
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
            arena.getGameMap().getBorder().changeSize(getNextSafeAreaSize(), borderChangeTicks);

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
                for (UUID uuid : players) {
                    Player target = Bukkit.getPlayer(uuid);
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

    public void addDeadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        alivePlayers.remove(uuid);
        if (deathOrder.contains(uuid)) return;
        deathOrder.remove(uuid);
        deathOrder.add(uuid);
    }

    public void addRevivePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        alivePlayers.add(uuid);
        deathOrder.remove(uuid);
    }

    public void checkGameOver() {
        if (debugManager.isDisableCheckGameOver()) return;
        if (alivePlayers.isEmpty()) {
            gameOver();
        } else {
            GutitubowGameMode gameMode = getGameSetting().getGameMode();
            if (gameMode == GutitubowGameMode.SOLO) {
                if (alivePlayers.size() == 1) gameOver(Bukkit.getPlayer(alivePlayers.iterator().next()));
            } else if (gameMode == GutitubowGameMode.DUO || gameMode == GutitubowGameMode.TRIO) {
                if (teamManager.getAliveTeams().size() == 1) {
                    GameTeam winTeam = teamManager.getAliveTeams().stream().toList().getFirst();
                    gameOver(winTeam);
                }
            }
        }
    }

    public void showBossBar(Player player) {
        player.showBossBar(bossBar);
    }

    public void hideBossBar(Player player) {
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

        Iterator<UUID> teamPlayers = team.getPlayers().iterator();
        while (teamPlayers.hasNext()) {
            UUID uuid = teamPlayers.next();
            Player winteamPlayer = Bukkit.getPlayer(uuid);
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

        for (UUID uuid : team.getPlayers()) {
            Player teamPlayer = Bukkit.getPlayer(uuid);
            boolean isLast = uuid == team.getPlayers().getLast();
            rankingComponent = rankingComponent.append(getRankingPlayerComponent(teamPlayer, 1, team.getTeamColor()));
            if (!isLast) rankingComponent = rankingComponent.append(Component.text("\n"));
        }

        int rank = 2;
        List<UUID> otherPlayers = new ArrayList<>(deathOrder.reversed());
        otherPlayers.removeAll(team.getPlayers());
        for (UUID uuid : otherPlayers) {
            Player player = Bukkit.getPlayer(uuid);
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
        deathOrder.add(winner.getUniqueId());

        Component rankingComponent = Component.text("");

        for (int i = 0; i < deathOrder.size(); i++) {
            Player player = Bukkit.getPlayer(deathOrder.reversed().get(i));
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


    public @NotNull MapRepairManager getMapRepairManager() {
        return this.mapRepairManager;
    }

    public @NotNull GlassBreakManager getGlassBreakManager() {
        return this.glassBreakManager;
    }

    public @NotNull AbilityManager getAbilityManager() {
        return this.abilityManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public SpectatorManager getSpectatorManager() {
        return spectatorManager;
    }

    public @NotNull DebugManager getDebugManager() {
        return debugManager;
    }

    public boolean isInsideNextSafeArea(Location loc) {

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

    public PlayerBoard getPlayerBoard(GamePlayer gamePlayer) {
        return playerBoardManager.getPlayerBoard(gamePlayer);
    }

    public void onDisablePlugin() {
        reset();
        abilityManager.allResetAbilities();
        teamManager.removeAllTeam();
        if (arena != null) gutitubowManagers.getArenaManager().destroy(arena);
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public void setGameMap(GameMap gameMap) {
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

    public @Nullable Arena getArena() {
        return arena;
    }

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

    public GameMapSelectorGUI getGameMapSelectorGUI() {
        return gameMapSelectorGUI;
    }

}
