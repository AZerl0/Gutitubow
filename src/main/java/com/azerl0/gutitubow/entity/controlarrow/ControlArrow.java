package com.azerl0.gutitubow.entity.controlarrow;

import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import com.azerl0.gutitubow.entity.GutitubowEntityType;
import com.azerl0.gutitubow.util.EntityUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class ControlArrow extends GutitubowEntity {
    private float speed;
    private Arrow arrow;
    private boolean controlling;

    public ControlArrow(GutitubowEntityManager gutitubowEntityManager, Location location) {
        super(gutitubowEntityManager, location);
    }

    @Override
    public @NotNull GutitubowEntityType getType() {
        return GutitubowEntityType.CONTROL_ARROW;
    }

    @Override
    public @NotNull Location getLocation() {
        return arrow.getLocation();
    }

    @Override
    public void teleport(Location location) {
        arrow.teleport(location);
    }

    @Override
    public Set<UUID> getPartEntitiesUUID() {
        return Set.of(arrow.getUniqueId());
    }

    @Override
    protected void onRemove() {
        arrow.remove();
    }

    @Override
    protected void summon(Location location) {
        arrow = location.getWorld().spawn(location, Arrow.class);
        arrow.setCritical(false);
        EntityUtil.setGlow(arrow, NamedTextColor.DARK_PURPLE);
        arrow.setGlowing(true);
        arrow.setGravity(false);
        controlling = true;
    }

    @Override
    protected void onTick() {
        if (!(arrow.getShooter() != null && arrow.getShooter() instanceof LivingEntity shooter)) return;
        if (!controlling) return;
        if (!arrow.isValid()) {
            remove();
            return;
        }

        arrow.getWorld().spawnParticle(
                Particle.WITCH,
                arrow.getLocation(),
                2,
                0, 0, 0,
                0
        );
        Vector v = shooter.getEyeLocation().getDirection().multiply(speed);

        arrow.setVelocity(v);
    }

    @Override
    protected void onAnyProjectileHit(ProjectileHitEvent event) {
        if (event.getHitBlock() == null) return;
        if (!controlling) return;
        controlling = false;
        arrow.setGravity(true);
    }

    public Arrow getArrow() {
        return arrow;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isControlling() {
        return controlling;
    }

    public void setControlling(boolean controlling) {
        this.controlling = controlling;
    }
}
