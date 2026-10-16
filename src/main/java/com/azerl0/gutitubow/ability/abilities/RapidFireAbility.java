package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.entity.advancearrow.AdvanceProjectile;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerDeathEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerOnAbilityErasureEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerShootEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerSneakingEvent;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.util.ArrowUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RapidFireAbility extends Ability {
    private int rapidFireCharge = 0;
    private final int RAPID_FIRE_MAX_CHARGE = 2 * 20;
    private int remainedRapidFireTick = 0;
    private int shootRapidFireLevel = 0;


    public RapidFireAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.RAPIDFIRE;
    }

    @Override
    protected void onReset() {
        stopRapidFire();
    }

    @Override
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) rapidFireCharge = 0;
    }

    @Override
    public void onSneaking(GamePlayerSneakingEvent event) {
        Player player = event.getPlayer();
        if (player.hasCooldown(Material.BOW)) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (isErasing()) return;

        rapidFireCharge++;
        rapidFireCharge = Math.min(rapidFireCharge, getRapidFireLevel() * RAPID_FIRE_MAX_CHARGE);
        Title title = Title.title(
                Component.empty(),
                getChargeComponent(),
                Title.Times.times(
                        java.time.Duration.ofMillis(0),
                        java.time.Duration.ofSeconds(1),
                        java.time.Duration.ofMillis(0)
                )
        );
        player.showTitle(title);
        if ((rapidFireCharge + 1) % RAPID_FIRE_MAX_CHARGE == 0) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_VAULT_OPEN_SHUTTER, 1.0f, 1.0f);
        }
    }

    @Override
    public void onGamePlayerShoot(GamePlayerShootEvent event) {
        Player player = getPlayer();
        if (player == null) return;

        if (player.isSneaking() && rapidFireCharge >= RAPID_FIRE_MAX_CHARGE) {
            event.setCancelled(true);
            startRapidFire();
        }
    }

    @Override
    public void onTick() {
        Player player = getPlayer();
        if (player == null) return;

        if (remainedRapidFireTick <= 0) return;
        remainedRapidFireTick--;
        if (remainedRapidFireTick <= 0) {
            stopRapidFire();
        } else {
            World world = player.getWorld();
            if (world.getGameTime() % (9 - getShootSpeed()) == 0) {
                rapidFireShoot(player);
            }
            double chargeSecondsText = Math.floor(((double) remainedRapidFireTick / 20) * 10) / 10;
            Component message =
                    Component.text("連射中: " + chargeSecondsText + "秒 ").color(NamedTextColor.RED)
                            .append(Component.text("速度: " + getShootSpeed()).color(NamedTextColor.YELLOW));
            player.sendActionBar(message);

        }
    }

    @Override
    public void onDeath(GamePlayerDeathEvent event) {
        stopRapidFire();
    }

    @Override
    public void onAbilityErasure(GamePlayerOnAbilityErasureEvent event) {
        stopRapidFire();
    }

    private void startRapidFire() {
        Player player = getPlayer();
        if (player == null) return;

        Objects.requireNonNull(player.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.005);
        Objects.requireNonNull(player.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(0);
        player.setCooldown(Material.BOW, 18 * 20);
        shootRapidFireLevel = (int) ((double) (rapidFireCharge / RAPID_FIRE_MAX_CHARGE));
        GamePlayer gamePlayer = context.game().getGamePlayer(player);
        gamePlayer.setArrowAmount(gamePlayer.getArrowAmount() - shootRapidFireLevel);

        remainedRapidFireTick = switch (shootRapidFireLevel) {
            case 1 -> 3 * 20;
            case 2 -> 6 * 20;
            case 3 -> 9 * 20;
            default -> 0;
        };
        rapidFireCharge = 0;
    }


    private void rapidFireShoot(@NotNull Player player) {
        Vector velocity = player.getLocation().getDirection().multiply(3);
        Arrow arrow = player.getWorld().spawn(player.getEyeLocation(), Arrow.class);
        arrow.setShooter(player);
        arrow.setVelocity(velocity);
        ArrowUtil.setFixRotation(arrow, velocity);

        player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);
        boolean result = getGamePlayer().shoot(GamePlayerShootEvent.ShootReason.RAPID_FIRE, arrow.getLocation(), velocity, 1.0f, arrow);
        if (!result) return;

        AdvanceProjectile advanceProjectile = getAdvanceProjectile(arrow);
        advanceProjectile.setParticle(location -> location.getWorld().spawnParticle(
                Particle.INSTANT_EFFECT,
                location,
                3,
                0.15, 0.15, 0.15,
                new Particle.Spell(Color.fromRGB(199, 126, 0), 1)
        ));
    }

    private void stopRapidFire() {
        rapidFireCharge = 0;
        Player player = getPlayer();
        if (player == null) return;
        Objects.requireNonNull(player.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.1);
        Objects.requireNonNull(player.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(0.42);
    }

    private int getRapidFireLevel() {
        Player player = getPlayer();
        if (player == null) return 0;
        GamePlayer gamePlayer = context.game().getGamePlayer(player);
        return Math.min(gamePlayer.getMaxArrowStackSize(), 3);
    }

    private int getShootSpeed() {
        switch (shootRapidFireLevel) {
            case 1:
                if (remainedRapidFireTick <= 2 * 20) return 2;
                if (remainedRapidFireTick <= 3 * 20) return 1;
            case 2:
                if (remainedRapidFireTick <= 2 * 20) return 4;
                if (remainedRapidFireTick <= 4 * 20) return 3;
                if (remainedRapidFireTick <= 6 * 20) return 2;
            case 3:
                if (remainedRapidFireTick <= 2 * 20) return 5;
                if (remainedRapidFireTick <= 4 * 20) return 4;
                if (remainedRapidFireTick <= 6 * 20) return 3;
                if (remainedRapidFireTick <= 8 * 20) return 2;
                if (remainedRapidFireTick <= 9 * 20) return 1;
        }
        return 1;
    }

    private Component getChargeComponent() {
        Component charge = Component.empty();
        for (int i = 0; i < 10; i++) {
            double section = (double) RAPID_FIRE_MAX_CHARGE / 10 * i;
            NamedTextColor color;
            if (RAPID_FIRE_MAX_CHARGE * 2 + section < rapidFireCharge) {
                color = NamedTextColor.RED;
            } else if (RAPID_FIRE_MAX_CHARGE + section < rapidFireCharge) {
                color = NamedTextColor.YELLOW;
            } else if (section < rapidFireCharge) {
                color = NamedTextColor.GREEN;
            } else {
                color = NamedTextColor.GRAY;
            }
            charge = charge.append(Component.text("■").color(color));
        }

        return Component.text("チャージ中... [").color(NamedTextColor.GRAY).append(charge).append(Component.text("]").color(NamedTextColor.GRAY));
    }
}
