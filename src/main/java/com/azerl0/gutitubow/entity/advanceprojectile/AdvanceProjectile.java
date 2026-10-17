package com.azerl0.gutitubow.entity.advanceprojectile;

import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import com.azerl0.gutitubow.entity.GutitubowEntityType;
import com.azerl0.gutitubow.util.EntityUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class AdvanceProjectile extends GutitubowEntity {
    private Projectile projectile;
    private boolean isControlling;
    private float controlSpeed;
    private @Nullable Consumer<Location> particle;


    public AdvanceProjectile(GutitubowEntityManager gutitubowEntityManager, Location location) {
        super(gutitubowEntityManager, location);
    }

    @Override
    public @NotNull GutitubowEntityType getType() {
        return GutitubowEntityType.ADVANCE_ARROW;
    }

    @Override
    public @NotNull Location getLocation() {
        return projectile.getLocation();
    }

    @Override
    public void teleport(Location location) {
        projectile.teleport(location);
    }

    @Override
    public Set<UUID> getPartEntitiesUUID() {
        return Set.of(projectile.getUniqueId());
    }

    @Override
    protected void onRemove() {
        projectile.remove();
    }

    @Override
    protected void summon(Location location) {
        projectile = location.getWorld().spawn(location, Arrow.class);
    }

    @Override
    protected void onAnyProjectileHit(ProjectileHitEvent event) {
        Optional<GutitubowEntity> entity = entityManager.getGutitubowEntity(event.getEntity());
        if (entity.isEmpty() || entity.get() != this) return;

        if (isControlling) {

        }
    }

    @Override
    protected void onTick() {
        if (!projectile.isValid()) {
            remove();
            return;
        }
        boolean isInBlock = projectile instanceof AbstractArrow abstractArrow && !abstractArrow.isInBlock();
        if (isControlling) {
            if (projectile.getShooter() != null && projectile.getShooter() instanceof LivingEntity shooter && isInBlock) {
                Vector velocity = shooter.getEyeLocation().getDirection().normalize().multiply(controlSpeed);
                projectile.setVelocity(velocity);
                projectile.getWorld().spawnParticle(
                        Particle.WITCH,
                        projectile.getLocation(),
                        2,
                        0, 0, 0,
                        0
                );
            }
        } else {
            if (particle != null && isInBlock) particle.accept(getLocation());
        }

    }

    public Projectile getProjectile() {
        return projectile;
    }

    public void replaceProjectile(@NotNull Projectile projectile) {
        this.projectile.remove();
        this.projectile = projectile;
    }

    public boolean isControlling() {
        return isControlling;
    }

    public void setControlling(boolean controlling) {
        isControlling = controlling;
    }

    public float getControlSpeed() {
        return controlSpeed;
    }

    public void setControlSpeed(float controlSpeed) {
        this.controlSpeed = controlSpeed;
    }

    public void setControlEffect(boolean glowColorOverride) {
        if (projectile instanceof AbstractArrow abstractArrow) abstractArrow.setCritical(false);
        if (glowColorOverride) {
            EntityUtil.setGlow(projectile, NamedTextColor.DARK_PURPLE);
            projectile.setGlowing(true);
        }
        projectile.setGravity(false);
    }

    public void setParticle(@NotNull Consumer<Location> particle) {
        this.particle = particle;
    }
}
