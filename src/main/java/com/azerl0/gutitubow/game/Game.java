package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityManager;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.carepackage.CarePackage;
import com.azerl0.gutitubow.carepackage.CarePackageManager;
import com.azerl0.gutitubow.data.GutitubowDamageType;
import com.azerl0.gutitubow.data.item.GutitubowItem;
import com.azerl0.gutitubow.debug.DebugManager;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerDamageEvent;
import com.azerl0.gutitubow.game.scoreboard.PlayerBoard;
import com.azerl0.gutitubow.game.scoreboard.ScoreboardManager;
import com.azerl0.gutitubow.manager.GlassBreakManager;
import com.azerl0.gutitubow.manager.LogManager;
import com.azerl0.gutitubow.map.Map;
import com.azerl0.gutitubow.map.MapRepairManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Game {
    private final JavaPlugin plugin;
    private boolean started;
    private int nextTick;
    private int currentTicks;
    private int phase;
    private boolean isShrinking;
    private Set<UUID> players;
    private final Set<UUID> alivePlayers = new java.util.HashSet<>();
    private final List<UUID> deathOrder = new ArrayList<>();
    private final BossBar bossBar = BossBar.bossBar(Component.text(""), 0.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
    private final Map map;
    private Location safeArea;
    private Location oldSafeArea;
    private double nextSafeAreaMinX;
    private double nextSafeAreaMaxX;
    private double nextSafeAreaMinZ;
    private double nextSafeAreaMaxZ;
    private final CarePackageManager carePackageManager;
    private final GamePlayerManager gamePlayerManager;
    private final AbilityManager abilityManager;
    private final DebugManager debugManager;
    private final MapRepairManager mapRepairManager = new MapRepairManager();
    private final GlassBreakManager glassBreakManager = new GlassBreakManager(mapRepairManager);
    private final ScoreboardManager scoreboardManager;

    public Game(
            JavaPlugin plugin,
            Map map,
            CarePackageManager carePackageManager,
            DebugManager debugManager
    ) {
        this.plugin = plugin;
        this.map = map;
        this.carePackageManager = carePackageManager;
        AbilityContext abilityContext = new AbilityContext(plugin, this);
        this.abilityManager = new AbilityManager(abilityContext);
        this.debugManager = debugManager;
        this.gamePlayerManager = new GamePlayerManager(plugin, this);
        reset();
        this.scoreboardManager = new ScoreboardManager(this);
    }

    public GamePlayer getGamePlayer(Player player) {
        return gamePlayerManager.getGamePlayer(player);
    }

    public boolean isStarted() {
        return this.started;
    }

    public void start() {
        this.started = true;
        this.players.clear();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            this.players.add(player.getUniqueId());
        }
    }

    private enum phaseTicks {
        PHASE1(180),
        PHASE2(120),
        PHASE3(120),
        PHASE4(60),
        PHASE5(60);

        private final int seconds;

        phaseTicks(int seconds) {
            this.seconds = seconds;
        }

        private int getTicks() {
            return seconds * 20;
        }
    }

    public int getPhase() {
        return this.phase;
    }

    public boolean isShrinking() {
        return this.isShrinking;
    }

    public double getProgress() {
        return (double) this.currentTicks / (double) this.nextTick;
    }

    public Set<UUID> getPlayers() {
        return this.players;
    }

    public Set<UUID> getAlivePlayers() {
        return this.alivePlayers;
    }

    public void reset() {
        this.started = false;
        this.nextTick = 11 * 20;
        this.currentTicks = 0;
        this.phase = -1;
        this.isShrinking = false;
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
        }
        map.getBorder().setCenter(0.0, 0.0);
        map.getBorder().setSize(59999968);
        map.getBorder().setDamageBuffer(0.0);
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(20);
            player.setHealth(20.0);
            for (Player otherPlayer : Bukkit.getServer().getOnlinePlayers()) {
                if (player.getUniqueId() != otherPlayer.getUniqueId()) {
                    player.showPlayer(plugin, otherPlayer);
                }
            }
        }

        this.safeArea = map.getCenter();
        carePackageManager.removeAllCarePackage();

        for (World world : Bukkit.getWorlds()) {
            for (Arrow arrow : world.getEntitiesByClass(Arrow.class)) {
                arrow.remove();
            }
        }

        this.mapRepairManager.repairBrokeBlocks();
        this.mapRepairManager.repairPlacedBlocks();
        this.mapRepairManager.reset();
    }


    private void init() {
        this.nextTick = 15 * 20;
        this.map.getBorder().setCenter(this.map.getCenter());
        this.map.getBorder().setSize(this.map.getSize());
        safeArea = map.getRandomSafeArea();
        updateNextSafeAreaSize();
        deathOrder.clear();
        alivePlayers.clear();
        Random random = new Random();
        List<AbilityType> abilityTypeList = new ArrayList<>(Arrays.stream(AbilityType.values()).toList());

        for (UUID uuid : this.players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            alivePlayers.add(uuid);
            player.showBossBar(this.bossBar);
            Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(6);
            player.setHealth(6);
            if (!debugManager.isNoStartTeleport()) {
                Location location = this.map.getRandomXZPosition(30);
                location.setY(200.0);
                location.getDirection().setY(0);
                player.teleport(location);
            }

            GamePlayer gamePlayer = gamePlayerManager.getGamePlayer(player);

            gamePlayer.resetMaxArrowStackSize();
            gamePlayer.resetArrowReloadTick();
            gamePlayer.resetTotalHit();
            gamePlayer.setTotalKill(0);
            gamePlayer.updatePlayerBoard();

            int randomIndex = random.nextInt(0, abilityTypeList.size());
            AbilityType ability = abilityTypeList.get(randomIndex);

            abilityTypeList.remove(randomIndex);
            if (abilityTypeList.isEmpty())
                abilityTypeList = new ArrayList<>(Arrays.stream(AbilityType.values()).toList());

            abilityManager.setAbility(player, ability);

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

            PlayerInventory inventory = player.getInventory();
            inventory.clear();
            inventory.setItem(0, GutitubowItem.GUTITUBOW);
            inventory.setItem(6, GutitubowItem.AREA_COMPASS);
            inventory.setItem(7, GutitubowItem.SPYGLASS);
            abilityManager.setDisplayItem(player, ability, 13);

            player.setCompassTarget(safeArea);

            if (!debugManager.isNoStartTeleport()) gamePlayer.firstElytra();
            gamePlayer.setArrowAmount(0);

            for (int i = 9; i <= 26; i++) {
                if (i != 13) inventory.setItem(i, GutitubowItem.INVALID_SLOT_BLACK);
            }

            for (UUID otherPlayerUUID : players) {
                Player target = Bukkit.getPlayer(otherPlayerUUID);
                if (target != null && player.getUniqueId() != target.getUniqueId()) {
                    player.hidePlayer(plugin, target);
                }
            }
        }
    }

    public void tick() {
        this.update();
    }


    private void update() {
        this.currentTicks++;

        if (this.currentTicks > this.nextTick && this.phase != 7) this.phaseUpdate();

        switch (phase) {
            case (1):
                if (this.currentTicks == 0 || this.currentTicks == 60 * 20 || this.currentTicks == 120 * 20) {
                    this.randomCarePackageSpawn(4, true);
                }
                break;
            case (2):
            case (3):
                if (this.currentTicks == 0 || this.currentTicks == 60 * 20) {
                    this.randomCarePackageSpawn(4, true);
                }
                break;
        }

        this.bossBar.progress((float) (this.phase == 7 ? 0.0 : 1.0 - this.getProgress()));

        int remainedSeconds = (this.nextTick - this.currentTicks) / 20;
        String remainedText = this.phase == 0 ? "セーフエリア決定まで後 " : this.isShrinking ? "次のフェーズまで後 " : "縮小開始まで後 ";
        if (this.phase != 6 && this.phase != 7) {
            this.bossBar.name(Component.text("現在のフェーズ " + getPhase() + ": " + remainedText + remainedSeconds + "秒"));

        }

        Component lastPhaseRemainedText = null;
        if (this.currentTicks % 20 == 0 && this.phase == 6) {
            lastPhaseRemainedText =
                    Component.text("あと").color(NamedTextColor.GOLD)
                            .append(Component.text(remainedSeconds).color(NamedTextColor.RED)
                                    .append(Component.text("秒で").color(NamedTextColor.GOLD)
                                            .append(Component.text("最終フェーズ").color(NamedTextColor.RED)
                                                    .append(Component.text("が始まります").color(NamedTextColor.GOLD)
                                                    ))));
            this.bossBar.name(lastPhaseRemainedText);
        }


        // プレイヤーへの処理
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {

            if (remainedSeconds <= 10 && this.currentTicks % 20 == 1 && this.phase == -1) {
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

            if (phase == 0 && (currentTicks + 10) % 20 == 0) {
                for (UUID uuid : alivePlayers) {
                    Player target = Bukkit.getPlayer(uuid);
                    if (target != null && uuid != player.getUniqueId()) {
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

            if (phase > 0 && phase < 6 && isShrinking) {
                setWorldBorderToSafeAreaByProgress(map.getCenter(), getShrinkProgress());
            }

            if (phase == 7) {
                int lastPhaseCycleTick = 15 * 20;
                int lastPhaseCycleCurrentTick = currentTicks % lastPhaseCycleTick;
                if (lastPhaseCycleCurrentTick == 0) {
                    oldSafeArea = safeArea;
                    Location newSafeArea = map.getRandomSafeArea();

                    double maxDistance = 15.0;

                    Vector diff = newSafeArea.toVector().subtract(safeArea.toVector());
                    double distance = diff.length();

                    if (distance > maxDistance) {
                        diff.normalize().multiply(maxDistance);
                        newSafeArea = safeArea.clone().add(diff);
                    }

                    safeArea = newSafeArea;
                } else {
                    setWorldBorderToSafeAreaByProgress(oldSafeArea, (float) lastPhaseCycleCurrentTick / (float) lastPhaseCycleTick);
                }
            }

            if (remainedSeconds <= 10 && this.currentTicks % 20 == 0 && this.phase != 7) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }

            if (this.phase == 6 && this.currentTicks % 20 == 0 && lastPhaseRemainedText != null) {
                showSubtitle(player, lastPhaseRemainedText);
            }

        }

    }

    private float getShrinkProgress() {
        if (phase > 0 && phase < 6) {
            float phaseTick = (30 * 20 + 20);
            float base = phaseTick * 5;
            float current = phaseTick * (phase - 1) + currentTicks;
            return current / base;
        }
        return 0.0f;
    }

    private void setWorldBorderToSafeAreaByProgress(Location start, float progress) {
        WorldBorder border = map.getBorder();
        if (progress >= 0.0f && progress <= 1.0f) {
            Vector offset = safeArea.toVector().subtract(start.toVector()).multiply(progress);
            border.setCenter(start.clone().add(offset));
        } else {
            border.setCenter(start);
        }
    }

    private void randomCarePackageSpawn(int count, boolean showLog) {
        if (showLog) {
            for (World world : Bukkit.getWorlds()) {
                for (Player player : world.getEntitiesByClass(Player.class)) {
                    player.sendMessage(Component.empty());
                    player.sendMessage(Component.text("[!] ケアパッケージが投下されました！").color(NamedTextColor.RED));
                    player.sendMessage(Component.empty());
                }
            }
        }
        for (int i = 0; i < count; i++) {
            Location location = this.map.getRandomXZPosition(50);
            location.setY(175);
            carePackageManager.summonPackage(location);
        }
    }

    private void phaseUpdate() {
        if (this.isShrinking || this.phase == -1 || this.phase == 0 || this.phase == 6) {
            this.phase += 1;
            updateNextSafeAreaSize();

            if (this.phase == 0) init();
            this.bossBar.color(BossBar.Color.RED);
            this.isShrinking = false;

            if (this.phase > 0 && this.phase <= 5) {
                this.bossBar.color(BossBar.Color.BLUE);
                if (debugManager.isQuickPhase()) {
                    this.nextTick = 15 * 20;
                } else this.nextTick = phaseTicks.valueOf("PHASE" + this.phase).getTicks();

            } else if (this.phase == 6) {
                this.nextTick = 15 * 20;
            }
        } else {
            this.bossBar.color(BossBar.Color.RED);
            this.isShrinking = true;
            this.nextTick = 30 * 20;
            int borderChangeTicks = 31 * 20;
            switch (this.phase) {
                case (1):
                    map.getBorder().changeSize(150, borderChangeTicks);
                    break;
                case (2):
                    map.getBorder().changeSize(105, borderChangeTicks);
                    break;
                case (3):
                    map.getBorder().changeSize(63, borderChangeTicks);
                    break;
                case (4):
                    map.getBorder().changeSize(31, borderChangeTicks);
                    break;
                case (5):
                    map.getBorder().changeSize(10, borderChangeTicks);
                    break;
            }
        }

        if (this.phase == 7) {
            this.bossBar.name(Component.text("最終フェーズ進行中"));
        }

        // Player への処理
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.75f, 1.0f);
            if (this.phase == 7) {
                showSubtitle(player,
                        Component.text("最終フェーズ").color(NamedTextColor.RED)
                                .append(Component.text("が始まりました").color(NamedTextColor.GOLD))
                );
            }
            if (this.phase == 1 && !isShrinking) {
                for (UUID uuid : players) {
                    Player target = Bukkit.getPlayer(uuid);
                    if (target != null) {
                        player.showPlayer(plugin, target);
                    }
                }
            }

            if (phase >= 1 && phase <= 5 && isShrinking) {
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

            if (phase >= 1 && phase <= 5 && !isShrinking) {
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

        for (CarePackage carePackage : carePackageManager.getCarePackages()) {
            Entity coreEntity = carePackage.getCoreEntity();
            if (coreEntity != null) {
                if (!map.getBorder().isInside(coreEntity.getLocation())) {
                    carePackage.remove();
                }
            }
        }

        this.currentTicks = 0;
        this.nextTick += 20;
    }

    private void updateNextSafeAreaSize() {
        double size = switch (phase) {
            case 0, 1 -> 150;
            case 2 -> 105;
            case 3 -> 63;
            case 4 -> 31;
            case 5, 6, 7 -> 10;
            default -> 300;
        };

        double centerX = safeArea.getX();
        double centerZ = safeArea.getZ();
        double half = size / 2;
        nextSafeAreaMaxX = centerX + half;
        nextSafeAreaMinX = centerX - half;
        nextSafeAreaMaxZ = centerZ + half;
        nextSafeAreaMinZ = centerZ - half;
    }

    private void checkGameOver() {
        if (alivePlayers.isEmpty()) {
            gameOver();
        } else if (alivePlayers.size() == 1) {
            gameOver(Bukkit.getPlayer(alivePlayers.iterator().next()));
        }
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

        deathOrder.add(winner.getUniqueId());

        Component rankingComponent = Component.text("");

        for (int i = 0; i < deathOrder.size(); i++) {
            Player player = Bukkit.getPlayer(deathOrder.reversed().get(i));
            boolean isLast = i == deathOrder.size() - 1;

            if (player != null) {
                gamePlayer = getGamePlayer(player);
                Component abilityType = gamePlayer.getAbility().getType().getName();
                Component playerRank = Component.text((i + 1) + "st. " + player.getName() + " [").append(abilityType).append(Component.text("]"));
                Component playerRankingComponent = playerRank.append(Component.text(
                        " - " + gamePlayer.getTotalKill() + "Kill, " + gamePlayer.getTotalHit() + "Hit"
                ));
                rankingComponent = rankingComponent.append(playerRankingComponent
                        .color(i == 0 ? NamedTextColor.YELLOW : NamedTextColor.RED));

                if (!isLast) rankingComponent = rankingComponent.append(Component.text("\n"));

            }
        }

        Bukkit.getServer().broadcast(Component.text("\n§lGame Set!\n").color(NamedTextColor.LIGHT_PURPLE));
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.showTitle(title);
            player.sendMessage(rankingComponent);
        }
        reset();

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

    public void damage(Player player, double amount, Player attacker, double distance, boolean canUpgrade) {

        GamePlayerDamageEvent event = new GamePlayerDamageEvent(player, amount, attacker, GutitubowDamageType.ARROW_SHOT);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        if (player.getHealth() <= amount) {
            this.kill(player, attacker, distance);

        } else {
            player.damage(amount);
            if (canUpgrade) {
                GamePlayer gamePlayer = getGamePlayer(attacker);
                gamePlayer.setTotalHit(gamePlayer.getTotalHit() + 1);
                if (gamePlayer.getTotalHit() % 2 == 0) {
                    Random random = new Random();
                    if (random.nextBoolean()) {
                        if (!gamePlayer.upgradeArrowReloadTick()) {
                            gamePlayer.upgradeMaxArrowStackSize();
                        }
                    } else {
                        if (!gamePlayer.upgradeMaxArrowStackSize()) {
                            gamePlayer.upgradeArrowReloadTick();
                        }
                    }
                }
            }
            LogManager.broadcastHitLog(attacker, player, distance);
        }
    }

    public void damage(Player player, double amount, GutitubowDamageType damageType) {

        GamePlayerDamageEvent event = new GamePlayerDamageEvent(player, amount, damageType);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        if (player.getHealth() <= amount) {
            this.death(player, damageType, false);

        } else {
            player.damage(amount);
        }
    }

    public void damage(Player player, double amount, Player attacker, boolean canUpgrade) {
        double distance = player.getLocation().distance(attacker.getLocation());
        damage(player, amount, attacker, distance, canUpgrade);

    }

    public void kill(Player player, Player attacker, double distance) {
        GamePlayer gamePlayer = getGamePlayer(attacker);
        gamePlayer.setTotalKill(gamePlayer.getTotalKill() + 1);
        LogManager.broadcastKillLog(attacker, player, distance);
        this.death(player, GutitubowDamageType.ARROW_SHOT, true);
    }

    public void death(Player player, GutitubowDamageType damageType, boolean hideLog) {
        World world = player.getWorld();
        Location location = player.getLocation();
        world.spawnParticle(Particle.EXPLOSION_EMITTER, location, 3);
        world.strikeLightningEffect(location);
        Title title = Title.title(
                Component.text("You Died!").color(NamedTextColor.RED),
                Component.empty(),
                Title.Times.times(
                        java.time.Duration.ofMillis(500),
                        java.time.Duration.ofSeconds(3),
                        java.time.Duration.ofMillis(500)
                )
        );
        player.showTitle(title);

        UUID uuid = player.getUniqueId();
        if (isStarted()) {
            player.setGameMode(GameMode.SPECTATOR);
            alivePlayers.remove(uuid);
            deathOrder.remove(uuid);
            deathOrder.add(uuid);

        } else {
            double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getBaseValue();
            player.setHealth(maxHealth);
        }
        if (!hideLog) {
            if (damageType == GutitubowDamageType.OUTSIDE_BORDER) {
                LogManager.broadcastDeathLog(player);
            }
        }
        if (isStarted()) checkGameOver();
    }

    public void death(Player player, GutitubowDamageType damageType) {
        this.death(player, damageType, false);
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

    public boolean isInsideNextSafeArea(Location loc) {

        if (!loc.getWorld().equals(safeArea.getWorld())) return false;

        double x = loc.getX();
        double z = loc.getZ();
        return x <= nextSafeAreaMaxX && x >= nextSafeAreaMinX && z <= nextSafeAreaMaxZ && z >= nextSafeAreaMinZ;
    }


    public PlayerBoard getPlayerBoard(GamePlayer gamePlayer) {
        return scoreboardManager.getPlayerBoard(gamePlayer);
    }

}
