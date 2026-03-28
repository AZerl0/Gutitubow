package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.data.damage.GutitubowDamageType;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerDamageEvent;
import com.azerl0.gutitubow.game.GamePlayer;
import com.azerl0.gutitubow.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class InvisibleAbility extends Ability {
    private BukkitTask sneakingTask;
    private BukkitTask invisibleTask;
    private final int toInvisibleTick = 5 * 20;
    private int toInvisibleProgressTick;

    public InvisibleAbility(AbilityContext context) {
        super(context);
    }


    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.INVISIBLE;
    }

    @Override
    public void init(Player player) {
        this.playerUUID = player.getUniqueId();
        toInvisibleProgressTick = 0;
    }

    @Override
    public void reset() {
        if (sneakingTask != null) sneakingTask.cancel();
        sneakingTask = null;
        stopInvisible();
    }

    private boolean isInvisible() {
        return invisibleTask != null;
    }

    @Override
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            if (sneakingTask != null && !sneakingTask.isCancelled()) return;

            sneakingTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!getPlayer().isSneaking()) {
                        this.cancel();
                        return;
                    }
                    onSneaking();
                }
            }.runTaskTimer(context.plugin(), 0L, 1L);
        } else {
            toInvisibleProgressTick = 0;
        }
    }

    private void onSneaking() {
        if (!isInvisible()) {
            toInvisible();
        }
    }

    private void toInvisible() {
        if (isInvisible()) return;
        Player player = getPlayer();
        toInvisibleProgressTick++;

        if (toInvisibleProgressTick >= toInvisibleTick) {
            startInvisible();
            return;
        }

        Component subtitle = ComponentUtil.getGaugeComponent(
                Component.text("§7隠密中... "),
                toInvisibleProgressTick,
                toInvisibleTick,
                NamedTextColor.BLACK,
                NamedTextColor.GRAY
        );
        Title title = Title.title(Component.empty(),
                subtitle,
                Title.Times.times(
                        java.time.Duration.ofMillis(0),
                        java.time.Duration.ofSeconds(1),
                        java.time.Duration.ofMillis(500)
                ));
        player.showTitle(title);

    }

    private void startInvisible() {
        Player player = getPlayer();
        if (player == null) return;

        player.playSound(player, Sound.ENTITY_PHANTOM_AMBIENT, 1.0f, 1.0f);

        if (invisibleTask != null && !invisibleTask.isCancelled()) return;

        invisibleTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateInvisible();
            }
        }.runTaskTimer(context.plugin(), 0L, 1L);
    }

    @Override
    public void onDamage(GamePlayerDamageEvent event) {
        if (event.getAttacker() != null || event.getDamageType() == GutitubowDamageType.UNKNOWN) stopInvisible();
    }


    @Override
    public void onArrowHit(ProjectileHitEvent event) {
        if (event.isCancelled()) return;
        Entity hitEntity = event.getHitEntity();
        if (hitEntity == null) return;
        if (hitEntity instanceof Player) startInvisible();
    }

    @Override
    public void onPlayerRightClickBow(PlayerInteractEvent event) {
        GamePlayer gamePlayer = context.game().getGamePlayer(getPlayer());
        int arrowAmount = gamePlayer.getArrowAmount();
        if (arrowAmount != 0) stopInvisible();
    }

    private void stopInvisible() {
        if (!isInvisible()) return;
        Player player = getPlayer();
        if (invisibleTask != null) invisibleTask.cancel();
        invisibleTask = null;
        if (player == null) return;
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.playSound(player, Sound.ENTITY_WITHER_SHOOT, 0.1f, 1.0f);

        Title title = Title.title(
                Component.empty(),
                Component.text("透明化解除").color(NamedTextColor.YELLOW),
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofSeconds(1),
                        java.time.Duration.ofMillis(500)
                )
        );
        player.showTitle(title);
    }

    private void updateInvisible() {
        Player player = getPlayer();

        if (player == null) {
            stopInvisible();
            return;
        }

        Title title = Title.title(
                Component.empty(),
                Component.text("透明化発動中...").color(NamedTextColor.GRAY),
                Title.Times.times(
                        java.time.Duration.ofMillis(0),
                        java.time.Duration.ofSeconds(1),
                        java.time.Duration.ofMillis(500)
                )
        );
        player.showTitle(title);

        PotionEffect invisibleEffect = new PotionEffect(PotionEffectType.INVISIBILITY, 2, 0, true, false, true);
        PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED, 5, 0, true, false, true);
        player.addPotionEffect(invisibleEffect);
        player.addPotionEffect(speedEffect);

        player.getWorld().spawnParticle(Particle.SMOKE,
                player.getLocation().clone().add(0.0, 1.0, 0.0),
                1,
                1.3, 1.25, 1.3,
                0);
    }
}
