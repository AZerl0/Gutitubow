package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.event.gameplayer.PlayerFixLeftClickEvent;
import com.azerl0.gutitubow.util.ArrowUtil;
import com.destroystokyo.paper.ClientOption;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CloneAbility extends Ability {
    private final int MAX_CLONE_STOCK_SIZE = 3;
    private int cloneStock = MAX_CLONE_STOCK_SIZE;
    private int cloneRestockTick = 0;
    private final Set<Clone> clones = new HashSet<>();

    public CloneAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.CLONE;
    }

    @Override
    protected void onReset() {
        resetClones();
    }

    @Override
    public void onLeftClick(PlayerFixLeftClickEvent event) {
        Player player = getPlayer();
        if (player == null) return;

        if (hasCompassCooldown()) return;
        if (cloneStock <= 0) return;

        if (isErasing()) {
            onErasureEffect();
            return;
        }

        if (!isDebugMode()) cloneStock--;
        CloneAIType aiType;
        if (player.getInventory().getItemInMainHand().getType() == Material.SPYGLASS) {
            aiType = CloneAIType.SPYGLASS;
        } else if (player.isSneaking()) {
            aiType = CloneAIType.CONTROL_RUN;
        } else aiType = CloneAIType.RUN;
        createClone(aiType);
        setCompassCooldown(10);
        startCompassCooldownTask();
    }

    @Override
    public void onAnyProjectileHit(ProjectileHitEvent event) {
        if (event.isCancelled()) return;
        if (event.getHitEntity() instanceof Mannequin hitMannequin) {
            Clone clone = getClone(hitMannequin.getUniqueId());
            if (clone != null) {
                if (!(event.getEntity().getShooter() instanceof Player shooter)) return;
                if (context.game().isTeammate(shooter.getUniqueId(), playerUUID)) return;
                clone.onHit(shooter);
                event.getEntity().remove();
            }
        }
    }

    @Override
    protected void onTick() {
        Player player = getPlayer();
        if (player == null) return;

        Component message = Component.text("CLONES: ");
        for (int i = 0; i < MAX_CLONE_STOCK_SIZE; i++) {
            message = message.append(Component.text(
                    i < cloneStock ? "☻" : "-"
            ).color(NamedTextColor.RED));
        }
        player.sendActionBar(message);
        if (cloneStock < MAX_CLONE_STOCK_SIZE) {
            cloneRestockTick++;
            int CLONE_RESTOCK_TICK = 20 * 20;
            if (cloneRestockTick >= CLONE_RESTOCK_TICK) {
                cloneStock++;
                cloneRestockTick = 0;
            }
        }
    }

    private void resetClones() {
        for (Clone clone : new HashSet<>(clones)) {
            clone.remove();
        }
    }

    private void applyCloneHitEffect(Player player) {
        PotionEffect glowing = new PotionEffect(PotionEffectType.GLOWING, 4 * 20, 0, true, false, true);
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 4 * 20, 0, true, false, true);
        player.addPotionEffects(Set.of(glowing, blindness));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.0f, 1.0f);
        Title title = Title.title(
                Component.empty(),
                Component.text("分身に攻撃してしまった！").color(NamedTextColor.RED),
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofSeconds(1),
                        java.time.Duration.ofMillis(500)
                )
        );
        player.showTitle(title);

    }

    private void createClone(CloneAIType aiType) {
        Player player = getPlayer();
        if (player == null) return;
        clones.add(new Clone(player, player.getLocation(), aiType));
    }

    private @Nullable Clone getClone(UUID coreEntityUUID) {
        for (Clone clone : clones) {
            if (clone.getEntityUUID() == coreEntityUUID) {
                return clone;
            }
        }
        return null;

    }

    private class Clone {
        private final UUID ownerUUID;
        private final Mannequin mannequin;
        private BukkitTask cloneUpdateTask;
        private Player shootTarget;
        private int shootProgress = 0;
        private final CloneAIType aiType;
        private final double movementSpeed = 0.35;
        private int timer;

        private Clone(Player owner, Location location, CloneAIType aiType) {
            ownerUUID = owner.getUniqueId();

            mannequin = location.getWorld().spawn(location, Mannequin.class);
            mannequin.setProfile(ResolvableProfile.resolvableProfile(owner.getPlayerProfile()));
            mannequin.setMainHand(owner.getMainHand());
            mannequin.setSkinParts(owner.getClientOption(ClientOption.SKIN_PARTS));
            mannequin.setInvulnerable(true);

            this.aiType = aiType;
            this.timer = 60 * 20;
            switch (aiType) {
                case RUN, CONTROL_RUN -> mannequin.getEquipment().setItemInMainHand(ItemStack.of(Material.BOW));
                case SPYGLASS -> {
                    mannequin.getEquipment().setItemInMainHand(ItemStack.of(Material.SPYGLASS));
                    Vector velocity = mannequin.getLocation().getDirection().multiply(movementSpeed);
                    mannequin.setVelocity(velocity);
                }
            }
            context.glowManager().addGlow(owner, mannequin);

            if (cloneUpdateTask != null && !cloneUpdateTask.isCancelled()) return;

            cloneUpdateTask = new BukkitRunnable() {
                @Override
                public void run() {
                    update();
                }
            }.runTaskTimer(context.gutitubow(), 0L, 1L);
        }

        private UUID getEntityUUID() {
            return mannequin.getUniqueId();
        }

        private void update() {
            timer--;
            if (timer <= 0) {
                mannequin.getWorld().spawnParticle(Particle.POOF, mannequin.getLocation(), 20, 1.0, 1.0, 1.0, 0);
                mannequin.remove();
                remove();
                return;
            }
            if (shootTarget == null) {
                Player owner = getPlayer();
                switch (aiType) {
                    case RUN -> {
                        double oldY = mannequin.getVelocity().getY();
                        Vector velocity = mannequin.getLocation().getDirection().multiply(movementSpeed);
                        double offsetY = mannequin.isOnGround() ? 0.45 : 0;
                        velocity.setY(oldY + offsetY);
                        mannequin.setVelocity(velocity);
                        if (mannequin.isOnGround()) {
                            Block block = mannequin.getLocation().getBlock().getRelative(BlockFace.DOWN);

                            mannequin.getWorld().spawnParticle(
                                    Particle.BLOCK,
                                    mannequin.getLocation(),
                                    2,
                                    0.2, 0.1, 0.2,
                                    block.getBlockData()
                            );
                        }
                    }
                    case CONTROL_RUN -> {
                        double oldY = mannequin.getVelocity().getY();
                        Vector velocity;
                        if (owner == null) {
                            velocity = mannequin.getLocation().getDirection().multiply(movementSpeed);
                        } else {
                            Vector direction = owner.getLocation().getDirection();
                            direction.setY(0);
                            direction.normalize();
                            velocity = direction.multiply(movementSpeed);
                            setRotation(owner);

                            if (mannequin.isOnGround()) {
                                Block block = mannequin.getLocation().getBlock().getRelative(BlockFace.DOWN);

                                mannequin.getWorld().spawnParticle(
                                        Particle.BLOCK,
                                        mannequin.getLocation(),
                                        2,
                                        0.2, 0.1, 0.2,
                                        block.getBlockData()
                                );
                            }

                        }
                        double offsetY = mannequin.isOnGround() ? 0.45 : 0;
                        velocity.setY(oldY + offsetY);
                        mannequin.setVelocity(velocity);
                    }
                    case SPYGLASS -> {
                        if (owner != null) {

                            setRotation(owner);
                            mannequin.setSneaking(false);

                            mannequin.startUsingItem(EquipmentSlot.HAND);
                        }
                    }
                }

            } else {
                shootProgress++;
                if (shootProgress >= 30) {
                    Vector velocity = shootTarget.getVelocity();
                    velocity.setY(0);
                    shoot(shootTarget.getEyeLocation().add(velocity).add(0.0, -0.5, 0.0));
                    mannequin.getWorld().spawnParticle(Particle.POOF, mannequin.getLocation(), 20, 1.0, 1.0, 1.0, 0);


                    shootProgress = 0;
                    shootTarget = null;
                    remove();
                } else {
                    mannequin.setGlowing(true);
                    Vector direction = mannequin.getEyeLocation().subtract(shootTarget.getEyeLocation()).toVector().multiply(-1);
                    Location loc = mannequin.getLocation();
                    loc.setDirection(direction);
                    loc.setYaw(loc.getYaw());
                    loc.setPitch(loc.getPitch());
                    mannequin.setRotation(loc.getYaw(), loc.getPitch());
                    if (shootProgress <= 20 && shootProgress >= 5) {

                        mannequin.setPose(shootProgress % 4 < 2 ? Pose.STANDING : Pose.SNEAKING);
                    } else mannequin.setSneaking(true);
                }
            }
        }

        private void setRotation(Player owner) {
            Location loc = mannequin.getLocation();
            loc.setYaw(owner.getLocation().getYaw());
            loc.setPitch(owner.getLocation().getPitch());
            mannequin.setRotation(loc.getYaw(), loc.getPitch());
        }

        private void onHit(Player attacker) {
            applyCloneHitEffect(attacker);
            setShootTarget(attacker);
        }

        private void setShootTarget(Player target) {
            shootProgress = 0;
            shootTarget = target;
            mannequin.getEquipment().setItemInMainHand(ItemStack.of(Material.BOW));
            mannequin.startUsingItem(EquipmentSlot.HAND);
        }

        private void shoot(Location location) {
            Vector velocity = getArrowVelocity(location, mannequin.getEyeLocation());
            if (velocity == null) return;
            Arrow arrow = mannequin.getWorld().spawn(mannequin.getEyeLocation(), Arrow.class);
            arrow.setShooter(Bukkit.getPlayer(ownerUUID));
            arrow.setVelocity(velocity);
            ArrowUtil.setFixRotation(arrow, velocity);
        }

        private Vector getArrowVelocity(Location target, Location start) {
            Vector diff = target.clone().subtract(start).toVector();

            double dx = diff.getX();
            double dy = diff.getY();
            double dz = diff.getZ();

            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

            double v = 3.0; // フルチャージ
            double g = 0.05;

            double v2 = v * v;
            double underRoot = v2 * v2 - g * (g * horizontalDistance * horizontalDistance + 2 * dy * v2);

            if (underRoot < 0) {
                // 届かない
                return null;
            }

            double sqrt = Math.sqrt(underRoot);

// 高い軌道 or 低い軌道（好きな方）
            double tanTheta = (v2 - sqrt) / (g * horizontalDistance);
// double tanTheta = (v2 + sqrt) / (g * horizontalDistance); // 高い軌道

            double cosTheta = 1 / Math.sqrt(1 + tanTheta * tanTheta);
            double sinTheta = tanTheta * cosTheta;

// 水平方向の正規化
            Vector horizontalDir = new Vector(dx, 0, dz).normalize();

// 最終velocity
            return horizontalDir.multiply(v * cosTheta)
                    .setY(v * sinTheta);
        }

        private void remove() {
            clones.remove(this);
            mannequin.remove();
            if (cloneUpdateTask != null) cloneUpdateTask.cancel();
            cloneUpdateTask = null;
        }
    }

    public enum CloneAIType {
        RUN,
        CONTROL_RUN,
        SPYGLASS
    }

}
