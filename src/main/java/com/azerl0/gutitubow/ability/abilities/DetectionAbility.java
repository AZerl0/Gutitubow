package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerSneakingEvent;
import com.azerl0.gutitubow.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DetectionAbility extends Ability {
    private int detectionTick = 0;
    private final int DETECTION_TICK = 2 * 20;
    private final Set<Player> detectingPlayers = new HashSet<>();
    private Set<Player> detectedPlayers = new HashSet<>();

    public DetectionAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.DETECTION;
    }

    @Override
    public void reset() {
        super.reset();
        resetGlowing();
    }

    @Override
    public void onArrowHit(ProjectileHitEvent event) {
        Player player = getPlayer();
        if (player == null) return;

        playAsteriskEffect(event.getEntity().getLocation());
        for (Player nearPlayer : event.getEntity().getWorld().getNearbyPlayers(event.getEntity().getLocation(), 3)) {
            if (!context.game().isTeammate(player, nearPlayer)) {
                PotionEffect glowing = new PotionEffect(PotionEffectType.GLOWING, 5 * 20, 0);
                nearPlayer.addPotionEffect(glowing);
            }
        }
    }

    @Override
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) {
            detectionTick = 0;
            resetGlowing();
            Player player = getPlayer();
            if (player == null) return;
            player.removePotionEffect(PotionEffectType.DARKNESS);
        }
    }

    @Override
    public void onSneaking(GamePlayerSneakingEvent event) {
        detectionTick++;
        Player player = getPlayer();
        if (player == null) return;

        if (detectionTick >= DETECTION_TICK) {
            if (detectionTick == DETECTION_TICK) {
                player.playSound(player, Sound.ENTITY_WITHER_SHOOT, 0.3f, 1.0f);
            }
            updateDetecting(player);
        } else {
            if (detectionTick == 15) {
                PotionEffect darkness = new PotionEffect(PotionEffectType.DARKNESS, -1, 0, true, false, true);
                player.addPotionEffect(darkness);
            }
            Component subtitle = ComponentUtil.getGaugeComponent(
                    Component.text("探知準備中... ").color(NamedTextColor.GREEN),
                    detectionTick,
                    DETECTION_TICK,
                    NamedTextColor.GREEN,
                    NamedTextColor.GRAY
            );
            Title title = Title.title(
                    Component.empty(),
                    subtitle,
                    Title.Times.times(
                            java.time.Duration.ofMillis(0),
                            java.time.Duration.ofSeconds(1),
                            java.time.Duration.ofMillis(500)
                    ));
            player.showTitle(title);
        }
    }

    private void updateDetecting(Player player) {
        Title title = Title.title(
                Component.empty(),
                Component.text("▸ 探知中").color(NamedTextColor.GREEN),
                Title.Times.times(
                        java.time.Duration.ofMillis(0),
                        java.time.Duration.ofSeconds(1),
                        java.time.Duration.ofMillis(500)
                ));
        player.showTitle(title);

        resetGlowing();
        detectedPlayers = new HashSet<>(detectingPlayers);
        for (Player nearPlayer : player.getWorld().getNearbyPlayers(player.getLocation(), 64)) {
            if (context.game().isTeammate(player, nearPlayer)) continue;
            detectingPlayers.add(nearPlayer);
            context.glowManager().addGlow(player, nearPlayer);
            context.glowManager().setGlowColor(player, nearPlayer, NamedTextColor.RED);
        }
    }

    private void resetGlowing() {
        Player player = getPlayer();
        if (player == null) return;
        for (Player detectedPlayer : detectedPlayers) {
            context.glowManager().setGlowColor(player, detectedPlayer, NamedTextColor.WHITE);
            context.glowManager().removeGlow(player, detectedPlayer);
        }
        detectedPlayers.clear();
    }

    private void playAsteriskEffect(Location center) {
        World world = center.getWorld();

        // 26方向
        List<Vector> dirs = new ArrayList<>();

        // 軸 (6)
        dirs.add(new Vector(1, 0, 0));
        dirs.add(new Vector(-1, 0, 0));
        dirs.add(new Vector(0, 1, 0));
        dirs.add(new Vector(0, -1, 0));
        dirs.add(new Vector(0, 0, 1));
        dirs.add(new Vector(0, 0, -1));

        // 面 (12)
        dirs.add(new Vector(1, 1, 0));
        dirs.add(new Vector(1, -1, 0));
        dirs.add(new Vector(-1, 1, 0));
        dirs.add(new Vector(-1, -1, 0));
        dirs.add(new Vector(1, 0, 1));
        dirs.add(new Vector(1, 0, -1));
        dirs.add(new Vector(-1, 0, 1));
        dirs.add(new Vector(-1, 0, -1));
        dirs.add(new Vector(0, 1, 1));
        dirs.add(new Vector(0, 1, -1));
        dirs.add(new Vector(0, -1, 1));
        dirs.add(new Vector(0, -1, -1));

        // 角 (8)
        dirs.add(new Vector(1, 1, 1));
        dirs.add(new Vector(1, 1, -1));
        dirs.add(new Vector(1, -1, 1));
        dirs.add(new Vector(1, -1, -1));
        dirs.add(new Vector(-1, 1, 1));
        dirs.add(new Vector(-1, 1, -1));
        dirs.add(new Vector(-1, -1, 1));
        dirs.add(new Vector(-1, -1, -1));

        // Dust設定（緑）
        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(0, 255, 0), 1.0f);

        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                step++;

                double dist = step * 0.5;

                for (Vector dir : dirs) {
                    Vector v = dir.clone().normalize().multiply(dist);
                    Location loc = center.clone().add(v);

                    world.spawnParticle(
                            Particle.DUST,
                            loc,
                            4, // count
                            0, 0, 0, // offset
                            0,
                            dust
                    );
                }

                if (step >= 12) {
                    cancel();
                }
            }
        }.runTaskTimer(context.plugin(), 0L, 2L);
    }


}
