package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.util.EntityUtil;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.potion.PotionEffectType;

public class GamePlayerListener implements Listener {
    private final Game game;

    public GamePlayerListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;

        GamePlayer gamePlayer = this.game.getGamePlayer(player);
        gamePlayer.removeArrow(1);
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (event.isCancelled()) return;

        Entity shooter = (Entity) event.getEntity().getShooter();
        Entity tar = event.getHitEntity();

        if (!(event.getEntity() instanceof AbstractArrow)) return;
        if (event.getEntity() instanceof SpectralArrow) return;
        if (shooter == null || tar == null) return;

        if (shooter.getUniqueId().equals(tar.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (EntityUtil.isInvalidHitEntity(tar)) return;

        if (!(tar instanceof Player tarPlayer)) {
            if (!(tar instanceof ArmorStand armorStand && armorStand.isInvulnerable())) event.setCancelled(true);
            return;
        }
        if (!(shooter instanceof Player shooterPlayer)) return;

        double distance = shooter.getLocation().distance(tar.getLocation());

        this.game.damage(tarPlayer, 2, shooterPlayer, distance, true);

        event.setCancelled(true);
        event.getEntity().remove();
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getDamage() == 0.0) return;

        DamageType type = event.getDamageSource().getDamageType();
        if (type == DamageType.OUTSIDE_BORDER) {
            this.game.getGamePlayer(player).borderOutside();
            event.setCancelled(true);
            return;
        }
        if (type == DamageType.GENERIC) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        event.setCancelled(true);
        player.setFoodLevel(20);
        player.setSaturation(20f);
    }

    @EventHandler
    public void onChangePotionEffect(EntityPotionEffectEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        GamePlayer gamePlayer = game.getGamePlayer(player);

        if (event.getModifiedType() == PotionEffectType.BLINDNESS) gamePlayer.setFlashParticleTask();
    }
}
