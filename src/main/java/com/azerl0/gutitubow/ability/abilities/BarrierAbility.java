package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.map.MapRepairManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

public class BarrierAbility extends Ability {
    private final int BARRIER_RESTORE_TICK = 20 * 20;
    private int barrierRestockCharge;
    private int barrierStack;
    private BukkitTask barrierRestockTask = null;

    public BarrierAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public final @NotNull AbilityType getType() {
        return AbilityType.BARRIER;
    }

    @Override
    public void init(Player player) {
        this.playerUUID = player.getUniqueId();
        this.barrierStack = 3;
        this.barrierRestockCharge = 0;
        if (barrierRestockTask != null && !barrierRestockTask.isCancelled()) return;

        barrierRestockTask = new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimer(context.gutitubow(), 0L, 1L);
    }

    @Override
    public void reset() {
        super.reset();
        if (barrierRestockTask != null) barrierRestockTask.cancel();
        barrierRestockTask = null;
    }

    private void update() {
        if (barrierStack < 3) {
            barrierRestockCharge++;
                    if (barrierRestockCharge >= BARRIER_RESTORE_TICK) {
                        barrierStack++;
                        barrierRestockCharge = 0;
                    }
        }

        Component message = Component.text("Barrier: ");
        for (int i = 0; i < 3; i++) {
            message = message.append(Component.text(i < barrierStack ? "○" : "-"));
        }
        getPlayer().sendActionBar(message);
    }

    @Override
    public void onPlayerLeftClickBow(PlayerInteractEvent event) {

        if (barrierStack <= 0 || hasCompassCooldown()) return;
        placeBarrier();
        setCompassCooldown(20);
        startCompassCooldownTask();
    }

    private void placeBarrier() {
        Location baseLoc = getBaseLocation(getPlayer());

        //Up
        fiLLGlass(baseLoc.clone().add(1, 2, 1), baseLoc.clone().add(-1, 2, -1));
        //East
        fiLLGlass(baseLoc.clone().add(2, 1, 1), baseLoc.clone().add(2, -1, -1));
        //West
        fiLLGlass(baseLoc.clone().add(-2, 1, 1), baseLoc.clone().add(-2, -1, -1));
        //South
        fiLLGlass(baseLoc.clone().add(1, 1, 2), baseLoc.clone().add(-1, -1, 2));
        //North
        fiLLGlass(baseLoc.clone().add(1, 1, -2), baseLoc.clone().add(-1, -1, -2));
        //Down
        fiLLGlass(baseLoc.clone().add(1, -2, 1), baseLoc.clone().add(-1, -2, -1));

        baseLoc.getWorld().playSound(baseLoc, Sound.BLOCK_ANVIL_PLACE, 1.0f, 2.0f);
        barrierStack--;
    }

    private @NotNull Location getBaseLocation(Player player) {
        int maxDistance = 5;

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
            double offsetY = (dirY > 0.3 || dirY < -0.25 ? 2 : 0);
            return blockResult.getHitBlock().getLocation().add(0, offsetY, 0);
        }
        return player.getEyeLocation()
                .add(player.getEyeLocation().getDirection().multiply(maxDistance));
    }

    private void fiLLGlass(Location a, Location b) {

        if (a.getWorld() != b.getWorld()) return;
        int minX = Math.min(a.getBlockX(), b.getBlockX());
        int minY = Math.min(a.getBlockY(), b.getBlockY());
        int minZ = Math.min(a.getBlockZ(), b.getBlockZ());
        for (int x = 0; x <= Math.abs(a.getBlockX() - b.getBlockX()); x++) {
            for (int y = 0; y <= Math.abs(a.getBlockY() - b.getBlockY()); y++) {
                for (int z = 0; z <= Math.abs(a.getBlockZ() - b.getBlockZ()); z++) {
                    Block block = a.getWorld().getBlockAt(minX + x, minY + y, minZ + z);
                    setGlass(block.getLocation());
                }
            }
        }
    }

    private void setGlass(Location loc) {
        Block block = loc.getBlock();
        Material type = block.getType();

        if (!(type == Material.AIR || type == Material.WATER)) return;

        MapRepairManager mapRepairManager = context.game().getMapRepairManager();

        mapRepairManager.savedPlacedBlock(block);
        block.setType(Material.GLASS);
    }
}
