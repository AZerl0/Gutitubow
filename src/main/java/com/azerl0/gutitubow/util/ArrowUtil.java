package com.azerl0.gutitubow.util;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Objects;

public class ArrowUtil {

    public static void setFixRotation(AbstractArrow arrow, Vector velocity) {
        // https://bugs.mojang.com/browse/MC-112474
        double x = -velocity.getX();
        double y = -velocity.getY();
        double z = velocity.getZ();

        float yaw = (float) Math.toDegrees(Math.atan2(-x, z));
        float pitch = (float) Math.toDegrees(Math.atan2(-y, Math.sqrt(x * x + z * z)));

        arrow.setRotation(yaw, pitch);
    }

    public static void RicochetEvent(JavaPlugin plugin, ProjectileHitEvent event, double multiple) {
        Projectile proj = event.getEntity();

        Block hitBlock = event.getHitBlock();
        if (hitBlock == null) return;

        if (!(proj instanceof AbstractArrow arrow)) return;


        Vector v = event.getEntity().getVelocity();
        if (v.length() < 0.1 & proj.isInWater()) return;


        Vector newVelocity = event.getEntity().getVelocity().clone().multiply(multiple);
        BlockFace face = event.getHitBlockFace();
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
            });
            arrow.setCritical(arrow.isCritical());

            ArrowUtil.setFixRotation(arrow, newVelocity);
        });
    }

    public static void RicochetEvent(JavaPlugin plugin, ProjectileHitEvent event) {
        RicochetEvent(plugin, event, 1.0);
    }
}
