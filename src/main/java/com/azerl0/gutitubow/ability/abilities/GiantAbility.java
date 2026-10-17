package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerOnAbilityErasureEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerPreDamageEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerSneakingEvent;
import com.azerl0.gutitubow.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class GiantAbility extends Ability {
    private final int GIANT_TICK = 50;
    private int toGiantProcessTick;
    private int giantRemainedTick;
    private boolean isGiant = false;

    public GiantAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.GIANT;
    }

    @Override
    protected void onReset() {
        resetGiant();
    }

    @Override
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            if (isGiant) deactivateGiant();
        } else {
            toGiantProcessTick = 0;
        }
    }

    @Override
    public void onSneaking(GamePlayerSneakingEvent event) {
        if (hasCompassCooldown()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        if (isErasing()) return;

        if (!isGiant) {
            toGiantProcessTick++;
            if (toGiantProcessTick >= GIANT_TICK) {
                activateGiant();
            } else {
                Component subtitle = ComponentUtil.getGaugeComponent(
                        Component.text("巨体化チャージ中... ").color(NamedTextColor.GRAY),
                        toGiantProcessTick,
                        GIANT_TICK,
                        NamedTextColor.GOLD,
                        NamedTextColor.GRAY
                );
                player.showTitle(Title.title(
                        Component.empty(),
                        subtitle,
                        Title.Times.times(
                                java.time.Duration.ofMillis(0),
                                java.time.Duration.ofSeconds(1),
                                java.time.Duration.ofMillis(0)
                        )
                ));
            }
        }
    }

    @Override
    public void onDamage(GamePlayerPreDamageEvent event) {
        if (isGiant) {
            double amount = event.getAmount();
            if (amount >= 1) {
                double newAmount = amount / 2;
                if (newAmount % 1 > 0) newAmount = Math.floor(newAmount);
                event.setAmount(newAmount);
            }
        }
    }

    @Override
    public void onAbilityErasure(GamePlayerOnAbilityErasureEvent event) {
        if (isGiant) resetGiant();
    }

    @Override
    protected void onTick() {
        if (isGiant) updateGiant();
    }

    private void activateGiant() {
        Player player = getPlayer();
        if (player == null) return;
        isGiant = true;
        toGiantProcessTick = 0;
        giantRemainedTick = 30 * 20;
        Objects.requireNonNull(player.getAttribute(Attribute.SCALE)).setBaseValue(6.0);
        PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, -1, 1, true, false, false);
        PotionEffect jump = new PotionEffect(PotionEffectType.JUMP_BOOST, -1, 3, true, false, false);
        player.addPotionEffects(List.of(speed, jump));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 1.0f);

        double absorption = -4;
        for (Player nearPlayer : player.getLocation().getNearbyPlayers(50)) {
            if (playerUUID.equals(nearPlayer.getUniqueId())) continue;
            if (context.game().isTeammate(player, nearPlayer)) continue;
            absorption += 4;
        }
        if (absorption > -4) {
            Objects.requireNonNull(player.getAttribute(Attribute.MAX_ABSORPTION)).setBaseValue(absorption);
            PotionEffect effect = new PotionEffect(PotionEffectType.ABSORPTION, -1, 0, true, false, false);
            player.addPotionEffect(effect);
        }
    }

    private void deactivateGiant() {
        Player player = getPlayer();
        if (player == null) return;
        setCompassCooldown(30 * 20);
        startCompassCooldownTask();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_RAVAGER_DEATH, 1.0f, 1.0f);

        resetGiant();
    }

    private void updateGiant() {
        giantRemainedTick--;
        if (giantRemainedTick <= 0) {
            deactivateGiant();
            return;
        }
        Player player = getPlayer();
        if (player == null) return;
        player.sendActionBar(Component.text("§6巨大化: §f" + giantRemainedTick / 20 + "秒"));
    }

    private void resetGiant() {
        isGiant = false;
        toGiantProcessTick = 0;
        Player player = getPlayer();
        if (player == null) return;
        Objects.requireNonNull(player.getAttribute(Attribute.SCALE)).setBaseValue(1.0);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        player.removePotionEffect(PotionEffectType.ABSORPTION);
        player.setAbsorptionAmount(0.0);
    }
}
