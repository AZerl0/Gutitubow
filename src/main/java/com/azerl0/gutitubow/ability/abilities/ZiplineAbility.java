package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.entity.zipline.ZiplineNode;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerShootEvent;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ZiplineAbility extends Ability {
    private @Nullable Chicken leashChicken;
    private Set<ZiplineNode> ziplineNodeSet = new HashSet<>();


    public ZiplineAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.ZIPLINE;
    }

    @Override
    protected void onReset() {
        if (leashChicken != null) leashChicken.remove();
        for (ZiplineNode ziplineNode : ziplineNodeSet) {
            ziplineNode.remove();
        }
    }

    @Override
    public void onGamePlayerShoot(GamePlayerShootEvent event) {
        if (event.isCancelled()) return;
        Player player = getPlayer();
        if (player == null) return;
        if (event.getProjectile() == null) return;
        if (!hasCompassCooldown() && player.isSneaking()) {

            event.getProjectile().addScoreboardTag("gutitubow.ziplineArrow");
            if (leashChicken != null) leashChicken.remove();
            leashChicken = player.getWorld().spawn(event.getProjectile().getLocation(), Chicken.class);
            leashChicken.addScoreboardTag("gutitubow.cancelUnleash");
            leashChicken.setCollidable(false);
            leashChicken.setSilent(true);
            leashChicken.setAI(false);
            leashChicken.setInvulnerable(true);
            leashChicken.setInvisible(true);
            Objects.requireNonNull(leashChicken.getAttribute(Attribute.SCALE)).setBaseValue(0.0);
            leashChicken.setLeashHolder(player);
            event.getProjectile().addPassenger(leashChicken);
            setCompassCooldown(isDebugMode() ? 2 * 20 : 30 * 20);
            startCompassCooldownTask();
        }
    }

    @Override
    public void onArrowHit(ProjectileHitEvent event) {
        if (event.getEntity().getScoreboardTags().contains("gutitubow.ziplineArrow") && event.getHitBlock() != null && getPlayer() != null) {

            for (ZiplineNode ziplineNode : ziplineNodeSet) {
                ziplineNode.remove();
            }
            ziplineNodeSet.clear();

            Location start = getPlayer().getLocation();
            Location end = event.getEntity().getLocation().add(0.0, 0.25, 0.0);
            // https://bugs.mojang.com/browse/MC-112474
            Vector offsetDir = event.getEntity().getLocation().getDirection();
            offsetDir.setZ(-offsetDir.getZ());
            end.add(offsetDir.multiply(0.1));
            if (leashChicken != null) leashChicken.remove();
            leashChicken = null;
            if (!start.clone().add(0.0, 1.0, 0.0).getBlock().isCollidable()) {
                start.add(0.0, 1.0, 0.0);
            }

            if (event.getHitBlockFace() == BlockFace.UP && !end.clone().add(0.0, 1.0, 0.0).getBlock().isCollidable()) {
                end.add(0.0, 1.0, 0.0);
            }

            ZiplineNode startNode = createNode(start);
            ZiplineNode endNode = createNode(end);

            int life = 120 * 20;
            startNode.addConnectionNode(endNode);
            startNode.setLife(life);
            endNode.addConnectionNode(startNode);
            endNode.setLife(life);
            ziplineNodeSet.add(startNode);
            ziplineNodeSet.add(endNode);
        }
    }

    @Override
    protected void onTick() {
        if (leashChicken != null) {
            if (leashChicken.isInsideVehicle()) {
                if (getPlayer() != null) leashChicken.setLeashHolder(getPlayer());
            }
        }
    }

    private ZiplineNode createNode(@NotNull Location location) {
        return context.getGutitubowManagers().getGutitubowEntityManager().spawnGutitubowEntity(location, ZiplineNode.class);
    }
}
