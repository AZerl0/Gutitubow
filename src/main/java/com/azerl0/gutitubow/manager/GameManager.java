package com.azerl0.gutitubow.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;

public class GameManager {

    private final JavaPlugin plugin;

    public GameManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Game newGame() {
        return new Game();
    }

    public class Game {
        private boolean started;
        private int nextTick;
        private int currentTicks;
        private int phase;
        private boolean isShrinking;
        private Set<Player> players;
        private final int gamePhase0Ticks;
        private BukkitTask task = null;
        private final BossBar bossBar = BossBar.bossBar(Component.text(""), 0.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);

        private Game() {
            this.gamePhase0Ticks = 10 * 20;
            reset();
        }

        public boolean isStarted() {
            return this.started;
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

        }

        public void start() {
            this.started = true;
            Bukkit.getServer().broadcast(Component.text("game start"));

            if (task != null && !task.isCancelled()) return;

            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!started) {
                        this.cancel();
                        return;
                    }
                    update();
                }
            }.runTaskTimer(plugin, 0L, 1L);

        }

        private void update() {
            this.currentTicks += 1;

            if (this.currentTicks > this.nextTick && this.phase != 7) phaseUpdate();

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

                if (this.phase == 7 && this.currentTicks == 0) {
                    showSubtitle(player,
                            Component.text("最終フェーズ").color(NamedTextColor.RED)
                                    .append(Component.text("が始まりました").color(NamedTextColor.GOLD))
                    );
                }

                if (this.currentTicks == 0 && this.phase != 0) {
                    player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.75f, 1.0f);
                }
            }

        }

        private void init() {
            this.nextTick = 15 * 20;
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                player.showBossBar(this.bossBar);
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
                    this.nextTick = phaseTicks.valueOf("PHASE" + this.phase).getTicks();
                } else if (this.phase == 6) {
                    this.nextTick = 15 * 20;
                }
            } else {
                this.bossBar.color(BossBar.Color.RED);
                this.isShrinking = true;
                this.nextTick = 30 * 20;
            }

            if (this.phase == 7) {
                this.bossBar.name(Component.text("最終フェーズ進行中"));
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


    }
}
