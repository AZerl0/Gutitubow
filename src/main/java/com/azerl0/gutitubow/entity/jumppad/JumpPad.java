package com.azerl0.gutitubow.entity.jumppad;

import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import com.azerl0.gutitubow.entity.GutitubowEntityType;
import com.azerl0.gutitubow.util.MathUtil;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class JumpPad extends GutitubowEntity {
    private double size;
    private float yaw;
    private float pitch;
    private float power;
    private final Map<UUID, Integer> usedCooldown = new HashMap<>();


    public JumpPad(GutitubowEntityManager gutitubowEntityManager, Location location) {
        super(gutitubowEntityManager, location);
    }

    @Override
    public @NotNull GutitubowEntityType getType() {
        return GutitubowEntityType.JUMP_PAD;
    }

    @Override
    public @NotNull Location getLocation() {
        return location;
    }

    @Override
    public void teleport(Location location) {
        this.location = location;
    }

    @Override
    public Set<UUID> getPartEntitiesUUID() {
        return Set.of();
    }

    @Override
    protected void onRemove() {

    }

    @Override
    protected void summon(Location location) {
        this.location = location;
    }

    @Override
    protected void onTick() {

        if (location.getWorld().getGameTime() % 10 == 0) {
            spawnCircle(
                    location,
                    yaw,
                    pitch,
                    size / 2,
                (int) (size * 5)
            );
        }

        for (UUID useUUID : new HashSet<>(usedCooldown.keySet())) {
            int cooldown = usedCooldown.get(useUUID) - 1;
            if (cooldown <= 0) {
                usedCooldown.remove(useUUID);
            } else {
                usedCooldown.put(useUUID, cooldown);
            }
        }

        double r = size / 2;
        for (Entity nearEntity : location.getNearbyEntities(r, r, r)) {
            if (nearEntity instanceof FallingBlock) continue;
            if (!usedCooldown.containsKey(nearEntity.getUniqueId())) applyVelocity(nearEntity);
        }
    }

    public void setSize(double size) {
        this.size = size;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPower(float power) {
        this.power = power;
    }

    public void applyVelocity(Entity entity) {
        Vector velocity = MathUtil.getDirection(yaw, pitch).normalize().multiply(power);

        JumpPadMover mover = entityManager.spawnGutitubowEntity(entity.getLocation(), JumpPadMover.class);
        mover.setConnectionEntity(entity);
        mover.setVelocity(velocity);

        entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1.0f, 1.0f);
        usedCooldown.put(entity.getUniqueId(), 20);
    }

    public static void spawnCircle(
            Location center,
            float yaw,
            float pitch,
            double radius,
            int points
    ) {
        // 円の法線となる方向ベクトル
        Vector normal = new Vector(
                -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)),
                -Math.sin(Math.toRadians(pitch)),
                Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))
        ).normalize();

        // 円の平面上にある基底ベクトルを2つ求める
        Vector u = new Vector(0, 1, 0).crossProduct(normal);

        // 真上・真下を向いている場合の対策
        if (u.lengthSquared() < 1.0E-10) {
            u = new Vector(1, 0, 0);
        }

        u.normalize();

        Vector v = normal.clone().crossProduct(u).normalize();


        ParticleBuilder particleBuilder = Particle.DUST.builder()
                .color(Color.fromRGB(3145600), 2.0F);


        for (int i = 0; i < points; i++) {
            double angle = 2.0 * Math.PI * i / points;

            Vector offset = u.clone().multiply(radius * Math.cos(angle))
                    .add(v.clone().multiply(radius * Math.sin(angle)));

            Location loc = center.clone().add(offset);

            particleBuilder.location(loc).receivers(32, true).spawn();
        }
    }
}
