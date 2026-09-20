package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.ability.data.CloneAIType;
import com.azerl0.gutitubow.event.gameplayer.PlayerFixLeftClickEvent;
import com.azerl0.gutitubow.fakeplayer.FakePlayer;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.util.ArrowUtil;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.PlayerAnimation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
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
    private final int CLONE_RESTOCK_TICK = 20 * 20;
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
    public void onAnyArrowHit(ProjectileHitEvent event) {
        if (event.isCancelled()) return;
        if (event.getHitEntity() instanceof Player hitPlayer) {
            Clone clone = getClone(hitPlayer.getUniqueId());
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
        GamePlayer gamePlayer = context.game().getGamePlayer(player);
        gamePlayer.setFlashParticleTask();
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
        private final FakePlayer fakePlayer;
        private final NPC npc;
        private final Player clonePlayer;
        private BukkitTask cloneUpdateTask;
        private Player shootTarget;
        private int shootProgress = 0;
        private final CloneAIType aiType;
        private final double movementSpeed = 0.35;
        private int timer;

        private Clone(Player owner, Location location, CloneAIType aiType) {
            this.ownerUUID = owner.getUniqueId();
            fakePlayer = context.fakePlayerManager().createFakePlayer(owner.getName(), location);
            npc = fakePlayer.getNpc();
            clonePlayer = (Player) npc.getEntity();
            this.aiType = aiType;
            this.timer = 60 * 20;
            switch (aiType) {
                case RUN, CONTROL_RUN -> {
                    clonePlayer.getEquipment().setItemInMainHand(ItemStack.of(Material.BOW));
                }
                case SPYGLASS -> {
                    clonePlayer.getEquipment().setItemInMainHand(ItemStack.of(Material.SPYGLASS));
                    Vector velocity = clonePlayer.getLocation().getDirection().multiply(movementSpeed);
                    clonePlayer.setVelocity(velocity);
                }
            }
            context.glowManager().addGlow(owner, clonePlayer);

            Bukkit.getScheduler().runTaskLater(context.gutitubow(), () -> {
//                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "npc select " + npc.getId());
//                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "npc skinlayers --cape true");
//                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "npc skinlayers --cape false");
                clonePlayer.setSneaking(true);
                clonePlayer.setSneaking(false);
            }, 2L);

            if (cloneUpdateTask != null && !cloneUpdateTask.isCancelled()) return;

            cloneUpdateTask = new BukkitRunnable() {
                @Override
                public void run() {
                    update();
                }
            }.runTaskTimer(context.gutitubow(), 0L, 1L);
        }

        private UUID getEntityUUID() {
            return fakePlayer.getNpc().getEntity().getUniqueId();
        }

        private void update() {
            timer--;
            if (timer <= 0) {
                clonePlayer.getWorld().spawnParticle(Particle.POOF, clonePlayer.getLocation(), 20, 1.0, 1.0, 1.0, 0);
                npc.despawn();
                remove();
                return;
            }
            if (shootTarget == null) {
                Player owner = getPlayer();
                switch (aiType) {
                    case RUN -> {
                        double oldY = clonePlayer.getVelocity().getY();
                        Vector velocity = clonePlayer.getLocation().getDirection().multiply(movementSpeed);
                        double offsetY = ((Entity) clonePlayer).isOnGround() ? 0.45 : 0;
                        velocity.setY(oldY + offsetY);
                        clonePlayer.setVelocity(velocity);
                    }
                    case CONTROL_RUN -> {
                        double oldY = clonePlayer.getVelocity().getY();
                        Vector velocity;
                        if (owner == null) {
                            velocity = clonePlayer.getLocation().getDirection().multiply(movementSpeed);
                        } else {
                            Vector direction = owner.getLocation().getDirection();
                            direction.setY(0);
                            direction.normalize();
                            velocity = direction.multiply(movementSpeed);

                            setRotation(owner);

                        }
                        double offsetY = ((Entity) clonePlayer).isOnGround() ? 0.45 : 0;
                        velocity.setY(oldY + offsetY);
                        clonePlayer.setVelocity(velocity);
                    }
                    case SPYGLASS -> {
                        if (owner != null) {

                            setRotation(owner);
                            clonePlayer.setSneaking(false);

                            try {
                                PlayerAnimation.STOP_USE_ITEM.play(clonePlayer);
                                PlayerAnimation.START_USE_MAINHAND_ITEM.play(clonePlayer);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }

            } else {
                shootProgress++;
                if (shootProgress >= 30) {
                    Vector velocity = shootTarget.getVelocity();
                    velocity.setY(0);
                    shoot(shootTarget.getEyeLocation().add(velocity).add(0.0, -0.5, 0.0));
                    try {
                        PlayerAnimation.STOP_USE_ITEM.play(clonePlayer);
                    } catch (Exception ignored) {
                    }
                    clonePlayer.getWorld().spawnParticle(Particle.POOF, clonePlayer.getLocation(), 20, 1.0, 1.0, 1.0, 0);


                    shootProgress = 0;
                    shootTarget = null;
                    remove();
                } else {
                    clonePlayer.setGlowing(true);
                    Vector direction = clonePlayer.getEyeLocation().subtract(shootTarget.getEyeLocation()).toVector().multiply(-1);
                    Location loc = clonePlayer.getLocation();
                    loc.setDirection(direction);
                    loc.setYaw(loc.getYaw());
                    loc.setPitch(loc.getPitch());
                    clonePlayer.setRotation(loc.getYaw(), loc.getPitch());
                    if (shootProgress <= 20 && shootProgress >= 5) {

                        clonePlayer.setSneaking(shootProgress % 4 < 2);
                    } else clonePlayer.setSneaking(true);
                    try {
                        PlayerAnimation.START_USE_MAINHAND_ITEM.play(clonePlayer);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        private void setRotation(Player owner) {
            Location loc = clonePlayer.getLocation();
            loc.setYaw(owner.getLocation().getYaw());
            loc.setPitch(owner.getLocation().getPitch());
            clonePlayer.setRotation(loc.getYaw(), loc.getPitch());
        }

        private void onHit(Player attacker) {
            applyCloneHitEffect(attacker);
            setShootTarget(attacker);
        }

        private void setShootTarget(Player target) {
            shootProgress = 0;
            shootTarget = target;
            clonePlayer.getInventory().setItemInMainHand(ItemStack.of(Material.BOW));
        }

        private void shoot(Location location) {
            Vector velocity = getArrowVelocity(location, clonePlayer.getEyeLocation());
            if (velocity == null) return;
            Arrow arrow = clonePlayer.getWorld().spawn(clonePlayer.getEyeLocation(), Arrow.class);
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
            npc.destroy();
            if (cloneUpdateTask != null) cloneUpdateTask.cancel();
            cloneUpdateTask = null;
        }
    }
}
