package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ShieldAbility extends Ability {
    private final int shieldRegTImer = 10 * 20;
    private final int maxShieldHealth = 5;
    private final int shieldHeight = 5;
    private final int shieldWidth = 11;
    private int shieldHealth;
    private final Map<UUID, Integer> shieldEntities = new HashMap<>();
    Map<UUID, Long> blockedPlayers = new HashMap<>();
    private boolean isBlocking;
    private BukkitTask updateTask;
    private int shieldRegProgressTimer;

    public ShieldAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.SHIELD;
    }

    @Override
    public void init(Player player) {
        this.playerUUID = player.getUniqueId();
        shieldHealth = 5;
        shieldRegProgressTimer = shieldRegTImer;
        isBlocking = false;
        createShield();
        if (updateTask != null && !updateTask.isCancelled()) return;

        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimer(context.plugin(), 0L, 1L);
    }

    @Override
    public void reset() {
        removeShield();
    }

    @Override
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            if (!isBlocking && shieldHealth > 0 && getPlayer().getGameMode() != GameMode.SPECTATOR) raiseShield();
        } else {
            if (isBlocking) lowerShield();
        }
    }

    @Override
    public void onAnyArrowHit(ProjectileHitEvent event) {
        Entity hitEntity = event.getHitEntity();
        if (hitEntity == null) return;
        if (!shieldEntities.containsKey(hitEntity.getUniqueId())) return;
        Entity shooter = (Entity) event.getEntity().getShooter();
        if (shooter != null && shooter.getUniqueId() == playerUUID) return;

        block(event.getEntity().getLocation());
        damageShield();
        event.getEntity().remove();
    }

    private void createShield() {
        shieldEntities.clear();
        for (int i = 0; i < shieldHeight * shieldWidth; i ++) {
            World world = getPlayer().getWorld();
            ArmorStand armorStand = world.spawn(getPlayer().getLocation(), ArmorStand.class, (ArmorStand as) -> {
                as.setInvulnerable(true);
                as.setGravity(false);
                as.setInvisible(true);
                as.setMarker(true);
            });
            shieldEntities.put(armorStand.getUniqueId(), i);

        }
    }

    private void removeShield() {
        isBlocking = false;
        for (UUID uuid : shieldEntities.keySet()) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) entity.remove();
        }

    }

    private void raiseShield() {
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 2.0f);
        isBlocking = true;
        for (UUID uuid : shieldEntities.keySet()) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity == null) return;
            if (!(entity instanceof ArmorStand armorStand)) return;
            if (!isInvalidBottomShieldEntity(uuid)) armorStand.setMarker(false);
            Objects.requireNonNull(armorStand.getAttribute(Attribute.SCALE)).setBaseValue(1.0);

        }
    }

    private void damageShield() {
        shieldHealth -= 1;
        if (shieldHealth <= 0) breakShield();
    }

    private void breakShield() {
        isBlocking = false;
        shieldRegProgressTimer = 20 * 20;
        shieldHealth = 0;
        World world = getPlayer().getWorld();
        for (UUID uuid : shieldEntities.keySet()) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity == null) return;
            if (!(entity instanceof ArmorStand armorStand)) return;
            Location shieldsLocation = armorStand.getEyeLocation();
            BlockData blockData = Material.RED_STAINED_GLASS_PANE.createBlockData();
            world.spawnParticle(Particle.BLOCK, shieldsLocation, 4, 0.3, 0.3, 0.3, blockData);
            world.playSound(shieldsLocation, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
        }
        lowerShield();
    }

    private void lowerShield() {
        Player player = getPlayer();
        isBlocking = false;
        for (UUID uuid : shieldEntities.keySet()) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity == null) return;
            if (!(entity instanceof ArmorStand armorStand)) return;
            armorStand.setItem(EquipmentSlot.HEAD, ItemStack.of(Material.AIR));
            armorStand.setMarker(true);
            Objects.requireNonNull(armorStand.getAttribute(Attribute.SCALE)).setBaseValue(0.0);
        }
        player.setCooldown(Material.BOW, 20);
    }

    private void update() {
        Player player = getPlayer();
        if (player == null) return;
        if (isBlocking) {
            updateShield();
        } else {
            shieldRecoveryUpdate();
            if (shieldHealth > maxShieldHealth) {
                shieldHealth = maxShieldHealth;
            }
            for (UUID uuid : shieldEntities.keySet()) {
                Entity entity = Bukkit.getEntity(uuid);
                if (entity == null) return;
                if (!(entity instanceof ArmorStand armorStand)) return;
                armorStand.teleport(player.getLocation());
            }
        }
        Component healthComponent = Component.empty();
        for (int i = 0; i < 5; i++) {
            healthComponent = healthComponent.append(Component.text(i < shieldHealth ? "♥" : "-").color(NamedTextColor.RED));
        }
        player.sendActionBar(Component.text("SHIELD: ").append(healthComponent));
    }

    private void shieldRecoveryUpdate() {
        if (shieldHealth >= maxShieldHealth) return;
        shieldRegProgressTimer--;
        if (shieldRegProgressTimer <= 0) {
            shieldHealth++;
            shieldRegProgressTimer = shieldRegTImer;
        }
    }

    private void updateShield() {
        Player player = getPlayer();
        if (player == null) return;

        for (UUID uuid : shieldEntities.keySet()) {
            int index = shieldEntities.get(uuid);
            int indexX = index % shieldWidth;
            int indexY = index / shieldWidth;
            Entity entity = Bukkit.getEntity(uuid);
            if (entity == null) continue;
            if (!(entity instanceof ArmorStand armorStand)) continue;
            World world = player.getWorld();

            armorStand.setItem(EquipmentSlot.HEAD, ItemStack.of(getShieldDisplayItem(shieldHealth)));
            Location teleportLocation = getOffsetLocation(player, 3.5, 0.6     * (indexX - shieldWidth / 2), 0.6 * (indexY - shieldHeight / 2) - 1);
            armorStand.teleport(teleportLocation.add(0.0, -1.0, 0.0));
            EulerAngle headAngle = new EulerAngle(
                    Math.toRadians(player.getEyeLocation().getPitch()),
                    0,
                    0
            );
            armorStand.setHeadPose(headAngle);

            if (isInvalidBottomShieldEntity(uuid)) continue;
            for (Player nearPlayer : world.getNearbyPlayers(armorStand.getLocation(), 1, 1, 1)) {
                if (context.game().isTeammate(getPlayer(), nearPlayer)) continue;
                UUID nearPlayerUUID = nearPlayer.getUniqueId();
                if (nearPlayerUUID == playerUUID) continue;
                if (blockedPlayers.containsKey(nearPlayerUUID) && world.getGameTime() - blockedPlayers.get(nearPlayerUUID) < 20) {
                    continue;
                } else {
                    blockedPlayers.remove(uuid);
                }
                if (shieldEntities.containsKey(nearPlayerUUID)) continue;
                blockedPlayers.put(nearPlayerUUID, world.getGameTime());

                Vector velocity = player.getLocation().getDirection().multiply(2);
                if (velocity.getY() < 0) velocity.setY(0.5);
                nearPlayer.setVelocity(velocity);
                block(nearPlayer.getLocation());
            }
        }
        player.setCooldown(Material.BOW, 200);
    }

    private void block(Location location) {
        World world = location.getWorld();
        BlockData blockData = Material.BARRIER.createBlockData();
        world.spawnParticle(Particle.BLOCK_MARKER, location, 1, blockData);
        world.playSound(location, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.5f, 2.0f);

    }

    private Material getShieldDisplayItem(int shieldHealth) {
        return switch (shieldHealth) {
            case (1) -> Material.RED_STAINED_GLASS_PANE;
            case (2) -> Material.YELLOW_STAINED_GLASS_PANE;
            case (3) -> Material.LIME_STAINED_GLASS_PANE;
            case (4) -> Material.LIGHT_BLUE_STAINED_GLASS_PANE;
            case (5) -> Material.BLUE_STAINED_GLASS_PANE;
            default -> Material.BLUE_STAINED_GLASS_PANE;
        };
    }

    public Location getOffsetLocation(Player player, double forward, double right, double up) {
        Location eyeLoc = player.getEyeLocation();
        Vector dir = eyeLoc.getDirection().normalize();

        // ローカル座標系を構築
        Vector worldUp = new Vector(0, 1, 0);
        Vector rightVec = dir.getCrossProduct(worldUp).normalize();
        Vector upVec    = rightVec.getCrossProduct(dir).normalize();

        // 視点の先にn進んで、そこから横・縦にオフセット
        Vector offset = dir.multiply(forward)
                .add(rightVec.multiply(right))
                .add(upVec.multiply(up));

        return eyeLoc.add(offset);
    }

    private boolean isInvalidBottomShieldEntity(UUID uuid) {
        if (!shieldEntities.containsKey(uuid)) return false;
        int indexY = shieldEntities.get(uuid) / shieldWidth;
        return indexY == shieldHeight || indexY == shieldHeight - 1;
    }

}
