package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerSneakingEvent;
import com.azerl0.gutitubow.game.GamePlayer;
import com.azerl0.gutitubow.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class FloatAbility extends Ability {
    private final double floatAreaRadius = 3;
    private final double maxFloatHeight = 12;
    private final int floatMaxCharge = 20 * 20;

    private int floatCharge;
    private BukkitTask updateTask;
    private BukkitTask floatingTask;
    private int areaPlacingCharge;
    private Location floatAreaCenter;
    private boolean isPlacing;

    public FloatAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.FLOAT;
    }

    @Override
    public void init(Player player) {
        this.playerUUID = player.getUniqueId();
        this.floatCharge = 15 * 20;
        this.floatAreaCenter = null;
        this.isPlacing = false;

        if (updateTask != null && !updateTask.isCancelled()) return;

        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimer(context.plugin(), 0L, 1L);
    }

    @Override
    public void reset() {
        if (updateTask != null) updateTask.cancel();
        updateTask = null;
        stopFloating();
        getPlayer().setCooldown(Material.COMPASS, 0);
    }

    private double getCooldown() {
        Player player = getPlayer();
        if (player == null) return 0.0;
        return player.getCooldown(Material.COMPASS);
    }

    private boolean isOnGround() {
        return ((Entity) getPlayer()).isOnGround();
    }

    private boolean isNowCooldown() {
        return getCooldown() > 0.0;
    }

    private boolean isFloating() {
        return floatingTask != null;
    }

    @Override
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            if (isFloating()) {
                stopFloating();
            }

            areaPlacingCharge = 0;
            if (!(getCooldown() > 0.0)) isPlacing = true;

        }
    }

    private void update() {
        Player player = getPlayer();
        if (player == null) return;

        double cooldown = getCooldown();
        if (cooldown > 0.0) {
            sendActionBarCompassCooldown();
        } else {
            double chargeSecondsText = Math.floor(((double) floatCharge / 20) * 10) / 10;
            Component message = Component.text("浮遊チャージ: " + chargeSecondsText + "秒");
            player.sendActionBar(message);
        }


        if (isOnGround()) {
            if (getPlayer().getWorld().getGameTime() % 3 == 0) floatCharge++;
            if (floatCharge > floatMaxCharge) floatCharge = floatMaxCharge;
        } else {
            areaPlacingCharge = 0;
        }
    }

    @Override
    public void onSneaking(GamePlayerSneakingEvent event) {
        Player player = getPlayer();
        if (player == null) return;
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (mainHandItem.getType() != Material.COMPASS) placingFloatArea();
    }

    private void placingFloatArea() {
        if (isNowCooldown()) return;

        areaPlacingCharge++;
        if (areaPlacingCharge < 20) {
            if (isPlacing) {
                Component subtitle = ComponentUtil.getGaugeComponent(
                        Component.text("§7設置中... "),
                        areaPlacingCharge,
                        20,
                        NamedTextColor.AQUA,
                        NamedTextColor.GRAY
                );

                Title title = Title.title(
                        Component.empty(),
                        subtitle,
                        Title.Times.times(
                                java.time.Duration.ofMillis(0),
                                java.time.Duration.ofSeconds(1),
                                java.time.Duration.ofMillis(0)
                        )
                );
                GamePlayer gamePlayer = context.game().getGamePlayer(getPlayer());
                if (!gamePlayer.isFlashing()) getPlayer().showTitle(title);
            }
        } else {
            if (floatAreaCenter == null && floatingTask == null) startFloating();
        }
    }

    private void stopFloating() {
        floatAreaCenter = null;
        if (floatingTask != null) floatingTask.cancel();
        floatingTask = null;
        Player player = getPlayer();
        if (player == null) return;
        setCompassCooldown(10 * 20);
    }

    private void startFloating() {
        isPlacing = false;
        floatAreaCenter = getPlayer().getLocation();
        floatAreaCenter.getWorld().playSound(floatAreaCenter, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
        if (floatingTask != null && !floatingTask.isCancelled()) return;

        floatingTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateFloat();
            }
        }.runTaskTimer(context.plugin(), 0L, 1L);
    }

    private void updateFloat() {
        floatCharge--;
        if (floatCharge <= 0) {
            stopFloating();
            return;
        }
        Player player = getPlayer();
        if (player == null) {
            stopFloating();
            return;
        }
        Location playerLocation = player.getLocation();
        Location floatingAreaHorizonLocation = floatAreaCenter.clone();
        floatingAreaHorizonLocation.setY(playerLocation.getY());

        if (playerLocation.clone().distance(floatingAreaHorizonLocation) > floatAreaRadius) {
            stopFloating();
            player.setVelocity(player.getVelocity().add(new Vector(0.0, 1.0, 0.0)));
            player.playSound(player, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0f, 1.0f);
            return;
        }

        circleParticle(floatAreaCenter, floatAreaRadius, 30, (double) floatCharge / 2);
        if (floatCharge % 6 == 0) {
            Random random = new Random();
            double rad = Math.toRadians(random.nextDouble(0.0, 360.0));
            double radius = random.nextDouble(0.0, floatAreaRadius);
            double x = radius * Math.cos(rad);
            double z = radius * Math.sin(rad);

            Location location = floatAreaCenter.clone().add(x, random.nextDouble(0.0, maxFloatHeight - 2), z);


            floatAreaCenter.getWorld().spawnParticle(
                    Particle.CAMPFIRE_COSY_SMOKE,
                    location,
                    1,
                    0, 0, 0,
                    0
            );
        }
        playerAppleFloatEffect(player, floatAreaCenter);
    }

    private void playerAppleFloatEffect(Player player, Location floatAreaCenter) {
        Location playerLocation = player.getLocation();
        double playerFloatingHeight = playerLocation.getY() - floatAreaCenter.getY();
        if (playerFloatingHeight < -1) return;

        PotionEffect effect;
        if (playerFloatingHeight < maxFloatHeight) {
            effect = new PotionEffect(PotionEffectType.LEVITATION, 2, 5, true, false, true);
        } else {
            effect = new PotionEffect(PotionEffectType.SLOW_FALLING, 2, 1, true, false, false);
        }
        player.addPotionEffect(effect);
    }

    private void circleParticle(Location center, double radius, double spacing, double radOffset) {
        World world = center.getWorld();
        if (world == null) return;

        for (double angle = 0; angle < 360; angle += spacing) {
            double rad = Math.toRadians(angle);

            double x = radius * Math.cos(rad + radOffset);
            double z = radius * Math.sin(rad + radOffset);

            Location location = center.clone().add(x, 0, z);

            Particle.DustOptions dust = new Particle.DustOptions(
                    Color.fromRGB(30, 240, 255),
                    1.0f
            );

            world.spawnParticle(
                    Particle.DUST,
                    location,
                    1,
                    0, 0, 0,
                    0,
                    dust
            );
        }
    }
}
