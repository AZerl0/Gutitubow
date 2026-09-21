package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.data.damage.DamageSource;
import com.azerl0.gutitubow.data.damage.GutitubowDamageType;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerDeathEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerKillEvent;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.util.AdvancementUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public class LastStandAbility extends Ability {
    private BukkitTask lastStandTask;
    private final int LAST_STAND_TICK = 30 * 20;
    private int lastStandRemainedTick = 0;

    public LastStandAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.LASTSTAND;
    }

    @Override
    protected void onReset() {
        stopLastStand();
    }

    @Override
    public void onDeath(GamePlayerDeathEvent event) {
        Player player = getPlayer();
        if (player == null) return;
        if (!isActive() && event.getDamageSource().getDamageType() != GutitubowDamageType.FAILED_LAST_STAND) {
            event.setCancelled(true);
            double absorptionAmount = context.game().getGameSetting().getDefaultHealth() - 2;
            Player attacker = event.getDamageSource().getDirectPlayer();
            if (attacker instanceof Player attackPlayer) {
                absorptionAmount = Objects.requireNonNull(attackPlayer.getAttribute(Attribute.MAX_HEALTH)).getValue() + attackPlayer.getAbsorptionAmount();
            }
            activeLastStand(absorptionAmount);
        } else {
            stopLastStand();
        }
    }

    @Override
    public void onKill(GamePlayerKillEvent event) {
        if (isActive()) {
            stopLastStand();
            Player player = getPlayer();
            if (player == null) return;

            double health;
            if (context.game().isStarted()) {
                health = context.game().getGameSetting().getDefaultHealth();
            } else {
                health = 20;
            }
            Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(health);
            player.setHealth(health);
            Objects.requireNonNull(player.getAttribute(Attribute.MAX_ABSORPTION)).setBaseValue(0);
            player.removePotionEffect(PotionEffectType.ABSORPTION);
            player.setAbsorptionAmount(0);
            player.removePotionEffect(PotionEffectType.SPEED);
            GamePlayer gamePlayer = context.game().getGamePlayer(player);
            gamePlayer.setArrowReloadTick(60);
            event.setKillMessage(Component.text("背水の陣は成功した").color(NamedTextColor.YELLOW));
            AdvancementUtil.grantAdvancement(player, "gutitubow:ability/extend/laststand_success");
        }
    }

    private void activeLastStand(double absorptionAmount) {
        if (isActive()) return;
        lastStandRemainedTick = LAST_STAND_TICK;

        Player player = getPlayer();
        if (player == null) return;
        World world = player.getWorld();
        Location location = player.getLocation();

        PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, -1, 1, true, false, true);
        PotionEffect absorption = new PotionEffect(PotionEffectType.ABSORPTION, -1, 0, true, false, true);
        player.addPotionEffects(Set.of(speed, absorption));
        GamePlayer gamePlayer = context.game().getGamePlayer(player);
        gamePlayer.setMaxArrowStackSize(3);
        gamePlayer.setArrowReloadTick(20);
        gamePlayer.setArrowAmount(3);
        world.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
        world.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
        world.strikeLightningEffect(location);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, location, 3);


        Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(2);
        Objects.requireNonNull(player.getAttribute(Attribute.MAX_ABSORPTION)).setBaseValue(absorptionAmount);
        player.setAbsorptionAmount(absorptionAmount);
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, -1, 0, true, false, false));

        Title title = Title.title(
                Component.empty(),
                Component.text("背水の陣を敷いた").color(NamedTextColor.YELLOW),
                Title.Times.times(
                        java.time.Duration.ofMillis(500),
                        java.time.Duration.ofSeconds(2),
                        java.time.Duration.ofMillis(500)
                )
        );
        player.showTitle(title);



        if (lastStandTask != null && !lastStandTask.isCancelled()) return;

        lastStandTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateLastStand();
            }
        }.runTaskTimer(context.gutitubow(), 0L, 1L);
    }

    private void updateLastStand() {
        lastStandRemainedTick--;
        Player player = getPlayer();
        if (player == null) return;

        if (lastStandRemainedTick <= 0) {
            stopLastStand();
            GamePlayer gamePlayer = context.game().getGamePlayer(player);
            DamageSource damageSource = DamageSource.builder(GutitubowDamageType.FAILED_LAST_STAND).build();
            gamePlayer.damage(damageSource, 255);
        } else {
            World world = player.getWorld();
            Location location = player.getLocation();
            world.spawnParticle(Particle.FLAME, location, 2, 0.2, 0.2, 0.2, 0);
            player.sendActionBar(Component.text("§e[!] " + Math.floor(((double) lastStandRemainedTick / 20) * 10) / 10 + "秒以内に誰かを倒せ！ [!]"));
        }
    }

    private boolean isActive() {
        return lastStandRemainedTick > 0;
    }

    private void stopLastStand() {
        if (lastStandTask != null) lastStandTask.cancel();
        lastStandTask = null;
        lastStandRemainedTick = 0;
        Player player = getPlayer();
        if (player == null) return;
        player.removePotionEffect(PotionEffectType.ABSORPTION);
        player.removePotionEffect(PotionEffectType.SPEED);
    }
}
