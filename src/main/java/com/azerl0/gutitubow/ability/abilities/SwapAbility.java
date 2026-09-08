package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.util.EntityUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
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
        if (hasCompassCooldown()) return;
        Player player = getPlayer();
        if (player == null) return;
        if (trySwap()) {
            setCompassCooldown(isDebugMode() ? 20 : 15 * 20);
        } else {
            setCompassCooldown(60);
        }
    }

    private boolean trySwap() {
        Player player = getPlayer();
        if (player == null) return false;
        Optional<Entity> lookingEntity = getLookingEntity(player, 100);
        if (lookingEntity.isPresent()) {
            Location playerLocation = player.getLocation().clone();
            Location entityLocation = lookingEntity.get().getLocation().clone();

            player.teleport(entityLocation.setDirection(playerLocation.getDirection()));
            lookingEntity.get().teleport(playerLocation.setDirection(entityLocation.getDirection()));
            entityLocation.getWorld().playSound(entityLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
            playerLocation.getWorld().playSound(playerLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
            return true;
        } else {
            player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(Component.text("視線先に交換できるエンティティがいません").color(NamedTextColor.RED));
            return false;
        }
    }

    private Optional<Entity> getLookingEntity(
            Player player,
            double maxDistance
    ) {
        Optional<Entity> lookingEntity = Optional.empty();
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
            if (context.game().isStarted() && context.game().getPhase() == 0) {
                if (entity.getType() == EntityType.PLAYER) continue;
            }
            if (entity instanceof Arrow arrow) {
                if (arrow.isInBlock()) continue;
            }


            Location playerLocation = player.getLocation().clone();

            RayTraceResult blockHit = player.getWorld().rayTraceBlocks(
                    playerLocation,
                    entity.getLocation().clone().toVector().subtract(playerLocation.toVector()),
                    playerLocation.distance(entity.getLocation()),
                    FluidCollisionMode.NEVER,
                    true
            );

            if (blockHit != null) continue;

            double distance = distanceToRay(player, entity);


            if (distance < nearestDistance && distance <= 20) {
                nearestDistance = distance;
                lookingEntity = Optional.of(entity);
            }
        }

        return lookingEntity;
    }

    private double distanceToRay(Player player, Entity entity) {
        Location playerLocation = player.getEyeLocation();

        Vector origin = playerLocation.toVector();
        Vector direction = playerLocation.getDirection().normalize();

        Vector entityPosition = entity.getLocation().toVector();

        double t = entityPosition.clone()
                .subtract(origin)
                .dot(direction);

        if (t < 0) {
            return Double.MAX_VALUE;
        }

        Vector closestPoint = origin.clone().add(direction.clone().multiply(t));

        return entityPosition.distance(closestPoint);
    }
}
