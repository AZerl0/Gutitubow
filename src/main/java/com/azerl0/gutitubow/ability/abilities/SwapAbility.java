package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.GutitubowEntityType;
import com.azerl0.gutitubow.entity.carepackage.CarePackage;
import com.azerl0.gutitubow.entity.teleportable.BukkitTeleportableEntity;
import com.azerl0.gutitubow.entity.teleportable.Teleportable;
import com.azerl0.gutitubow.game.GamePhase;
import com.azerl0.gutitubow.util.AdvancementUtil;
import com.azerl0.gutitubow.util.EntityUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SwapAbility extends Ability {
    public SwapAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.SWAP;
    }

    @Override
    public void onPlayerLeftClickBow(PlayerInteractEvent event) {
        Player player = getPlayer();
        if (player == null) return;
        if (hasCompassCooldown()) {
            player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(Component.text("まだクールタイム中です!").color(NamedTextColor.RED));
            return;
        }
        if (isErasing()) {
            onErasureEffect();
            return;
        }
        if (trySwap()) {
            setCompassCooldown(isDebugMode() ? 20 : 15 * 20);
            startCompassCooldownTask();
        } else {
            setCompassCooldown(20);
            startCompassCooldownTask();
        }
    }

    private boolean trySwap() {
        Player player = getPlayer();
        if (player == null) return false;
        Optional<Teleportable> lookingEntity = getLookingEntity(player, 50);
        if (lookingEntity.isPresent()) {
            Vector playerVelocity = player.getVelocity().clone();
            Vector entityVelocity;
            if (lookingEntity.get() instanceof BukkitTeleportableEntity entity) {
                entityVelocity = entity.getEntity().getVelocity().clone();
            } else {
                entityVelocity = null;
            }
            Location playerLocation = player.getLocation().clone();
            Location entityLocation = lookingEntity.get().getLocation().clone();

            player.teleport(entityLocation.clone().setDirection(playerLocation.getDirection()));
            lookingEntity.get().teleport(playerLocation.setDirection(entityLocation.getDirection()));
            player.setVelocity(playerVelocity);
            if (lookingEntity.get() instanceof BukkitTeleportableEntity entity) {
                entity.getEntity().setVelocity(entityVelocity);
            }

            entityLocation.getWorld().playSound(entityLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 2.0f);
            entityLocation.getWorld().playSound(entityLocation, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.5f);

            playerLocation.getWorld().playSound(playerLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 2.0f);
            playerLocation.getWorld().playSound(entityLocation, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.5f);
            if (lookingEntity.get() instanceof CarePackage carePackage && carePackage.isFalling()) {
                AdvancementUtil.grantAdvancement(player, "gutitubow:ability/extend/swap_carepackage");
            }
            return true;
        } else {
            player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(Component.text("視線先に交換できるエンティティがいません").color(NamedTextColor.RED));
            return false;
        }
    }

    private Optional<Teleportable> getLookingEntity(
            Player player,
            double maxDistance
    ) {
        Optional<Teleportable> lookingEntity = Optional.empty();
        double nearestDistance = Double.MAX_VALUE;


        for (Entity entity : player.getEyeLocation().getNearbyEntities(maxDistance, maxDistance, maxDistance)) {
            EntityType entityType = entity.getType();
            Set<EntityType> entityTypeSet = new HashSet<>(List.of(
                    EntityType.PLAYER,
                    EntityType.ARROW,
                    EntityType.SPECTRAL_ARROW
            ));
            if (!entityTypeSet.contains(entityType)) continue;
            if (player.getUniqueId() == entity.getUniqueId()) continue;
            if (EntityUtil.isInvalidHitEntity(entity)) continue;
            if (context.game().isStarted() && context.game().getPhase() == GamePhase.PHASE0) {
                if (entity.getType() == EntityType.PLAYER) continue;
            }
            if (entity instanceof Arrow arrow) {
                if (arrow.isInBlock()) continue;
            }

            Location playerEyeLocation = player.getEyeLocation().clone();
            Location entityLocation = entity.getLocation();
            if (entity instanceof LivingEntity livingEntity) {
                entityLocation = livingEntity.getEyeLocation();
            }

            RayTraceResult blockHit = player.getWorld().rayTraceBlocks(
                    playerEyeLocation,
                    entityLocation.clone().toVector().subtract(playerEyeLocation.toVector()),
                    playerEyeLocation.distance(entityLocation),
                    FluidCollisionMode.NEVER,
                    true
            );

            if (blockHit != null) continue;

            double distance = distanceToRay(player, entity);

            if (distance < nearestDistance && distance <= 20) {
                nearestDistance = distance;
                lookingEntity = Optional.of(new BukkitTeleportableEntity(entity));
            }
        }

        for (GutitubowEntity gutitubowEntity : context.getGutitubowManagers().getGutitubowEntityManager().getGutitubowEntities()) {
            GutitubowEntityType gutitubowEntityType = gutitubowEntity.getType();
            if (gutitubowEntityType != GutitubowEntityType.CAREPACKAGE) continue;
            Location gutitubowEntityLocation = gutitubowEntity.getLocation();
            Location playerLocation = player.getLocation().clone();

            if (gutitubowEntityLocation.distance(playerLocation) > maxDistance) continue;

            Location playerEyeLocation = player.getEyeLocation().clone();

            RayTraceResult blockHit = player.getWorld().rayTraceBlocks(
                    playerEyeLocation,
                    gutitubowEntityLocation.clone().toVector().subtract(playerEyeLocation.toVector()),
                    playerEyeLocation.distance(gutitubowEntityLocation),
                    FluidCollisionMode.NEVER,
                    true
            );

            if (blockHit != null) continue;

            double distance = distanceToRay(player, gutitubowEntityLocation);

            if (distance < nearestDistance && distance <= 20) {
                nearestDistance = distance;
                lookingEntity = Optional.of(gutitubowEntity);
            }
        }

        return lookingEntity;
    }

    private double distanceToRay(Player player, Entity entity) {
        return distanceToRay(player, entity.getLocation());
    }

    private double distanceToRay(Player player, Location location) {
        Location playerLocation = player.getEyeLocation();

        Vector origin = playerLocation.toVector();
        Vector direction = playerLocation.getDirection().normalize();

        Vector targetPosition = location.toVector();

        double t = targetPosition.clone()
                .subtract(origin)
                .dot(direction);

        if (t < 0) {
            return Double.MAX_VALUE;
        }

        Vector closestPoint = origin.clone().add(direction.clone().multiply(t));

        return targetPosition.distance(closestPoint);
    }
}
