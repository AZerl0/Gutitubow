package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerPreDamageEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerKillEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TrackingAbility extends Ability {
    private final Set<UUID> trackingPlayers = new HashSet<>();
    private BukkitTask trackingTask;

    public TrackingAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.TRACKING;
    }

    @Override
    public void onReset() {
        if (trackingTask != null) trackingTask.cancel();
        trackingTask = null;

        Player player = getPlayer();
        if (player == null) return;

        for (UUID uuid : new HashSet<>(trackingPlayers)) {
            Player trackingPlayer = Bukkit.getPlayer(uuid);
            if (trackingPlayer == null) continue;
            removeTrackingPlayer(trackingPlayer);
        }
        trackingPlayers.clear();
    }

    @Override
    public void onDealDamage(GamePlayerPreDamageEvent event) {
        Player hitPlayer = event.getPlayer();
        if (!isTracking(hitPlayer)) {
            newTrackingPlayer(hitPlayer);
            event.setHitMessage(Component.text("§c追跡開始 §6≫ §c" + hitPlayer.getName()));
            hitPlayer.sendMessage(Component.text("§c[!] §4" + event.getDamageSource().getDirectPlayer().getName() + "に追跡されている..."));
        }
    }

    @Override
    public void onKill(GamePlayerKillEvent event) {
        if (isTracking(event.getTarget())) {
            event.setKillMessage(Component.text("§6≫ §c追跡対象を排除した。"));
            removeTrackingPlayer(event.getTarget());
        }
    }

    public void onTracking() {
        Player player = getPlayer();
        if (player == null) return;

        Double distance = getNeraTrackingPlayerDistance();
        if (distance != null && distance <= 30) {
            PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 5, 1, true, true, true);
            player.addPotionEffect(speed);
        }

        for (UUID uuid : trackingPlayers) {
            Player trackingPlayer = Bukkit.getPlayer(uuid);
            if (trackingPlayer == null || !trackingPlayer.isOnline()) continue;
            context.glowManager().addGlow(player, trackingPlayer);
        }
    }

    private boolean isTracking(Player player) {
        return trackingPlayers.contains(player.getUniqueId());
    }

    private void newTrackingPlayer(Player trackingPlayer) {
        Player player = getPlayer();
        if (player == null) return;
        trackingPlayers.add(trackingPlayer.getUniqueId());
        context.glowManager().setGlowColor(player, trackingPlayer, NamedTextColor.RED);
        context.glowManager().addGlow(player, trackingPlayer);

        if (trackingTask != null && !trackingTask.isCancelled()) return;

        trackingTask = new BukkitRunnable() {
            @Override
            public void run() {
                onTracking();
            }
        }.runTaskTimer(context.gutitubow(), 0L, 1L);
    }

    private void removeTrackingPlayer(@NotNull Player trackingPlayer) {
        Player player = getPlayer();
        if (player == null) return;
        if (!trackingPlayers.contains(trackingPlayer.getUniqueId())) return;

        trackingPlayers.remove(trackingPlayer.getUniqueId());
        context.glowManager().setGlowColor(player, trackingPlayer, NamedTextColor.WHITE);
        context.glowManager().removeGlow(player, trackingPlayer);
    }


    private @Nullable Double getNeraTrackingPlayerDistance() {
        Player player = getPlayer();
        if (player == null) return null;

        Location playerLocation = player.getLocation();

        Double distance = null;
        for (UUID uuid : trackingPlayers) {
            Player trackingPlayer = Bukkit.getPlayer(uuid);
            if (trackingPlayer == null || !trackingPlayer.isOnline()) continue;
            double playerDistance = playerLocation.distance(trackingPlayer.getLocation());
            if (distance == null || playerDistance < distance) distance = playerDistance;
        }
        return distance;
    }
}
