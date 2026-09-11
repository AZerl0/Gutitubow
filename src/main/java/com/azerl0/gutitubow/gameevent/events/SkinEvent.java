package com.azerl0.gutitubow.gameevent.events;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.gameevent.GameEvent;
import com.azerl0.gutitubow.gameevent.GameEventType;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

public class SkinEvent extends GameEvent {
    @Override
    public @NotNull GameEventType getEventType() {
        return GameEventType.SKIN;
    }

    @Override
    protected boolean overridePlayerEffect() {
        return true;
    }

    @Override
    protected void onActivate() {
        Player skinPlayer = getSkinPlayer();
        if (skinPlayer == null) return;
        playersConsumer(player -> {
            Gutitubow.get().getPlayerSkinChanger().setSkin(player, skinPlayer.getName());
        });

        playActivateEffect(
                Component.text("全てのプレイヤーがイケメンになってしまった！"),
                Component.text("かわいいお顔"),
                List.of(
                        Component.text("全員のスキンが").append(skinPlayer.name()).append(Component.text("になってしまった！"))
                )
        );
    }

    @Override
    protected void onDeactivated() {
        playersConsumer(player -> {
            Gutitubow.get().getPlayerSkinChanger().resetSkin(player);
        });
    }

    private @Nullable Player getSkinPlayer() {
        Game game = Gutitubow.get().getGame();
        Set<GamePlayer> players = new HashSet<>();
        if (game.isStarted()) {
            game.getCurrentGamePlayers().forEach(gamePlayer -> {
                if (!gamePlayer.isDead()) players.add(gamePlayer);
            });
        } else {
            game.getAllGamePlayers().forEach(gamePlayer -> {
                Player player = gamePlayer.getPlayer();
                if (player != null && player.getGameMode() != GameMode.SPECTATOR) {
                    players.add(gamePlayer);
                }
            });
        }

        List<GamePlayer> playerList = players.stream().toList();
        if (playerList.isEmpty()) return null;
        GamePlayer skinPlayer = playerList.get(new Random().nextInt(0, playerList.size()));
        return skinPlayer.getPlayer();
    }

    private void playersConsumer(Consumer<Player> player) {
        Game game = Gutitubow.get().getGame();
        if (game.isStarted()) {
            game.getCurrentGamePlayers().forEach(gamePlayer -> {
                Player p = gamePlayer.getPlayer();
                if (p != null) player.accept(p);
            });
        } else {
            game.getAllGamePlayers().forEach(gamePlayer -> {
                Player p = gamePlayer.getPlayer();
                if (p != null) player.accept(p);
            });
        }
    }
}
