package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.util.MathUtil;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class DashAbility extends Ability {
    private int dashingTick;
    private BukkitTask task = null;
    private final int topSpeedTick = 5 * 20;
    private final Set<Block> replaceableBlocks;
    private final Set<Block> replacedBlocks;

    public DashAbility(AbilityContext context) {
        super(context);
        this.dashingTick = 0;
        this.replaceableBlocks = new java.util.HashSet<>();
        this.replacedBlocks = new java.util.HashSet<>();
    }

    @Override
    public final @NotNull AbilityType getType() {
        return AbilityType.DASH;
    }

    @Override
    public void onReset() {
        stopSprinting();
    }

    @Override
    public void onToggleSprint(PlayerToggleSprintEvent event) {
        Player player = getPlayer();

        if (event.isSprinting()) {
            if (task != null && !task.isCancelled()) return;

            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isSprinting() || player.isSwimming() || player.isFlying() || player.isSneaking()) {
                        stopSprinting();
                        this.cancel();
                        return;
                    }
                    Sprinting(player);
                }
            }.runTaskTimer(context.gutitubow(), 0L, 1L);
        }
    }

    private void Sprinting(Player player) {
        this.dashingTick += 1;

        if (this.dashingTick < topSpeedTick && this.dashingTick >= 1.2 * 20) {
            double speed = MathUtil.getLerp(this.dashingTick, 1.2 * 20, topSpeedTick, 0.1, 0.175);
            Objects.requireNonNull(player.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(speed);
        }

        if (this.dashingTick == topSpeedTick) {
            player.getWorld().playSound(player, Sound.ENTITY_WITHER_SHOOT, 0.1f, 1.0f);
            Objects.requireNonNull(player.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.1);
        }

        if (isTopSpeed()) {
            PotionEffect effect = new PotionEffect(PotionEffectType.SPEED, 5, 4, true, false, true);
            player.addPotionEffect(effect);

            Objects.requireNonNull(player.getAttribute(Attribute.STEP_HEIGHT)).setBaseValue(1.0);

            Random random = new Random();
            double particleRadius = 0.5;
            for (int i = 0; i < 2; i++) {
                double randomX = random.nextDouble(-particleRadius, particleRadius);
                double randomZ = random.nextDouble(-particleRadius, particleRadius);
                player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation().clone().add(randomX, 0.5, randomZ), 1);
            }

            PotionEffect slowness = new PotionEffect(PotionEffectType.SLOWNESS, 20, 1, true, true, true);
            for (Player nearPlayer : player.getLocation().getNearbyPlayers(4)) {
                if (context.game().isTeammate(player, nearPlayer)) continue;
                GameMode nearPlayerGameMode = nearPlayer.getGameMode();
                if (nearPlayerGameMode == GameMode.SPECTATOR || nearPlayerGameMode == GameMode.CREATIVE) continue;
                nearPlayer.addPotionEffect(slowness);
            }

            double replaceRange = 3;
            Location baseLoc = player.getLocation();

            checkReplaceFill(baseLoc.clone().add(replaceRange, 1, replaceRange), baseLoc.clone().add(-replaceRange, -1, -replaceRange));//

            undoWater();
            for (Block block : this.replaceableBlocks) {
                checkReplaceBlock(block);
            }
            this.replaceableBlocks.clear();

        }
    }

    private boolean isTopSpeed() {
        return dashingTick >= topSpeedTick;
    }

    private void stopSprinting() {
        Player player = getPlayer();
        this.dashingTick = 0;
        Objects.requireNonNull(player.getAttribute(Attribute.STEP_HEIGHT)).setBaseValue(0.6);
        Objects.requireNonNull(player.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.1);
        undoWater();
    }

    @Override
    public void onPressJumpKey(PlayerInputEvent event) {
        Player player = event.getPlayer();
        if (this.dashingTick >= topSpeedTick) {
            Vector velocity = player.getLocation().getDirection().clone().setY(0.25).normalize().multiply(0.8);
            player.setVelocity(velocity);
        }
        stopSprinting();
    }

    @Override
    public void onJump(PlayerJumpEvent event) {
        stopSprinting();
    }

    @Override
    public void onPlayerRightClickBow(PlayerInteractEvent event) {
        stopSprinting();
    }

    private void checkReplaceFill(Location a, Location b) {

        if (a.getWorld() != b.getWorld()) return;
        int minX = Math.min(a.getBlockX(), b.getBlockX());
        int minY = Math.min(a.getBlockY(), b.getBlockY());
        int minZ = Math.min(a.getBlockZ(), b.getBlockZ());
        for (int x = 0; x <= Math.abs(a.getBlockX() - b.getBlockX()); x++) {
            for (int y = 0; y <= Math.abs(a.getBlockY() - b.getBlockY()); y++) {
                for (int z = 0; z <= Math.abs(a.getBlockZ() - b.getBlockZ()); z++) {
                    Block block = a.getWorld().getBlockAt(minX + x, minY + y, minZ + z);
                    this.replaceableBlocks.add(block);//

                }
            }


        }
    }


    private void checkReplaceBlock(Block block) {
        if (block.getType() == Material.WATER) {
            BlockData data = block.getBlockData();

            if (data instanceof Levelled levelled) {
                if (levelled.getLevel() == 0) {
                    this.replacedBlocks.add(block);
                    block.setType(Material.BLUE_STAINED_GLASS);
                }
            }
        }
    }

    private void undoWater() {
        for (Block block : this.replacedBlocks) {
//            if (!this.replaceableBlocks.contains(block)) block.setType(Material.WATER);
            block.setType(Material.WATER);
        }
        this.replacedBlocks.clear();
    }
}
