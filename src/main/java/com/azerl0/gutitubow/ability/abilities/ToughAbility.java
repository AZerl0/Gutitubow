package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.data.damage.GutitubowDamageType;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerDamageEvent;
import com.azerl0.gutitubow.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public class ToughAbility extends Ability {
    private int regenerationTick;
    private int regeneratingProgressTick;
    private BukkitTask regenerationTask;

    public ToughAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.TOUGH;
    }

    @Override
    public void init(Player player) {
        this.playerUUID = player.getUniqueId();
        regenerationTick = 60 * 20;
        regeneration();
        regeneratingProgressTick = 0;
        Objects.requireNonNull(player.getAttribute(Attribute.MAX_ABSORPTION)).setBaseValue(-2);
    }

    @Override
    public void reset() {
        stopRegeneration();
        Player player = getPlayer();
        Objects.requireNonNull(player.getAttribute(Attribute.MAX_ABSORPTION)).setBaseValue(0);
        player.removePotionEffect(PotionEffectType.ABSORPTION);
        player.setAbsorptionAmount(0);
    }

    @Override
    public void onDamage(GamePlayerDamageEvent event) {
        Player player = getPlayer();
        GutitubowDamageType damageType = event.getDamageSource().getDamageType();
        Set<GutitubowDamageType> cantCancelDamage = Set.of(GutitubowDamageType.OUTSIDE_BORDER);
        if (event.getAmount() <= 1.0 && !cantCancelDamage.contains(damageType)) event.setCancelled(true);

        double absorption = player.getAbsorptionAmount();
        if (absorption - event.getAmount() > 0.0) return;
        regeneratingProgressTick = 0;
        startRegeneration();

    }

    private void startRegeneration() {

        if (regenerationTask != null && !regenerationTask.isCancelled()) return;

        regenerationTask = new BukkitRunnable() {
            @Override
            public void run() {
                regenerating();
            }
        }.runTaskTimer(context.plugin(), 0L, 1L);
    }

    private void regenerating() {
        regeneratingProgressTick++;
        Player player = getPlayer();

        if (regeneratingProgressTick > 60 * 20) {
            regeneration();
            stopRegeneration();
            return;
        }
        player.sendActionBar(ComponentUtil.getGaugeComponent(
                Component.text("§d肉体再生中... "),
                regeneratingProgressTick,
                regenerationTick,
                NamedTextColor.LIGHT_PURPLE,
                NamedTextColor.WHITE
        ));
    }

    private void regeneration() {
        Player player = getPlayer();
        if (player == null) return;

        PotionEffect effect = new PotionEffect(PotionEffectType.ABSORPTION, -1, 0, true, false, false);
        player.addPotionEffect(effect);
        player.playSound(player, Sound.BLOCK_ANVIL_DESTROY, 0.5f, 1.0f);
        Title title = Title.title(
                Component.empty(),
                Component.text("肉体の再生が完了した。").color(NamedTextColor.DARK_RED),
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofSeconds(1),
                        java.time.Duration.ofMillis(500)
                )
        );
        player.showTitle(title);
    }

    private void stopRegeneration() {
        if (regenerationTask != null) regenerationTask.cancel();
        regenerationTask = null;
        Player player = getPlayer();
        player.sendActionBar(Component.empty());
    }
}
