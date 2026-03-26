package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.game.GamePlayer;
import com.azerl0.gutitubow.util.ArrowUtil;
import com.azerl0.gutitubow.util.MathUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class FlashAbility extends Ability {
    private BukkitTask cooldownTask;


    public FlashAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.FLASH;
    }

    @Override
    public void reset() {
        super.reset();
        if (cooldownTask != null) cooldownTask.cancel();
        cooldownTask = null;
    }

    @Override
    public void onPlayerLeftClickBow(PlayerInteractEvent event) {
        if (!hasCompassCooldown()) {
            launchFlash();
        } else  {
            activeFlash();
        }
    }

    private void launchFlash() {
        Player player = getPlayer();

        Vector velocity = player.getLocation().getDirection().multiply(3);
        SpectralArrow arrow = player.getWorld().spawn(player.getEyeLocation(), SpectralArrow.class);
        arrow.setShooter(player);
        arrow.setVelocity(velocity);
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        ArrowUtil.setFixRotation(arrow, velocity);

        setCompassCooldown(12 * 20);
        if (cooldownTask != null && !cooldownTask.isCancelled()) return;

        cooldownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!hasCompassCooldown()) {
                    getPlayer().sendActionBar(Component.text(" - 能力使用可能 - ").color(NamedTextColor.GREEN));
                    this.cancel();
                    return;
                }
                sendActionBarCompassCooldown();
            }
        }.runTaskTimer(context.plugin(), 0L, 1L);


        new BukkitRunnable() {
            @Override
            public void run() {

                if (!arrow.isValid()) {
                    cancel();
                    return;
                }

                arrow.getWorld().spawnParticle(
                        Particle.INSTANT_EFFECT,
                        arrow.getLocation(),
                        2,
                        0.05, 0.05, 0.05,
                        new Particle.Spell(Color.fromRGB(255, 255, 255), 1)
                );
            }
        }.runTaskTimer(context.plugin(), 0L, 1L);
    }

    private void activeFlash() {
        for (World world : Bukkit.getWorlds()) {
            for (SpectralArrow arrow : world.getEntitiesByClass(SpectralArrow.class)) {
                if (!(arrow.getShooter() instanceof Player shooter)) continue;
                if (!shooter.getUniqueId().equals(playerUUID)) continue;

                flash(arrow.getLocation());
                arrow.remove();
            }
        }
    }

    @Override
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof SpectralArrow)) return;

        Bukkit.getScheduler().runTaskLater(context.plugin(), () -> {
            if (!event.getEntity().isValid()) return;
            event.getEntity().getWorld().spawnParticle(Particle.FLASH, event.getEntity().getLocation(), 2, Color.WHITE);
        }, 15L);

        Bukkit.getScheduler().runTaskLater(context.plugin(), () -> {
            if (!event.getEntity().isValid()) return;
            flash(event.getEntity().getLocation());
            event.getEntity().remove();
        }, 20L);
    }


    private void flash(Location location) {
        World world = location.getWorld();

        for (Player nearPlayer : location.getWorld().getNearbyPlayers(location, 50)) {
            Location eye = nearPlayer.getEyeLocation();
            double distance = eye.distance(location);
            float soundVolume;
            if (distance <= 20) {
                soundVolume = 1.0f;
            } else {
                soundVolume = (float) MathUtil.getLerp(distance, 20, 50, 1.0, 0.05);
            }
            world.playSound(nearPlayer, Sound.ENTITY_FIREWORK_ROCKET_BLAST, soundVolume, 1.0f);
            if (nearPlayer.getGameMode() == GameMode.SPECTATOR) continue;

            Vector direction = location.toVector().subtract(eye.toVector());


            RayTraceResult blockHit = world.rayTraceBlocks(
                    eye,
                    direction,
                    distance,
                    FluidCollisionMode.NEVER,
                    true
            );

            boolean isBlocking = blockHit != null;

            boolean isInView = MathUtil.isInView(getPlayer(), location, 160, 110);
            int duration = getFlashTicks(distance, isInView, isBlocking);
            applyFlashEffect(nearPlayer, duration);

//            nearPlayer.sendMessage("isBlocking: " + isBlocking + ", isInView: " + isInView);
        }
    }

    public static int getFlashTicks(double distance, boolean isInView, boolean isBlocking) {

        if (isBlocking && distance > 25) return 0;

        // 距離減衰
        double distanceFactor = 1.0 / (1.0 + distance / 20);

        // 至近距離ボーナス
        double distanceBonus = distance < 5 && !isBlocking ? 50 : 0;

        // 視界補正
        double viewFactor = isInView ? 1.0 : 0.15;

        // 障害物補正
        double blockingFactor = isBlocking ? 0.025 : 1.0;

        double baseTime = 4.5 * 20;

        return (int) (baseTime * distanceFactor * viewFactor * blockingFactor + distanceBonus);
    }

    private void applyFlashEffect(Player player, long tick) {
        if (tick <= 0) return;

        Title title = Title.title(
                Component.text("\uE000"),
                Component.empty(),
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofMillis(50 * tick),
                        java.time.Duration.ofMillis(1000)
                )
        );
        player.showTitle(title);
        GamePlayer gamePlayer = context.game().getGamePlayer(player);
        gamePlayer.setFlashParticle((int) tick + 20);

    }
}
