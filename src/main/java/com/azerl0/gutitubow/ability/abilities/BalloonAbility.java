package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerDeathEvent;
import com.azerl0.gutitubow.event.gameplayer.PlayerFixLeftClickEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.HappyGhast;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class BalloonAbility extends Ability {
    private final double HAPPY_GHAST_MAX_HEALTH = 20;
    private final int HAPPY_GHAST_REGENERATION_TICK = 12 * 20;
    private HappyGhast happyGhast;
    private double happyGhastHealth = HAPPY_GHAST_MAX_HEALTH;
    private int happyGhastRegenerationTick;
    private boolean canSummon = true;
    private final ItemStack harness = randomHarness();
    private BukkitTask updateTask;

    public BalloonAbility(AbilityContext context) {
        super(context);
    }


    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.BALLOON;
    }

    @Override
    public void onInit(Player player) {
        if (updateTask != null && !updateTask.isCancelled()) return;

        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimer(context.gutitubow(), 0L, 1L);
    }

    @Override
    public void onReset() {
        if (happyGhast != null) happyGhast.remove();
        if (updateTask != null) updateTask.cancel();
        updateTask = null;
    }

    @Override
    public void onLeftClick(PlayerFixLeftClickEvent event) {
        Player player = getPlayer();
        if (player == null) return;
        if (happyGhast == null) {
            if (hasCompassCooldown()) {
                player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(Component.text("まだクールダウン中です！").color(NamedTextColor.RED));
            } else {
                if (canSummon) {
                    sendOutGhast(player);

                } else {
                    player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);

                    player.sendMessage(Component.text("死亡したため完全に回復するまで召喚できません！").color(NamedTextColor.RED));
                }
            }
        } else {
            if (hasCompassCooldown()) {
                player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(Component.text("まだクールダウン中です！").color(NamedTextColor.RED));
            } else {
                recallGhast();
            }
        }
    }

    @Override
    public void onDeath(GamePlayerDeathEvent event) {
        if (happyGhast != null) {
            recallGhast();
        }
    }

    private void update() {
        Player player = getPlayer();
        if (player == null) return;
        if (happyGhast != null) {
            happyGhastHealth = happyGhast.getHealth();
            if (happyGhast.isDead()) {
                canSummon = false;
                happyGhast = null;
            } else {
                int highest = getHappyGhastLocationHighest(happyGhast.getLocation());
                int max = highest + 50;
                if (happyGhast.getLocation().getY() > max) {
                    happyGhast.setVelocity(happyGhast.getVelocity().add(new Vector(0.0, -0.2, 0.0)));
                }
            }

        }
        Component healthMessage = Component.text("ハッピーガスト: ").color(NamedTextColor.GREEN);
        for (int i = 0; i < HAPPY_GHAST_MAX_HEALTH / 2; i++) {
            healthMessage = healthMessage.append(Component.text("♥").color(i * 2 < happyGhastHealth ? NamedTextColor.RED : NamedTextColor.GRAY));
        }
        player.sendActionBar(healthMessage);
        if (happyGhastHealth < HAPPY_GHAST_MAX_HEALTH) {
            happyGhastRegenerationTick++;
            if (happyGhastRegenerationTick >= HAPPY_GHAST_REGENERATION_TICK) {
                happyGhastHealth = Math.min(happyGhastHealth + 2, HAPPY_GHAST_MAX_HEALTH);
                if (happyGhast != null) happyGhast.setHealth(happyGhastHealth);
                happyGhastRegenerationTick = 0;
                if (happyGhastHealth >= HAPPY_GHAST_MAX_HEALTH) canSummon = true;
            }
        }
    }

    private boolean canSendOutGhast(Location location) {
        double happyGhastSize = 4;
        return !isCollidable(
                location.clone().add(happyGhastSize / 2, happyGhastSize / 2, happyGhastSize / 2),
                location.clone().add(-happyGhastSize / 2, -happyGhastSize / 2, -happyGhastSize / 2)
        );
    }

    private boolean isCollidable(Location a, Location b) {
        if (a.getWorld() != b.getWorld()) return true;

        int minX = Math.min(a.getBlockX(), b.getBlockX());
        int minY = Math.min(a.getBlockY(), b.getBlockY());
        int minZ = Math.min(a.getBlockZ(), b.getBlockZ());
        for (int x = 0; x <= Math.abs(a.getBlockX() - b.getBlockX()); x++) {
            for (int y = 0; y <= Math.abs(a.getBlockY() - b.getBlockY()); y++) {
                for (int z = 0; z <= Math.abs(a.getBlockZ() - b.getBlockZ()); z++) {
                    Block block = a.getWorld().getBlockAt(minX + x, minY + y, minZ + z);
                    if (block.isCollidable()) return true;

                }
            }
        }
        return false;
    }

    private void sendOutGhast(@NotNull Player player) {
        Location location = getBaseLocation(player);
        if (canSendOutGhast(location)) {
            summonGhast(location);
            return;
        } else {
            if (player.getLocation().getDirection().getY() < 0.2) {
                location.add(0.0, 2, 0.0);
                if (canSendOutGhast(location)) {
                    summonGhast(location);
                    return;
                }
            }
        }
        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        player.sendMessage(Component.text("十分な広さがありません！").color(NamedTextColor.RED));
    }

    private void summonGhast(Location location) {
        happyGhast = location.getWorld().spawn(location, HappyGhast.class);
        happyGhast.setHealth(happyGhastHealth);
        happyGhast.setAI(false);
        happyGhast.getEquipment().setItem(EquipmentSlot.BODY, harness);
        location.getWorld().spawnParticle(Particle.POOF, location, 15, 2.0, 2.0, 2.0);
        location.getWorld().spawnParticle(Particle.SMOKE, location, 15, 2.0, 2.0, 2.0);
    }

    private void recallGhast() {
        Player player = getPlayer();
        happyGhast.remove();
        setCompassCooldown(20);
        if (player != null) {
            player.sendMessage(Component.text("ハッピーガストを回収しました！").color(NamedTextColor.GREEN));
        }
        happyGhast = null;
    }

    private @NotNull Location getBaseLocation(Player player) {
        double maxDistance = 3.5;

        RayTraceResult blockResult = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(),
                player.getLocation().getDirection(),
                maxDistance
        );

        RayTraceResult entityResult = player.getWorld().rayTraceEntities(player.getEyeLocation(),
                player.getLocation().getDirection(),
                maxDistance,
                entity -> !entity.equals(player)
        );

        if (entityResult != null && entityResult.getHitEntity() != null) {
            return entityResult.getHitEntity().getLocation();
        }

        if (blockResult != null && blockResult.getHitBlock() != null) {
            double dirY = player.getLocation().getDirection().getY();
            double offsetY = (dirY < -0.25 ? 1.5 : 0);
            return blockResult.getHitBlock().getLocation().add(0, offsetY, 0);
        }
        return player.getEyeLocation()
                .add(player.getEyeLocation().getDirection().multiply(maxDistance));
    }

    private int getHappyGhastLocationHighest(Location location) {
        return getLocationHighest(
                location.clone().add(2, 0, 2),
                location.clone().add(-2, 0, -2)
        );
    }

    private int getLocationHighest(Location a, Location b) {
        int highest = Integer.MIN_VALUE;

        int minX = Math.min(a.getBlockX(), b.getBlockX());
        int maxX = Math.max(a.getBlockX(), b.getBlockX());

        int minZ = Math.min(a.getBlockZ(), b.getBlockZ());
        int maxZ = Math.max(a.getBlockZ(), b.getBlockZ());

        World world = a.getWorld();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int y = world.getHighestBlockYAt(x, z);
                highest = Math.max(highest, y);
            }
        }

        return highest;
    }

    private ItemStack randomHarness() {
        List<Material> harness = List.of(
                Material.BLACK_HARNESS,
                Material.BLUE_HARNESS,
                Material.BROWN_HARNESS,
                Material.GRAY_HARNESS,
                Material.CYAN_HARNESS,
                Material.YELLOW_HARNESS,
                Material.RED_HARNESS,
                Material.ORANGE_HARNESS,
                Material.GREEN_HARNESS,
                Material.LIGHT_BLUE_HARNESS,
                Material.LIGHT_GRAY_HARNESS,
                Material.LIME_HARNESS,
                Material.MAGENTA_HARNESS,
                Material.PINK_HARNESS,
                Material.PURPLE_HARNESS
        );
        Random random = new Random();
        return ItemStack.of(harness.get(random.nextInt(0, harness.size() - 1)));
    }

}
