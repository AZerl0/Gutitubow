package com.azerl0.gutitubow.entity.jumppad;

import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import com.azerl0.gutitubow.entity.GutitubowEntityType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class JumpPadMover extends GutitubowEntity {
    private FallingBlock fallingBlock;
    private @Nullable Entity connectionEntity;

    public JumpPadMover(GutitubowEntityManager gutitubowEntityManager, Location location) {
        super(gutitubowEntityManager, location);
    }

    @Override
    public @NotNull GutitubowEntityType getType() {
        return GutitubowEntityType.JUMP_PAD_MOVER;
    }

    @Override
    public @NotNull Location getLocation() {
        return fallingBlock.getLocation();
    }

    @Override
    public void teleport(Location location) {

    }

    @Override
    public Set<UUID> getPartEntitiesUUID() {
        return Set.of(fallingBlock.getUniqueId());
    }

    @Override
    protected void onRemove() {
        fallingBlock.remove();
    }

    @Override
    protected void summon(Location location) {
        fallingBlock = location.getWorld().spawnFallingBlock(location, Material.MOVING_PISTON.createBlockData());
        fallingBlock.setCancelDrop(true);
        fallingBlock.setDropItem(false);
    }

    @Override
    protected void onTick() {
        if (!fallingBlock.isValid()) {
            remove();
            return;
        }
        if (fallingBlock.getVelocity().getY() <= 0) {
            remove();
            return;
        }

        if (connectionEntity != null) {
            connectionEntity.setVelocity(connectionEntity.getVelocity().add(fallingBlock.getVelocity()));
        }
    }

    public void setConnectionEntity(@Nullable Entity connectionEntity) {
        this.connectionEntity = connectionEntity;
    }

    public void setVelocity(Vector velocity) {
        fallingBlock.setVelocity(velocity);
    }
}
