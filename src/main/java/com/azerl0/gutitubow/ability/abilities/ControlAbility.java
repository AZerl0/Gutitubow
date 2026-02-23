package com.azerl0.gutitubow.ability.abilities;


import com.azerl0.gutitubow.ability.Ability;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class ControlAbility implements Ability {

    private final JavaPlugin plugin;

    public ControlAbility(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "control";
    }

    public void onArrowLaunch(EntityShootBowEvent event) {
        event.getProjectile().setGlowing(true);
        startControl(event);
    }

    private void startControl(EntityShootBowEvent event) {

        Arrow arrow = (Arrow) event.getProjectile();

        new BukkitRunnable() {

            @Override
            public void run() {

                // 矢が消えた or 地面に刺さったら終了
                if (!arrow.isValid() || arrow.isOnGround()) {
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
}
