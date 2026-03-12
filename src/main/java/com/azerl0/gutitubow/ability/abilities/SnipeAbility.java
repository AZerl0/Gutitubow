package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.manager.GlassBreakManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import com.azerl0.gutitubow.data.tags.GutitubowBlockTags;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SnipeAbility extends Ability {
    private final GlassBreakManager breakManager;

    public SnipeAbility(AbilityContext context) {
        super(context);
        this.breakManager = context.game().getGlassBreakManager();
    }

    @Override
    public final @NotNull AbilityType getType() {
        return AbilityType.SNIPE;
    }

    @Override
    public void onArrowLaunch(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.isSneaking()) return;
        if (event.getForce() < 1.0) return;
        if (__Snipe((AbstractArrow) event.getProjectile())) event.setCancelled(true);
    }

    private boolean __Snipe(AbstractArrow arrow) {
        Player player = (Player) arrow.getShooter();
        if (player == null) return false;
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        World world = player.getWorld();

        double maxDistance = 127;

        RayTraceResult blockHit = __RayTraceSnipe(world, eye, dir, maxDistance);

        double entityDistance = maxDistance;
        Location hitPos = player.getEyeLocation().add(player.getLocation().getDirection().multiply(maxDistance));

        if (blockHit != null) {
            entityDistance = blockHit.getHitPosition().distance(eye.toVector());
            if (blockHit.getHitBlock() != null) {
                hitPos = blockHit.getHitPosition().toLocation(player.getWorld());
            }
        }

        RayTraceResult entityHit = world.rayTraceEntities(
                eye.clone().add(dir.clone().multiply(0.1)),
                dir,
                entityDistance,
                0.2,
                e -> e instanceof LivingEntity && e != player
        );

        if (entityHit != null) {
            Entity hitEntity = entityHit.getHitEntity();
            if (hitEntity instanceof Player hitPlayer) {
                hitPos = entityHit.getHitPosition().toLocation(player.getWorld());
                double distance = entityHit.getHitEntity().getLocation().distance(player.getLocation());

                if (distance > 36.0) {
                    Game game = context.game();
                    game.damage(hitPlayer, 2, player, distance, true);

                } else {
                    Component message =
                            Component.text("")
                                    .append(Component.text("[!] 狙撃失敗: " + Math.floor(distance * 10) / 10 + "m")
                                            .color(NamedTextColor.LIGHT_PURPLE)
                                    );
                    player.sendMessage(message);
                }
            }
        }

        player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.0f);

        player.getWorld().spawnParticle(
                Particle.EXPLOSION,
                player.getEyeLocation(), 1);

        __spawnParticleLine(
                player.getWorld(),
                player.getEyeLocation(),
                hitPos,
                0.2

        );
        return true;
    }

    @Nullable
    private RayTraceResult __RayTraceSnipe(World world, Location location, Vector dir, double maxDistance) {
        RayTraceResult blockHit = world.rayTraceBlocks(
                location,
                dir,
                maxDistance,
                FluidCollisionMode.NEVER,
                true
        );

        if (blockHit == null || blockHit.getHitBlock() == null) return null;
        Block block = blockHit.getHitBlock();
        if (GutitubowBlockTags.BREAKABLE_BY_GUTITUBOW_ARROW.contains(block.getType())) {
            this.breakManager.breakConnectedGlass(block, 64);
            Location hitLocation = blockHit.getHitPosition().toLocation(world);
            double distance = location.distance(hitLocation);
            return __RayTraceSnipe(world, hitLocation, dir, maxDistance - distance);

        } else {
            return blockHit;
        }
    }

    private void __spawnParticleLine(
            World world,
            Location start,
            Location end,
            double spacing
    ) {

        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();

        direction.normalize();

        // 何個パーティクルを打つか
        int particles = (int) (distance / spacing);

        for (int i = 0; i <= particles; i++) {
            Vector offset = direction.clone().multiply(spacing * i);
            Location point = start.clone().add(offset);

            world.spawnParticle(
                    Particle.DUST,
                    point,
                    1,
                    0, 0, 0,
                    0,
                    new Particle.DustOptions(
                            Color.RED,
                            1.0f
                    ),
                    true
            );
        }
    }
}

