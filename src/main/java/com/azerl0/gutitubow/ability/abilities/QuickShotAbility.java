package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.util.ArrowUtil;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class QuickShotAbility implements Ability {
    private JavaPlugin plugin;

    public QuickShotAbility(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "quickshot";
    }

    public void onPlayerLeftClickBow(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        int cooldown = player.getCooldown(Material.BOW);
        if (cooldown != 0) return;
        QuickShot(player);
        player.setCooldown(Material.BOW, 100);
    }

    private void QuickShot(Player player) {
        Vector velocity = player.getLocation().getDirection().multiply(3);
        Arrow arrow = player.getWorld().spawn(player.getEyeLocation(), Arrow.class);
        arrow.setShooter(player);
        arrow.setVelocity(velocity);
        ArrowUtil.setFixRotation(arrow, velocity);

        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation(), 1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1);
    }
}
