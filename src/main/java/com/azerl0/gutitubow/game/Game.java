package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.ability.AbilityManager;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.carepackage.CarePackageManager;
import com.azerl0.gutitubow.data.item.GutitubowItem;
import com.azerl0.gutitubow.debug.DebugManager;
import com.azerl0.gutitubow.manager.GamePlayerManager;
import com.azerl0.gutitubow.map.Map;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.CompassMeta;

import java.util.*;

public class Game {
    private boolean started;
    private int nextTick;
    private int currentTicks;
    private int phase;
    private boolean isShrinking;
    private Set<Player> players;
    private final BossBar bossBar = BossBar.bossBar(Component.text(""), 0.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
    private final Map map;
    private final CarePackageManager carePackageManager;
    private final GamePlayerManager gamePlayerManager;
    private final AbilityManager abilityManager;
    private final DebugManager debugManager;

    public Game(
            Map map,
            CarePackageManager carePackageManager,
            GamePlayerManager gamePlayerManager,
            AbilityManager abilityManager,
            DebugManager debugManager
    ) {
        this.map = map;
        this.carePackageManager = carePackageManager;
        this.gamePlayerManager = gamePlayerManager;
        this.abilityManager = abilityManager;
        this.debugManager = debugManager;
        reset();
    }

    public boolean isStarted() {
        return this.started;
    }

    public void start() {
        this.started = true;
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

    public Set<Player> getPlayers() {
        return this.players;
    }

    public void reset() {
        this.started = false;
        this.nextTick = 11 * 20;
        this.currentTicks = 0;
        this.phase = -1;
        this.isShrinking = false;
        this.players = new java.util.HashSet<>();
        this.bossBar.progress(0.0f);
        this.bossBar.color(BossBar.Color.BLUE);
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.hideBossBar(this.bossBar);
        }
        this.map.getBorder().setSize(59999968);
        this.map.getBorder().setDamageBuffer(0.0);
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(20);
            player.setHealth(20.0);
        }
        carePackageManager.removeAllCarePackage();

        for (World world : Bukkit.getWorlds()) {
            for (Arrow arrow : world.getEntitiesByClass(Arrow.class)) {
                arrow.remove();
            }
        }
    }

    public void tick() {
        this.update();
    }


    private void update() {
        this.currentTicks++;

        if (this.currentTicks > this.nextTick && this.phase != 7) phaseUpdate();

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

        this.bossBar.progress((float) (this.phase == 7 ? 0.0 : 1.0 - getProgress()));

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

            if (remainedSeconds <= 10 && this.currentTicks % 20 == 0 && this.phase != 7) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }

            if (this.phase == 6 && this.currentTicks % 20 == 0 && lastPhaseRemainedText != null) {
                showSubtitle(player, lastPhaseRemainedText);
            }

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

    private void init() {
        this.nextTick = 15 * 20;
        this.map.getBorder().setCenter(this.map.getCenter());
        this.map.getBorder().setSize(this.map.getSize());
        Random random = new Random();
        List<AbilityType> abilityTypeList = new ArrayList<>(Arrays.stream(AbilityType.values()).toList());

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.showBossBar(this.bossBar);
            Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(6);
            player.setHealth(6);
            Location location = this.map.getRandomXZPosition(30);
            location.setY(200.0);
            location.getDirection().setY(0);
            player.teleport(location);

            GamePlayerManager.GamePlayer gamePlayer = gamePlayerManager.getGamePlayer(player);

            gamePlayer.resetMaxArrowStackSize();
            gamePlayer.resetArrowReloadTick();
            gamePlayer.resetTotalHit();

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
            inventory.setItem(7, ItemStack.of(Material.SPYGLASS));

            ItemStack compass = ItemStack.of(Material.COMPASS);
            CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();
            compassMeta.setLodestone(this.map.getCenter());
            compass.setItemMeta(compassMeta);
            inventory.setItem(6, compass);

            gamePlayer.firstElytra();
            gamePlayer.setArrowAmount(0);
        }
    }

    private void phaseUpdate() {
        if (this.isShrinking || this.phase == -1 || this.phase == 0 || this.phase == 6) {
            this.phase += 1;

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
        }

        this.currentTicks = 0;
        this.nextTick += 20;
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

    public void handlePlayerDeath() {

    }

}
