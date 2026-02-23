package com.azerl0.gutitubow.listener;


import com.azerl0.gutitubow.ability.AbilityManager;
import com.azerl0.gutitubow.manager.GlassBreakManager;
import de.tr7zw.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import java.util.Objects;


public class ProjectileHitListener implements Listener {

    private final JavaPlugin plugin;
    private final GlassBreakManager breakManager;

    public ProjectileHitListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.breakManager = new GlassBreakManager();
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {

        Block block = event.getHitBlock();

        RicochetEvent(event);

        if (block != null && event.getEntity() instanceof Arrow) {

            if (breakManager.breakConnectedGlass(block, 64)) {
                Bukkit.getScheduler().runTask(plugin, () -> NBT.modify(event.getEntity(), nbt -> {
                    nbt.setBoolean("inGround", false);
                }));
            }
        }

    }


    private void RicochetEvent(ProjectileHitEvent event) {
        Projectile proj = event.getEntity();


        Block hitBlock = event.getHitBlock();
        if (hitBlock == null) return;
        Player shooter = ((Player) Objects.requireNonNull(proj.getShooter())).getPlayer();
        if (shooter == null) return;

        String ability = AbilityManager.getAbilityId(shooter);
        if (hitBlock.getType() == Material.SLIME_BLOCK) {
            if (ability != null && ability.equals("control")) return;

        } else {
            if (ability == null || !ability.equals("ricochet")) return;
        }

        if (!(proj instanceof AbstractArrow)) return;


        Vector v = event.getEntity().getVelocity();
        if (v.length() < 0.1 & proj.isInWater()) return;


        boolean crit = NBT.get(proj, nbt -> (boolean) nbt.getBoolean("crit"));

        Vector newVelocity = event.getEntity().getVelocity().clone();
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

}
