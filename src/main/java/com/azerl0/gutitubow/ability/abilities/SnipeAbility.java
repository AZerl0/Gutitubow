package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.damage.DamageSource;
import com.azerl0.gutitubow.damage.GutitubowDamageType;
import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.shield.Shield;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.game.GamePhase;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.manager.GlassBreakManager;
import com.azerl0.gutitubow.util.AdvancementUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import com.azerl0.gutitubow.data.tags.GutitubowBlockTags;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.isSneaking()) return;
        if (event.getForce() < 1.0) return;
        if (isErasing()) {
            onErasureEffect();
            return;
        }
        snipe();
        event.setCancelled(true);
    }
    @Override
    public void onSwapItem(PlayerSwapHandItemsEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        if (player.getActiveItem().getType() != Material.SPYGLASS) return;
        if (player.getActiveItemHand() == EquipmentSlot.OFF_HAND) return;
        if (player.hasCooldown(Material.SPYGLASS)) return;
        if (isErasing()) {
            onErasureEffect();
            return;
        }

        GamePlayer gamePlayer = getGamePlayer();
        if (gamePlayer.getArrowAmount() <= 0) return;

        snipe();
        gamePlayer.removeArrow(1);
        player.setCooldown(Material.SPYGLASS, 2 * 20);
        event.setCancelled(true);
    }

    private void snipe() {
        Player player = getPlayer();
        if (player == null) return;
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        World world = player.getWorld();
        Location start = eye.clone().add(dir.clone().multiply(0.1));

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
        final boolean[] failed = {false};
        RayTraceResult entityHit = world.rayTraceEntities(
                start,
                dir,
                entityDistance,
                0.2,
                entity -> {
                    if (entity instanceof Player hitPlayer) {
                        return !context.game().isTeammate(player, hitPlayer);
                    } else if (entity instanceof ArmorStand armorStand) {
                        Optional<GutitubowEntity> gutitubowEntity = context.gutitubow().getGutitubowEntityManager().getGutitubowEntity(armorStand);
                        if (gutitubowEntity.isPresent() && gutitubowEntity.get() instanceof Shield shield) {
                            if (context.game().isTeammate(playerUUID, shield.getOwnerUUID())) return false;
                            double distance = start.distance(armorStand.getLocation());
                            if (distance >= 36.0) {
                                shield.shieldBreak();
                                AdvancementUtil.grantAdvancement(player, "gutitubow:ability/extend/snipe_break_shield");
                                return false;
                            } else {
                                if (!failed[0]) {
                                    failed[0] = true;
                                    player.sendMessage(getSnipeMessage(true, distance));
                                }
                                return true;
                            }
                        } else return false;
                    }
                    return false;
                }
        );

        if (entityHit != null) {
            Entity hitEntity = entityHit.getHitEntity();
            if (hitEntity instanceof Player hitPlayer) {
                hitPos = entityHit.getHitPosition().toLocation(player.getWorld());
                double distance = entityHit.getHitEntity().getLocation().distance(player.getLocation());

                Game game = context.game();
                if (!(game.isStarted() && game.getPhase() == GamePhase.PHASE0)) {
                    if (distance > 36.0) {
                        GamePlayer gamePlayer = game.getGamePlayer(hitPlayer);
                        DamageSource damageSource = DamageSource.builder(GutitubowDamageType.SNIPE).directPlayer(player).damageLocation(hitPos).distance(distance).build();
                        gamePlayer.damage(damageSource, 2);

                    } else {
                        player.sendMessage(getSnipeMessage(true, distance));
                    }
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
    }

    private Component getSnipeMessage(boolean isFailed, double distance) {
        if (isFailed) {
            return Component.text("")
                    .append(Component.text("[!] 狙撃失敗: " + Math.floor(distance * 10) / 10 + "m")
                            .color(NamedTextColor.LIGHT_PURPLE)
                    );
        } else {
            return Component.empty();
        }
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
        if (GutitubowBlockTags.BREAKABLE_BY_GUTITUBOW_ARROW.isTagged(block.getType())) {
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

