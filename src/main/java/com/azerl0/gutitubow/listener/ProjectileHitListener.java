package com.azerl0.gutitubow.listener;


import com.azerl0.gutitubow.ability.AbilityManager;
import de.tr7zw.nbtapi.NBT;
import org.bukkit.Particle;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class ProjectileHitListener implements Listener {

    private final JavaPlugin plugin;

    public ProjectileHitListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {

        Projectile proj = event.getEntity();

        Player shooter = ((Player) Objects.requireNonNull(proj.getShooter())).getPlayer();
        if (!(shooter != null && AbilityManager.getAbilityId(shooter).equals("ricochet"))) return;

        if (!(proj instanceof AbstractArrow)) return;

        if (event.getHitBlock() == null) return;

        Vector v = event.getEntity().getVelocity();
        if (v.length() < 0.1 & proj.isInWater()) return;

        Block hitBlock = event.getHitBlock();

        boolean crit = NBT.get(proj, nbt -> (boolean) nbt.getBoolean("crit"));
        Vector newVelocity = getVector(event, hitBlock);

        proj.getWorld().spawnParticle(
                Particle.BLOCK,
                proj.getLocation(),
                5,
                0, 0, 0,
                0.1,
                hitBlock.getBlockData());

        Bukkit.getScheduler().runTask(plugin, () -> {

            proj.setVelocity(newVelocity);
            NBT.modify(proj, nbt -> {
                nbt.setBoolean("inGround", false);
                nbt.setBoolean("crit", crit);
            });


//            https://bugs.mojang.com/browse/MC-112474
            double x = -newVelocity.getX();
            double y = -newVelocity.getY();
            double z = newVelocity.getZ();

            float yaw = (float) Math.toDegrees(Math.atan2(-x, z));
            float pitch = (float) Math.toDegrees(Math.atan2(-y, Math.sqrt(x * x + z * z)));

            proj.setRotation(yaw, pitch);

        });

    }

    private static @NotNull Vector getVector(ProjectileHitEvent event, Block hitBlock) {
        BlockFace face = event.getHitBlockFace();

        @NotNull Vector newVelocity = event.getEntity().getVelocity().clone();
        switch (Objects.requireNonNull(face)) {
            case EAST:
            case WEST:
                newVelocity.setX(-newVelocity.getX());
                break;
            case UP:
            case DOWN:
                newVelocity.setY(-newVelocity.getY());
                break;
            case NORTH:
            case SOUTH:
                newVelocity.setZ(-newVelocity.getZ());
                break;
        }
        return newVelocity;
    }

}
