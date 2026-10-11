package com.azerl0.gutitubow.entity.shield;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.api.game.Game;
import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import com.azerl0.gutitubow.entity.GutitubowEntityType;
import com.azerl0.gutitubow.event.gutitubowentity.shield.ShieldBreakEvent;
import com.azerl0.gutitubow.event.gutitubowentity.shield.ShieldLowerEvent;
import com.azerl0.gutitubow.event.gutitubowentity.shield.ShieldRaisingEvent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Shield extends GutitubowEntity {
    private final int SHIELD_HEIGHT = 5;
    private final int SHIELD_WIDTH = 11;
    private final int MAX_HEALTH = 5;
    private final int SHIELD_REG_TIMER = 10 * 20;
    private int health;
    private int shieldRegProgress = SHIELD_REG_TIMER;
    private UUID ownerUUID;
    private boolean isBlocking;
    private final Map<ArmorStand, Integer> shieldStands = new HashMap<>();
    private final Map<UUID, Long> blockedPlayer = new HashMap<>();

    public Shield(GutitubowEntityManager gutitubowEntityManager, Location location) {
        super(gutitubowEntityManager, location);
        health = 5;
        isBlocking = false;
    }

    @Override
    public @NotNull GutitubowEntityType getType() {
        return GutitubowEntityType.SHIELD;
    }

    @Override
    public @NotNull Location getLocation() {
        return location;
    }

    @Override
    public void teleport(Location location) {
        for (ArmorStand stand : shieldStands.keySet()) {
            stand.teleport(location);
        }
        this.location = location;
    }

    @Override
    public Set<UUID> getPartEntitiesUUID() {
        Set<UUID> uuidSet = new HashSet<>();
        for (ArmorStand stand : shieldStands.keySet())  uuidSet.add(stand.getUniqueId());
        return uuidSet;
    }

    public void onRemove() {
        for (ArmorStand stand : shieldStands.keySet()) stand.remove();
    }

    @Override
    protected void summon(Location location) {
        for (int i = 0; i < SHIELD_HEIGHT * SHIELD_WIDTH; i++) {
            ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class, as -> {
                as.setInvulnerable(true);
                as.setGravity(false);
                as.setInvisible(true);
                as.setMarker(true);
                as.addDisabledSlots(EquipmentSlot.HEAD);
            });
            shieldStands.put(armorStand, i);
        }
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public @Nullable Player getOwner() {
        return Bukkit.getPlayer(ownerUUID);
    }

    public boolean isBlocking() {
        return isBlocking;
    }

    public int getHealth() {
        return health;
    }

    public void raise() {
        Player owner = getOwner();
        if (owner == null) return;

        owner.getWorld().playSound(owner.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.75f, 2.0f);
        isBlocking = true;
        for (ArmorStand stand : shieldStands.keySet()) {
            if (!isInvalidBottomShieldStand(stand)) stand.setMarker(false);
            Objects.requireNonNull(stand.getAttribute(Attribute.SCALE)).setBaseValue(1.0);
        }
    }

    public void lower() {
        _lower();
        ShieldLowerEvent event = new ShieldLowerEvent(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    private void _lower() {
        Player owner = getOwner();
        if (owner == null) return;

        isBlocking = false;
        for (ArmorStand stand : shieldStands.keySet()) {
            stand.setItem(EquipmentSlot.HEAD, ItemStack.of(Material.AIR));
            stand.setMarker(true);
            Objects.requireNonNull(stand.getAttribute(Attribute.SCALE)).setBaseValue(0.0);
        }
    }

    public void damage() {
        health--;
        if (health <= 0) shieldBreak();
    }

    public void shieldBreak() {
        Player owner = getOwner();
        if (owner == null) return;

        isBlocking = false;
        health = 0;
        World world = owner.getWorld();
        for (ArmorStand stand : shieldStands.keySet()) {
            Location shieldLocation = stand.getEyeLocation();
            BlockData blockData = Material.RED_STAINED_GLASS_PANE.createBlockData();
            world.spawnParticle(Particle.BLOCK, shieldLocation, 4, 0.3, 0.3, 0.3, blockData);
            world.playSound(shieldLocation, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
        }
        _lower();
        ShieldBreakEvent event = new ShieldBreakEvent(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    protected void onTick() {
        Player owner = getOwner();
        if (owner == null) return;
        Game game = Gutitubow.get().getGame();

        if (health > MAX_HEALTH) {
            health = MAX_HEALTH;
        }

        if (isBlocking) {
            for (ArmorStand armorStand : shieldStands.keySet()) {

                int index = shieldStands.get(armorStand);
                int indexX = index % SHIELD_WIDTH;
                int indexY = index / SHIELD_WIDTH;
                World world = owner.getWorld();

                armorStand.setItem(EquipmentSlot.HEAD, ItemStack.of(getShieldDisplayItem(health)));
                Location teleportLocation = getOffsetLocation(owner, 3.5, 0.6     * (indexX - SHIELD_WIDTH / 2), 0.6 * (indexY - SHIELD_HEIGHT / 2) - 1);
                armorStand.teleport(teleportLocation.add(0.0, -1.0, 0.0));
                EulerAngle headAngle = new EulerAngle(
                        Math.toRadians(owner.getEyeLocation().getPitch()),
                        0,
                        0
                );
                armorStand.setHeadPose(headAngle);

                if (isInvalidBottomShieldStand(armorStand)) continue;
                for (Player nearPlayer : world.getNearbyPlayers(armorStand.getLocation(), 1)) {
                    if (game.isTeammate(getOwner(), nearPlayer)) continue;
                    UUID nearPlayerUUID = nearPlayer.getUniqueId();
                    if (blockedPlayer.containsKey(nearPlayerUUID) && world.getGameTime() - blockedPlayer.get(nearPlayerUUID) < 20) {
                        continue;
                    } else {
                        blockedPlayer.remove(nearPlayerUUID);
                    }
                    blockedPlayer.put(nearPlayerUUID, world.getGameTime());

                    Vector velocity = owner.getLocation().getDirection().multiply(2);
                    if (velocity.getY() < 0) velocity.setY(0.5);
                    nearPlayer.setVelocity(velocity);
                    blockEffect(nearPlayer.getLocation());
                }

            }
            ShieldRaisingEvent event = new ShieldRaisingEvent(this);
            Bukkit.getPluginManager().callEvent(event);
        } else {
            for (ArmorStand stand : shieldStands.keySet()) {
                stand.teleport(owner);
            }
            recoveryTick();
        }
    }

    private void recoveryTick() {
        if (health >= MAX_HEALTH) return;
        shieldRegProgress--;
        if (shieldRegProgress <= 0) {
            health++;
            shieldRegProgress = SHIELD_REG_TIMER;
        }
    }

    private Location getOffsetLocation(Player player, double forward, double right, double up) {
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

    public static void blockEffect(Location location) {
        World world = location.getWorld();
        BlockData blockData = Material.BARRIER.createBlockData();
        world.spawnParticle(Particle.BLOCK_MARKER, location, 1, blockData);
        world.playSound(location, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.5f, 2.0f);

    }

    private boolean isInvalidBottomShieldStand(ArmorStand stand) {
        if (!shieldStands.containsKey(stand)) return false;
        int indexY = shieldStands.get(stand) / SHIELD_WIDTH;
        return indexY == SHIELD_HEIGHT || indexY == SHIELD_WIDTH - 1;
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
