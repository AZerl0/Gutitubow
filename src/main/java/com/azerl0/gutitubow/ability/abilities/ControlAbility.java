package com.azerl0.gutitubow.ability.abilities;


import com.azerl0.gutitubow.ability.Ability;
import de.tr7zw.nbtapi.NBT;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.Component;

import java.awt.*;

public class ControlAbility implements Ability {

    private final JavaPlugin plugin;
    private final NamespacedKey controllingKey;
    private final NamespacedKey collectingKey;

    public ControlAbility(JavaPlugin plugin) {
        this.plugin = plugin;
        this.controllingKey = new NamespacedKey(plugin, "controlling");
        this.collectingKey = new NamespacedKey(plugin, "collecting");
    }

    @Override
    public String getId() {
        return "control";
    }

    public void onArrowLaunch(EntityShootBowEvent event) {
        event.getProjectile().setGlowing(true);
        startControl(event);
    }

    @Override
    public void onArrowHit(ProjectileHitEvent event) {
        Player shooter = (Player) event.getEntity().getShooter();

        if (shooter == null) return;

        if (event.getHitEntity() == shooter) {
            shooter.sendMessage("Collect!!");
            event.getEntity().remove();
        }

        if (event.getHitBlock() != null) {
            Boolean collecting = event.getEntity().getPersistentDataContainer().get(collectingKey, PersistentDataType.BOOLEAN);
            if (collecting == null) return;
            if (collecting) event.getEntity().remove();
        }


    }

    public void onPlayerLeftClickBow(PlayerInteractEvent event) {
        Collect(event);
    }

    private void startControl(EntityShootBowEvent event) {

        Arrow arrow = (Arrow) event.getProjectile();
        arrow.setGravity(false);

        arrow.getPersistentDataContainer().set(controllingKey, PersistentDataType.BOOLEAN, true);

        new BukkitRunnable() {

            @Override
            public void run() {

                Boolean controlling = arrow.getPersistentDataContainer().get(controllingKey, PersistentDataType.BOOLEAN);
                boolean inGround = NBT.get(arrow, nbt -> (boolean) nbt.getBoolean("inGround"));

                if (controlling == null) controlling = true;

                if (!arrow.isValid() || inGround || !controlling) {
                    cancel();
                    return;
                }

                arrow.getWorld().spawnParticle(
                        Particle.WITCH,
                        arrow.getLocation(),
                        2, // count
                        0, 0, 0,
                        0
                );

                @NotNull Vector v = event.getEntity().getEyeLocation().getDirection().multiply(1.0);

                arrow.setVelocity(v);


            }

        }.runTaskTimer(plugin, 0L, 1L); // 1tickごと
    }

    private void Collect(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        Title title = Title.title(
                Component.empty(),
                Component.text("周囲の矢を回収します").color(NamedTextColor.LIGHT_PURPLE),
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofSeconds(1),
                        java.time.Duration.ofMillis(500)
                )
        );
        player.showTitle(title);
        player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.8f, 1.0f);


        for (World world : Bukkit.getWorlds()) {
            for (Arrow arrow : world.getEntitiesByClass(Arrow.class)) {

                if (!(arrow.getShooter() instanceof Player shooter)) continue;
                if (!shooter.getUniqueId().equals(event.getPlayer().getUniqueId())) continue;

                Vector newVelocity = shooter.getLocation().toVector().subtract(arrow.getLocation().toVector()).normalize().multiply(3);

                arrow.getPersistentDataContainer().set(controllingKey, PersistentDataType.BOOLEAN, false);
                arrow.getPersistentDataContainer().set(collectingKey, PersistentDataType.BOOLEAN, true);

                arrow.setVelocity(newVelocity);
                //            https://bugs.mojang.com/browse/MC-112474
                double x = -newVelocity.getX();
                double y = -newVelocity.getY();
                double z = newVelocity.getZ();

                float yaw = (float) Math.toDegrees(Math.atan2(-x, z));
                float pitch = (float) Math.toDegrees(Math.atan2(-y, Math.sqrt(x * x + z * z)));

                arrow.setRotation(yaw, pitch);

                NBT.modify(arrow, nbt -> {
                    nbt.setBoolean("inGround", false);

                });
            }
        }
    }
}
