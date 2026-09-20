package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.data.damage.GutitubowDamageType;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerPreDamageEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public class ShrinkAbility extends Ability {
    private BukkitTask shrinkTask;
    private int shrinkRemainedTick;

    public ShrinkAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.SHRINK;
    }

    @Override
    public void init(Player player) {
        super.init(player);
        Objects.requireNonNull(getPlayer().getAttribute(Attribute.SCALE)).setBaseValue(0.75);
    }

    @Override
    public void reset() {
        super.reset();
        Objects.requireNonNull(getPlayer().getAttribute(Attribute.SCALE)).setBaseValue(1.0);
        Objects.requireNonNull(getPlayer().getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.1);
        if (shrinkTask != null) shrinkTask.cancel();
        shrinkTask = null;
    }

    private boolean isShrinking() {
        return shrinkTask != null;
    }

    @Override
    public void onDealDamage(GamePlayerPreDamageEvent event) {
        if (isShrinking()) event.setAmount(event.getAmount() / 2);
    }

    @Override
    public void onDamage(GamePlayerPreDamageEvent event) {
        GutitubowDamageType type = event.getDamageSource().getDamageType();
        Set<GutitubowDamageType> canShrinkDamageTypes = Set.of(
                GutitubowDamageType.ARROW_SHOT,
                GutitubowDamageType.BOMB_BLAST,
                GutitubowDamageType.RICOCHET_BLAST
        );
        if (canShrinkDamageTypes.contains(type)) startShrink();
    }

    private void startShrink() {
        Player player = getPlayer();
        shrinkRemainedTick = 15 * 20;

        player.playSound(player, Sound.ENTITY_PUFFER_FISH_BLOW_OUT, 1.0f, 1.0f);
        Objects.requireNonNull(player.getAttribute(Attribute.SCALE)).setBaseValue(0.5);
        Objects.requireNonNull(player.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.2);


        if (shrinkTask != null && !shrinkTask.isCancelled()) return;

        shrinkTask = new BukkitRunnable() {

            @Override
            public void run() {
                updateShrink();
            }
        }.runTaskTimer(context.gutitubow(), 1L, 1L);
    }

    private void updateShrink() {
        shrinkRemainedTick--;
        if (shrinkRemainedTick <= 0) {
            stopShrink();
            return;
        }
        Player player = getPlayer();
        if (player == null) return;
        if (shrinkRemainedTick == 7 * 20) {
            Objects.requireNonNull(player.getAttribute(Attribute.SCALE)).setBaseValue(0.65);
        }
        player.sendActionBar(Component.text("§b縮身中: §f" + shrinkRemainedTick / 20 + "秒"));
    }

    private void stopShrink() {
        Player player = getPlayer();
        Objects.requireNonNull(player.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.1);
        Objects.requireNonNull(player.getAttribute(Attribute.SCALE)).setBaseValue(0.75);
        player.playSound(player, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 1.0f, 1.0f);
        if (shrinkTask != null) shrinkTask.cancel();
        shrinkTask = null;

    }
}
