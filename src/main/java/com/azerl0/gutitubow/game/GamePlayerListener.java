package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityManager;
import com.azerl0.gutitubow.util.EntityUtil;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import java.util.Random;

public class GamePlayerListener implements Listener {
    private final Game game;
    private final AbilityManager abilityManager;

    public GamePlayerListener(Game game, AbilityManager abilityManager) {
        this.game = game;
        this.abilityManager = abilityManager;
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;

        GamePlayer gamePlayer = this.game.getGamePlayer(player);
        gamePlayer.removeArrow(1);

        Ability ability = abilityManager.getAbility(player);
        if (ability != null) {
            ability.onArrowLaunch(event);
        }

    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (event.isCancelled()) return;

        Entity shooter = (Entity) event.getEntity().getShooter();
        Entity tar = event.getHitEntity();

        if (!(event.getEntity() instanceof AbstractArrow)) return;
        if (shooter == null || tar == null) return;

        if (shooter.getUniqueId().equals(tar.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (EntityUtil.isInvalidHitEntity(tar)) return;

        if (!(tar instanceof Player tarPlayer)) {
            event.setCancelled(true);
            return;
        }
        if (!(shooter instanceof Player shooterPlayer)) return;

        double distance = shooter.getLocation().distance(tar.getLocation());

        this.game.damage(tarPlayer, 2, shooterPlayer, distance);

        GamePlayer gamePlayer = game.getGamePlayer(shooterPlayer);
        gamePlayer.setTotalHit(gamePlayer.getTotalHit() + 1);
        if (gamePlayer.getTotalHit() % 2 == 0) {
            Random random = new Random();
            if (random.nextBoolean()) {
                if (!gamePlayer.upgradeArrowReloadTick()) {
                    gamePlayer.upgradeMaxArrowStackSize();
                }
            } else {
                if (!gamePlayer.upgradeMaxArrowStackSize()) {
                    gamePlayer.upgradeArrowReloadTick();
                }
            }
        }

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
    public void onMove(PlayerMoveEvent event) {
        Ability ability = abilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onMove(event);
    }

    @EventHandler
    public void onToggleSprint(PlayerToggleSprintEvent event) {
        Ability ability = abilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onToggleSprint(event);
    }

    @EventHandler
    public void onJump(PlayerJumpEvent event) {
        Ability ability = abilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onJump(event);
    }

    @EventHandler
    public void onPressJumpKey(PlayerInputEvent event) {
        if (!event.getInput().isJump()) return;
        Ability ability = abilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onPressJumpKey(event);

    }
}
