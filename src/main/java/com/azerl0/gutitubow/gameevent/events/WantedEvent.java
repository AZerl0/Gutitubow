package com.azerl0.gutitubow.gameevent.events;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.data.item.GutitubowItem;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerDeathEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerKillEvent;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.gameevent.GameEvent;
import com.azerl0.gutitubow.gameevent.GameEventType;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class WantedEvent extends GameEvent {
    private @Nullable GamePlayer wantedPlayer;

    @Override
    public @NotNull GameEventType getEventType() {
        return GameEventType.WANTED;
    }

    @Override
    protected void onActivate() {
        wantedPlayer = getWantedPlayer();
        Component message;
        if (wantedPlayer == null) {
            message = Component.text("プレイヤーがいない…");
        } else {
            message = wantedPlayer.getPlayer().displayName().append(Component.text("が賞金首だ！"));
            PotionEffect effect = new PotionEffect(PotionEffectType.GLOWING, -1, 2, true, false, false);
            wantedPlayer.getPlayer().addPotionEffect(effect);
        }
        Bukkit.getScheduler().runTaskLater(Gutitubow.get(), () -> {
            Title title = Title.title(
                    Component.empty(),
                    message,
                    Title.Times.times(
                            java.time.Duration.ofMillis(100),
                            java.time.Duration.ofSeconds(1),
                            java.time.Duration.ofMillis(500)
                    )
            );
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.showTitle(title);
            }
        }, 60L);
    }

    @Override
    protected void onDeactivated() {
        resetWantedPlayer();
    }


    @Override
    protected void onAnyPlayerKill(GamePlayerKillEvent event) {
        if (isWantedPlayer(event.getTarget())) {
            giveReward(event.getPlayer());
            showPlayerDeadMessage();
            resetWantedPlayer();
        }
    }

    @Override
    protected void onAnyPlayerDeath(GamePlayerDeathEvent event) {
        if (isWantedPlayer(event.getPlayer())) {
            showPlayerDeadMessage();
            resetWantedPlayer();
        }
    }

    private void giveReward(Player player) {
        for (int i = 0; i < 5; i++) player.give(GutitubowItem.RECOVERY_POTION);
    }

    private boolean isWantedPlayer(Player player) {
        return wantedPlayer != null && wantedPlayer.getPlayerUUID() == player.getUniqueId();
    }

    private void resetWantedPlayer() {
        if (wantedPlayer != null) {
            wantedPlayer.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
            wantedPlayer = null;
        }
    }

    private void showPlayerDeadMessage() {
        if (wantedPlayer == null) return;

        Title title = Title.title(
                Component.empty(),
                wantedPlayer.getPlayer().displayName().append(Component.text("は倒された！")),
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofSeconds(1),
                        java.time.Duration.ofMillis(500)
                )
        );
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(title);
        }
    }

    private @Nullable GamePlayer getWantedPlayer() {
        Game game = Gutitubow.get().getGame();
        Set<GamePlayer> players = new HashSet<>();
        if (game.isStarted()) {
            game.getCurrentGamePlayers().forEach(gamePlayer -> {
                if (!gamePlayer.isDead()) players.add(gamePlayer);
            });
        } else {
            game.getAllGamePlayers().forEach(gamePlayer -> {
                Player player = gamePlayer.getPlayer();
                if (player.isOnline() && player.getGameMode() != GameMode.SPECTATOR) {
                    players.add(gamePlayer);
                }
            });
        }

        int topPoint = 0;
        GamePlayer topGamePlayer = null;
        for (GamePlayer gamePlayer : players) {

            int point = gamePlayer.getTotalKill() * 10 + gamePlayer.getTotalHit();
            if (point >= topPoint) {
                if (point == topPoint) {
                    Random random = new Random();
                    if (random.nextBoolean()) topGamePlayer = gamePlayer;

                } else {
                    topGamePlayer = gamePlayer;
                }
            }
        }

        return topGamePlayer;
    }

}
