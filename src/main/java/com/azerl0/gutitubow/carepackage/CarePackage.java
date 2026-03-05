package com.azerl0.gutitubow.carepackage;

import com.azerl0.gutitubow.data.item.GutitubowItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CarePackage {
    private final UUID uuid;
    private Set<UUID> used_players = new HashSet<>();
    private final NamespacedKey isFallingKey;
    private final NamespacedKey fallingTickKey;
    private final NamespacedKey remainedPotionKey;

    public CarePackage(UUID uniqueId, NamespacedKey isFallingKey, NamespacedKey fallingTickKey, NamespacedKey remainedPotionKey) {
        this.uuid = uniqueId;
        this.isFallingKey = isFallingKey;
        this.fallingTickKey = fallingTickKey;
        this.remainedPotionKey = remainedPotionKey;
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    private int getFallingTick() {
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return 0;
        PersistentDataContainer pdc = coreEntity.getPersistentDataContainer();
        return pdc.getOrDefault(fallingTickKey, PersistentDataType.INTEGER, 0);
    }

    private int getRemainedPotion() {
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return 0;
        return coreEntity.getPersistentDataContainer().getOrDefault(remainedPotionKey, PersistentDataType.INTEGER, 0);
    }

    public void setRemainedPotion(int count) {
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return;

        coreEntity.getPersistentDataContainer().set(remainedPotionKey, PersistentDataType.INTEGER, count);
        this.setDisplayName(this.getRemainedPotionDisplay());
    }

    private void setDisplayName(Component displayName) {
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return;
        for (Entity entity : coreEntity.getPassengers()) {
            if (entity instanceof Interaction) {
                entity.customName(displayName);
            }
        }
    }

    private Component getRemainedPotionDisplay() {
        return Component.text("ケアパッケージ[" + this.getRemainedPotion() + "]").color(NamedTextColor.GREEN);
    }

    private void setFallingTick(int tick) {
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return;
        PersistentDataContainer pdc = coreEntity.getPersistentDataContainer();
        pdc.set(fallingTickKey, PersistentDataType.INTEGER, tick);
    }


    @Nullable
    public Entity getCoreEntity() {
        return Bukkit.getServer().getEntity(this.uuid);
    }

    public void onMove() {
        Entity entity = this.getCoreEntity();
        if (entity == null) return;
        boolean isFalling = entity.getPersistentDataContainer().getOrDefault(isFallingKey, PersistentDataType.BOOLEAN, true);
        if (isFalling) {
            if (entity.isOnGround()) {
                this.onLand();
                entity.getPersistentDataContainer().set(isFallingKey, PersistentDataType.BOOLEAN, false);
            } else {
                this.onFall();
            }
        } else {
            if (entity.getFallDistance() > 0.0) {
                entity.getPersistentDataContainer().set(isFallingKey, PersistentDataType.BOOLEAN, true);
            }
        }


    }

    private void onFall() {
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return;

        int fallingTick = this.getFallingTick();
        this.setFallingTick(fallingTick + 1);
        if (fallingTick % 60 == 0) {
            spawnFirework();
        }
    }

    private void onLand() {
        removeBalloonEntities();
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return;
        spawnPoofParticle();
        spawnFirework();

    }

    private void spawnFirework() {
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return;
        Firework firework = coreEntity.getWorld().spawn(coreEntity.getLocation(), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(Color.AQUA).with(FireworkEffect.Type.BALL_LARGE).build());
        meta.setPower(0);
        firework.setFireworkMeta(meta);

    }

    private void spawnPoofParticle() {
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return;

        Location center = coreEntity.getLocation();
        int speed = 1;

        for (int i = 0  ; i < 15; i++) {
            double x = Math.random() * 2 - 1;
            double y = Math.random();
            double z = Math.random() * 2 - 1;
            Vector dir = new Vector(x, y, z).normalize().multiply(0.5);

            center.getWorld().spawnParticle(
                    Particle.POOF,
                    coreEntity.getLocation().clone().add(0, 0.5, 0),
                    0,
                    dir.getX(), dir.getY(), dir.getZ(),
                    speed
            );
        }
    }

    public void remove() {
        ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(this.uuid);
        if (armorStand == null) return;

        this.removeBalloonEntities();

        for (Entity passenger : armorStand.getPassengers()) {
            passenger.remove();
        }
        armorStand.remove();
    }

    public void onInteract(Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (this.used_players.contains(player.getUniqueId())) return;
        player.sendMessage(Component.empty());
        player.sendMessage(
                Component.text("[!] ").color(NamedTextColor.RED)
                        .append(Component.text("ケアパッケージから[").color(NamedTextColor.WHITE)
                                .append(Component.text("回復ポーション").color(NamedTextColor.LIGHT_PURPLE))
                                .append(Component.text("]を入手した!").color(NamedTextColor.WHITE))
                        ));
        player.sendMessage(Component.empty());
        this.used_players.add(player.getUniqueId());
        player.give(GutitubowItem.RECOVERY_POTION);

        if (this.getRemainedPotion() <= 1) {
            this.remove();
            return;
        }

        this.setRemainedPotion(getRemainedPotion() - 1);
        setDisplayName(this.getRemainedPotionDisplay());
    }



    public void removeBalloonEntities() {
        ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(this.uuid);
        if (armorStand == null) return;

        for (Entity entity : armorStand.getNearbyEntities(10, 10, 10)) {
            if (entity instanceof LivingEntity living) {
                if (living.isLeashed()) {
                    if (living.getLeashHolder().equals(armorStand)) {
                        living.remove();
                    }
                }
            }
        }
    }

}
