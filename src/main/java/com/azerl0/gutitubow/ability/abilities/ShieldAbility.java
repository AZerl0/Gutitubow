package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
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
    private final int SHIELD_REG_TIMER = 10 * 20;
    private final int MAX_SHIELD_HEALTH = 5;
    private final int SHIELD_HEIGHT = 5;
    private final int SHIELD_WIDTH = 11;
    private BukkitTask updateTask;
    private Shield shield;

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
        shield = new Shield();
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
        super.reset();
        shield.remove();
        updateTask.cancel();
    }

    @Override
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        Player player = getPlayer();
        if (player == null) return;

        if (event.isSneaking()) {
            if (!shield.isBlocking() && shield.getHealth() > 0 && player.getGameMode() != GameMode.SPECTATOR && !hasCompassCooldown()) shield.raiseShield();
        } else {
            if (shield.isBlocking()) shield.lowerShield();
        }
    }

    @Override
    public void onAnyArrowHit(ProjectileHitEvent event) {
        if (event.isCancelled()) return;
        Entity hitEntity = event.getHitEntity();
        if (hitEntity == null) return;
        if (!shield.isThisShieldEntity(hitEntity)) return;
        Entity shooter = (Entity) event.getEntity().getShooter();
        if (shooter != null && context.game().isTeammate(playerUUID, shooter.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        block(event.getEntity().getLocation());
        shield.damage();
        event.getEntity().remove();
    }

    public Shield getShield() {
        return shield;
    }

    private void update() {
        Player player = getPlayer();
        if (player == null) return;
        shield.update();
        Component healthComponent = Component.empty();
        for (int i = 0; i < 5; i++) {
            healthComponent = healthComponent.append(Component.text(i < shield.getHealth() ? "♥" : "-").color(NamedTextColor.RED));
        }
        player.sendActionBar(Component.text("SHIELD: ").append(healthComponent));
    }

    private void block(Location location) {
        World world = location.getWorld();
        BlockData blockData = Material.BARRIER.createBlockData();
        world.spawnParticle(Particle.BLOCK_MARKER, location, 1, blockData);
        world.playSound(location, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.5f, 2.0f);
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

    public class Shield {
        private final Map<UUID, Integer> shieldEntities = new HashMap<>();
        Map<UUID, Long> blockedPlayers = new HashMap<>();
        private int shieldRegProgressTimer = SHIELD_REG_TIMER;
        private boolean isBlocking = false;

        private int health;

        private Shield() {
            health = 5;
            for (int i = 0; i < SHIELD_HEIGHT * SHIELD_WIDTH; i ++) {
                World world = getPlayer().getWorld();
                ArmorStand armorStand = world.spawn(getPlayer().getLocation(), ArmorStand.class, (ArmorStand as) -> {
                    as.setInvulnerable(true);
                    as.setGravity(false);
                    as.setInvisible(true);
                    as.setMarker(true);
                    as.addDisabledSlots(EquipmentSlot.HEAD);
                    as.addScoreboardTag("gutitubow.shieldEntity");
                });
                shieldEntities.put(armorStand.getUniqueId(), i);

            }
        }

        public UUID getOwnerUUID() {
            return playerUUID;
        }

        public boolean isBlocking() {
            return isBlocking;
        }

        public int getHealth() {
            return health;
        }

        public void setHealth(int health) {
            this.health = health;
        }

        public boolean isThisShieldEntity(Entity entity) {
            return shieldEntities.containsKey(entity.getUniqueId());
        }

        public boolean isThisShieldEntity(UUID uuid) {
            return shieldEntities.containsKey(uuid);
        }


        private void remove() {
            isBlocking = false;
            for (UUID uuid : shieldEntities.keySet()) {
                Entity entity = Bukkit.getEntity(uuid);
                if (entity != null) entity.remove();
            }

        }

        private void update() {
            Player player = getPlayer();
            if (player == null) return;

            if (health > MAX_SHIELD_HEALTH) {
                health = MAX_SHIELD_HEALTH;
            }

            if (isBlocking) {
                for (UUID uuid : shieldEntities.keySet()) {
                    int index = shieldEntities.get(uuid);
                    int indexX = index % SHIELD_WIDTH;
                    int indexY = index / SHIELD_WIDTH;
                    Entity entity = Bukkit.getEntity(uuid);
                    if (entity == null) continue;
                    if (!(entity instanceof ArmorStand armorStand)) continue;
                    World world = player.getWorld();

                    armorStand.setItem(EquipmentSlot.HEAD, ItemStack.of(getShieldDisplayItem(health)));
                    Location teleportLocation = getOffsetLocation(player, 3.5, 0.6     * (indexX - SHIELD_WIDTH / 2), 0.6 * (indexY - SHIELD_HEIGHT / 2) - 1);
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
                setCompassCooldown(200);

            } else {
                for (UUID uuid : shieldEntities.keySet()) {
                    Entity entity = Bukkit.getEntity(uuid);
                    if (entity == null) continue;
                    if (!(entity instanceof ArmorStand armorStand)) continue;
                    armorStand.teleport(player);
                }
                shieldRecoveryUpdate();

            }
        }

        private void shieldRecoveryUpdate() {
            if (shield.getHealth() >= MAX_SHIELD_HEALTH) return;
            shieldRegProgressTimer--;
            if (shieldRegProgressTimer <= 0) {
                health++;
                shieldRegProgressTimer = SHIELD_REG_TIMER;
            }
        }

        public void damage() {
            health -= 1;
            if (health <= 0) breakShield();
        }

        public void breakShield() {
            Player player = getPlayer();
            if (player == null) return;

            isBlocking = false;
            shieldRegProgressTimer = 20 * 20;
            health = 0;
            World world = player.getWorld();
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

        private void raiseShield() {
            Player player = getPlayer();
            if (player == null) return;

            player.getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.75f, 2.0f);
            isBlocking = true;
            for (UUID uuid : shieldEntities.keySet()) {
                Entity entity = Bukkit.getEntity(uuid);
                if (entity == null) return;
                if (!(entity instanceof ArmorStand armorStand)) return;
                if (!isInvalidBottomShieldEntity(uuid)) armorStand.setMarker(false);
                Objects.requireNonNull(armorStand.getAttribute(Attribute.SCALE)).setBaseValue(1.0);

            }
        }

        private void lowerShield() {
            Player player = getPlayer();
            if (player == null) return;

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
            setCompassCooldown(20);
            startCompassCooldownTask();
        }

        private boolean isInvalidBottomShieldEntity(UUID uuid) {
            if (!shieldEntities.containsKey(uuid)) return false;
            int indexY = shieldEntities.get(uuid) / SHIELD_WIDTH;
            return indexY == SHIELD_HEIGHT || indexY == SHIELD_HEIGHT - 1;
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
    }
}
