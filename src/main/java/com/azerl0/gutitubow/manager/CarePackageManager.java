package com.azerl0.gutitubow.manager;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.UUID;
import java.util.Random;

public class CarePackageManager {

    private final JavaPlugin plugin;

    public CarePackageManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public class CarePackage {

        private final UUID uuid;
        private final NamespacedKey isFallingKey;
        private final NamespacedKey fallingTickKey;

        public CarePackage(UUID uniqueId) {
            this.uuid = uniqueId;
            this.isFallingKey = new NamespacedKey(plugin, "carePackage.isFalling");
            this.fallingTickKey = new NamespacedKey(plugin, "gutitubow.fallingTick");
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

        public void onMove(CarePackage carePackage) {
            Entity entity = carePackage.getCoreEntity();
            if (entity == null) return;
            boolean isFalling = Boolean.TRUE.equals(entity.getPersistentDataContainer().get(isFallingKey, PersistentDataType.BOOLEAN));
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
                Firework firework = coreEntity.getWorld().spawn(coreEntity.getLocation(), Firework.class);
                FireworkMeta meta = firework.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder().withColor(Color.AQUA).with(FireworkEffect.Type.BALL_LARGE).build());
                meta.setPower(0);
                firework.setFireworkMeta(meta);
            }
        }

        private void onLand() {
            removeBalloonEntities();
            Entity coreEntity = this.getCoreEntity();
            if (coreEntity == null) return;
            spawnPoofParticle();

        }

        private void spawnPoofParticle() {
            Entity coreEntity = this.getCoreEntity();
            if (coreEntity == null) return;

            Location center = coreEntity.getLocation();
            int speed = 1;

            for (int i = 0  ; i < 15; i++) {
                double x = Math.random() * 2 - 1;
                double y = Math.random() * 2 - 1;
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
            player.sendMessage("回復ポーションを入手しました");
            this.remove();
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

    public void summonPackage(Location location) {
        location.setRotation(0.0f, 0.0f);
        World world = location.getWorld();

        BlockDisplay blockDisplay = world.spawn(location, BlockDisplay.class, (BlockDisplay display) -> {
            display.setBlock(Material.CHEST.createBlockData());
            display.setGlowing(true);
            Vector3f translation = new Vector3f(-0.5f, -0.1f, -0.5f);
            Transformation transformation = display.getTransformation();
            Transformation newTransformation = new Transformation(
                    translation,
                    transformation.getLeftRotation(),
                    transformation.getScale(),
                    transformation.getRightRotation()
            );
            display.setTransformation(newTransformation);
            display.addScoreboardTag("gutitubow.carePackageEntity");

        });

        Interaction interaction = world.spawn(location, Interaction.class, (Interaction i) -> {
            i.setInteractionWidth(0.8f);
            i.setInteractionHeight(0.8f);
            i.setCustomNameVisible(true);
            i.customName(Component.text("ケアパッケージ"));
            i.addScoreboardTag("gutitubow.carePackageEntity");

        });

        ArmorStand armorStand = world.spawn(location, ArmorStand.class, (ArmorStand as) -> {
            as.setInvisible(true);
            Objects.requireNonNull(as.getAttribute(Attribute.SCALE)).setBaseValue(0);
            Objects.requireNonNull(as.getAttribute(Attribute.GRAVITY)).setBaseValue(0.005f);
            as.addPassenger(blockDisplay);
            as.addPassenger(interaction);
            as.addScoreboardTag("gutitubow.carePackageEntity");
        });

        for (int i = 0; i < 10; i++) {
            double randomRadius = 0.8;
            Random random = new Random();
            double randomX = random.nextDouble(-randomRadius, randomRadius);
            double randomY = random.nextDouble(-randomRadius, randomRadius);
            world.spawn(location.clone().add(randomX, 3.5, randomY), Chicken.class, (Chicken c) -> {
                Objects.requireNonNull(c.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0);
                c.setLeashHolder(armorStand);
                c.addScoreboardTag("gutitubow.carePackageEntity");
                c.setVariant(Chicken.Variant.TEMPERATE);
                c.setInvulnerable(true);

            });
        }
    }

    public CarePackage getCarePackage(UUID uuid) {
        return new CarePackage(uuid);
    }

    public static boolean isCarePackageEntity(Entity entity) {
        return entity.getScoreboardTags().contains("gutitubow.carePackageEntity");
    }
}
