package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.manager.GamePlayerManager;
import com.azerl0.gutitubow.util.ArrowUtil;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class QuickShotAbility implements Ability {
    private final AbilityContext context;

    public QuickShotAbility(AbilityContext context) {
        this.context = context;
    }

    public final AbilityType getType() {
        return AbilityType.QUICKSHOT;
    }

    @Override
    public void onArrowHit(ProjectileHitEvent event) {
        Player player = (Player) event.getEntity().getShooter();
        if (player != null && event.getHitEntity() != null) player.setCooldown(Material.BOW, 0);
    }

    public void onPlayerLeftClickBow(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        int cooldown = player.getCooldown(Material.BOW);
        if (cooldown != 0) return;
        GamePlayerManager.GamePlayer gamePlayer = context.gamePlayerManager().getGamePlayer(player);
        if (gamePlayer.getArrowAmount() == 0) return;
        QuickShot(player);
        player.setCooldown(Material.BOW, 3 * 20);
    }

    private void QuickShot(Player player) {
        Vector velocity = player.getLocation().getDirection().multiply(3);
        Arrow arrow = player.getWorld().spawn(player.getEyeLocation(), Arrow.class);
        arrow.setShooter(player);
        arrow.setVelocity(velocity);
        ArrowUtil.setFixRotation(arrow, velocity);
        GamePlayerManager.GamePlayer gamePlayer = context.gamePlayerManager().getGamePlayer(player);
        gamePlayer.removeArrow(1);

        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getEyeLocation().add(player.getLocation().getDirection().normalize().multiply(1)), 1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1);
    }
}
