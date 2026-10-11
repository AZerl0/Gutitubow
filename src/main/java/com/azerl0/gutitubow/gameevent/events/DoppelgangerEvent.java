package com.azerl0.gutitubow.gameevent.events;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.api.game.Game;
import com.azerl0.gutitubow.damage.DamageSource;
import com.azerl0.gutitubow.damage.GutitubowDamageType;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerKillEvent;
import com.azerl0.gutitubow.game.GutitubowGameMode;
import com.azerl0.gutitubow.gameevent.GameEvent;
import com.azerl0.gutitubow.gameevent.GameEventContext;
import com.azerl0.gutitubow.gameevent.GameEventType;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.profiler.Profile;
import com.azerl0.gutitubow.profiler.ProfileChangeReason;
import com.azerl0.gutitubow.profiler.SkinData;
import com.azerl0.gutitubow.team.GameTeam;
import com.azerl0.gutitubow.util.MathUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DoppelgangerEvent extends GameEvent {
    private final Map<GamePlayer, Doppelganger> doppelgangers = new HashMap<>();

    public DoppelgangerEvent(GameEventContext context) {
        super(context);
    }

    @Override
    public @NotNull GameEventType getEventType() {
        return GameEventType.DOPPELGANGER;
    }

    @Override
    protected void onActivate() {
        doppelgangers.clear();
        List<GamePlayer> targets = new ArrayList<>();

        for (GamePlayer deadPlayer : context.game().getDeadPlayers()) {
            Player player = deadPlayer.getPlayer();
            if (player == null) continue;

            if (targets.isEmpty()) {
                targets.addAll(context.game().getAlivePlayers());
                Collections.shuffle(targets);
            }
            List<GamePlayer> playerTargets = new ArrayList<>(targets);
            GutitubowGameMode gameMode = context.game().getGameSetting().getGameMode();
            if (gameMode == GutitubowGameMode.DUO || gameMode == GutitubowGameMode.TRIO) {
                GameTeam team = deadPlayer.getTeam();
                if (team != null) {
                    playerTargets.removeAll(team.getPlayers());
                }
            }
            GamePlayer targetGamePlayer;
            if (playerTargets.isEmpty()) {
                targetGamePlayer = deadPlayer;
            } else {
                targetGamePlayer = playerTargets.getFirst();
            }
            Player target = targetGamePlayer.getPlayer();
            if (target == null) continue;

            Doppelganger doppelganger = new Doppelganger(context.gutitubow(), context.game(), deadPlayer, targetGamePlayer);
            doppelganger.spawn();
            context.gutitubow().getLogger().info("spawnDG: " + deadPlayer.getPlayer().getName() + ", target: " + doppelganger.target.getPlayer().getName());
            doppelgangers.put(deadPlayer, doppelganger);
        }
    }

    @Override
    protected void onDeactivated() {
        for (Doppelganger doppelganger : doppelgangers.values()) {
            if (doppelganger.isDead()) continue;

            if (!doppelganger.targetIsDead()) {
                DamageSource damageSource = DamageSource.builder(GutitubowDamageType.DOPPELGANGER_DESTROY).build();
                context.game().kill(doppelganger.gamePlayer, damageSource);
                context.game().kill(doppelganger.target, damageSource);
            }
        }
        doppelgangers.clear();
    }

    @Override
    protected void onAnyPlayerKill(GamePlayerKillEvent event) {
        Doppelganger doppelganger = doppelgangers.get(event.getGamePlayer());
        if (doppelganger == null) return;
        if (!doppelganger.isTarget(event.getTargetGamePlayer())) return;

        // kill origin
        context.gutitubow().getLogger().info("killTarget: " + doppelganger.target.getPlayer().getName());
    }

    private static class Doppelganger {
        private final Gutitubow gutitubow;
        private final Game game;
        private final GamePlayer gamePlayer;
        private final GamePlayer target;

        private Doppelganger(Gutitubow gutitubow, Game game, GamePlayer gamePlayer, GamePlayer target) {
            this.gutitubow = gutitubow;
            this.game = game;
            this.gamePlayer = gamePlayer;
            this.target = target;
        }

        private boolean isTarget(GamePlayer player) {
            return player.getPlayerUUID().equals(target.getPlayerUUID());
        }

        private boolean isDead() {
            return gamePlayer.isDead();
        }

        private boolean targetIsDead() {
            return target.isDead();
        }

        private void spawn() {
            Player player = gamePlayer.getPlayer();
            if (player == null) return;

            Location location = MathUtil.randomLocation(game.getSafeArea(), game.getNextSafeAreaSize() - 10);
            location.setY(175);
            location.getDirection().setY(0);
            player.teleport(location);

            player.setGameMode(GameMode.ADVENTURE);
            PotionEffect health = new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 255, true, false, false);
            player.addPotionEffect(health);

            Player targetPlayer = target.getPlayer();
            if (targetPlayer != null) {
                Profile profile = gutitubow.getGutitubowManagers().getProfileManager().getProfile(player);
                SkinData targetSkinData = gutitubow.getGutitubowManagers().getSkinDataManager().getPlayerSkinData(targetPlayer);
                if (targetSkinData != null) profile.setSkin(targetSkinData, ProfileChangeReason.DOPPELGANGER);
                profile.setName(targetPlayer.displayName(), ProfileChangeReason.DOPPELGANGER);

            }
            player.playSound(player, Sound.BLOCK_END_PORTAL_SPAWN, 0.7f, 1.0f);
            game.reviveSilent(gamePlayer);

            Bukkit.getScheduler().runTaskLater(Gutitubow.get(), () -> {
                Component message = Component.text("このフェーズ中にオリジナルを始末せよ！");
                Title title = Title.title(
                        Component.empty(),
                        message,
                        Title.Times.times(
                                java.time.Duration.ofMillis(100),
                                java.time.Duration.ofSeconds(2),
                                java.time.Duration.ofMillis(500)
                        )
                );
                player.showTitle(title);
            }, 100L);
            Component message = Component.text("\n")
                    .append(Component.text("君は今「").append(targetPlayer.displayName()).append(Component.text("」の姿で現世に復活した！\n")))
                    .append(Component.text("フェーズ終了時までに").append(targetPlayer.displayName()).append(Component.text("を倒し、成り代わるのだ！\n")));
            player.sendMessage(message);

        }
    }
}
